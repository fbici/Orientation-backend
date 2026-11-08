package com.orientation.orientationapp.ai.export.service.impl;
import com.orientation.orientationapp.ai.export.model.ExportResult;
import com.orientation.orientationapp.ai.export.service.ExportService;
import com.orientation.orientationapp.ai.history.*;
import com.orientation.orientationapp.modules.recommendation.service.PersistedRecommendationService;
import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DefaultExportService implements ExportService {
 private final DecisionHistoryService history;
 private final DecisionRecordRepository records;
 private final PersistedRecommendationService recommendations;
 private final ObjectMapper json;
 public record Cell(String field,String value) {}
 public List<Cell> content(UUID id) {
  Object payload;String kind;
  if(records.existsById(id)) {
   var row=history.get(id);kind=row.kind();
   if(kind.startsWith("CHAT"))throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Conversation export is not supported");
   payload=row;
  } else {payload=recommendations.get(id);kind="RECOMMENDATION";}
  var cells=new ArrayList<Cell>();
  cells.add(new Cell("report","ORIENTIA - "+kind));
  cells.add(new Cell("notice",kind.equals("SIMULATION")?"SIMULATION HYPOTHETIQUE - profil confirme inchange":"AIDE A LA DECISION - resultat historique"));
  cells.add(new Cell("disclaimer","NON OFFICIEL - compatibilite calculee, aucune probabilite ni garantie d'admission ou d'attribution de bourse"));
  cells.add(new Cell("exportedAt",java.time.Instant.now().toString()));
  flatten("",json.valueToTree(payload),cells);
  if(cells.size()>12000)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Report too large; select a smaller decision");
  return cells;
 }
 private static void flatten(String path,JsonNode node,List<Cell> cells) {
  if(cells.size()>12000)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Report too large");
  if(node==null||node.isNull())cells.add(new Cell(path,"INCONNU"));
  else if(node.isObject())node.fields().forEachRemaining(e->flatten(path.isEmpty()?e.getKey():path+"."+e.getKey(),e.getValue(),cells));
  else if(node.isArray()) {if(node.isEmpty())cells.add(new Cell(path,"AUCUNE DONNEE"));for(int i=0;i<node.size();i++)flatten(path+"["+i+"]",node.get(i),cells);}
  else cells.add(new Cell(path,node.asText()));
 }
 public static String safeCsvCell(String value) {
  String v=value==null?"INCONNU":value;
  String start=v.replaceFirst("^[\\s\\p{Z}\\uFEFF]*","");
  if(!start.isEmpty()&&"=+-@".indexOf(start.charAt(0))>=0||v.startsWith("\t")||v.startsWith("\r"))v="'"+v;
  return "\""+v.replace("\"","\"\"")+"\"";
 }
 public static byte[] csv(List<Cell> cells) {
  var out=new StringBuilder("\uFEFF\"Champ\",\"Valeur\"\r\n");
  for(var c:cells)out.append(safeCsvCell(c.field())).append(',').append(safeCsvCell(c.value())).append("\r\n");
  return out.toString().getBytes(StandardCharsets.UTF_8);
 }
 public static byte[] pdf(List<Cell> cells) {
  try(var document=new PDDocument();var out=new ByteArrayOutputStream()) {
   String label=cells.stream().anyMatch(c->c.value().contains("SIMULATION"))?"SIMULATION HYPOTHETIQUE - ":"";
   var font=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
   var lines=new ArrayList<String>();
   for(var cell:cells) {
    String text=printable(cell.field()+": "+cell.value(),font);
    for(String paragraph:text.split("\\R",-1)) {
     StringBuilder line=new StringBuilder();
     for(char ch:paragraph.toCharArray()) {
      if(font.getStringWidth(line.toString()+ch)/1000*9>510) {lines.add(line.toString());line.setLength(0);}
      line.append(ch);
     }
     lines.add(line.toString());
    }
   }
   for(int offset=0;offset<lines.size();offset+=52) {
    var page=new PDPage();document.addPage(page);
    try(var stream=new PDPageContentStream(document,page)) {
     stream.beginText();stream.setFont(font,9);stream.setLeading(13);stream.newLineAtOffset(40,750);
     stream.showText("ORIENTIA - "+label+"NON OFFICIEL - "+(offset/52+1));stream.newLine();
     for(int i=offset;i<Math.min(lines.size(),offset+52);i++){stream.showText(lines.get(i));stream.newLine();}
     stream.endText();
    }
   }
   document.save(out);return out.toByteArray();
  } catch(IOException e){throw new IllegalStateException("PDF generation failed",e);}
 }
 private static String printable(String s,PDType1Font font) {
  var text=new StringBuilder();
  s.codePoints().forEach(cp->{
   if(cp=='\n'||cp=='\r'){text.append('\n');return;}
   if(cp=='\t'){text.append("    ");return;}
   String c=new String(Character.toChars(cp));
   try{font.encode(c);text.append(c);}catch(Exception e){text.append(String.format("[U+%04X]",cp));}
  });return text.toString();
 }
 public static byte[] xlsx(List<Cell> cells) {
  try(var book=new XSSFWorkbook();var out=new ByteArrayOutputStream()) {
   var sheet=book.createSheet("ORIENTIA");
   var head=sheet.createRow(0);head.createCell(0).setCellValue("Champ");head.createCell(1).setCellValue("Valeur");
   int i=1;for(var cell:cells) {
    if(cell.value().length()>32767)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"A source exceeds XLSX cell length; use PDF or CSV");
    var row=sheet.createRow(i++);row.createCell(0).setCellValue(cell.field());row.createCell(1).setCellValue(cell.value());
   }
   sheet.setColumnWidth(0,12000);sheet.setColumnWidth(1,25000);book.write(out);return out.toByteArray();
  }catch(IOException e){throw new IllegalStateException("XLSX generation failed",e);}
 }
 private ExportResult result(UUID id,String format,String ext,String mime,byte[] data) {
  return ExportResult.builder().format(format).data(data).fileName("orientia_"+id+"."+ext).fileSize(data.length).mimeType(mime).build();
 }
 public ExportResult exportToPdf(UUID id){return result(id,"PDF","pdf","application/pdf",pdf(content(id)));}
 public ExportResult exportToCsv(UUID id){return result(id,"CSV","csv","text/csv;charset=UTF-8",csv(content(id)));}
 public ExportResult exportToExcel(UUID id){return result(id,"EXCEL","xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",xlsx(content(id)));}
}
