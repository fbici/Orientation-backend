package com.orientation.orientationapp.ai;
import com.orientation.orientationapp.ai.export.service.impl.DefaultExportService;
import com.orientation.orientationapp.ai.export.service.impl.DefaultExportService.Cell;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class DecisionExportTest {
 @Test void pdfIsParseablePaginatedAndContainsTheSimulationAndSource() throws Exception {
  var rows=new ArrayList<Cell>();rows.add(new Cell("type","SIMULATION HYPOTHETIQUE"));rows.add(new Cell("source","page 4; guide 2026; moyenne minimale: 12.5"));
  for(int i=0;i<110;i++)rows.add(new Cell("criterion."+i,"INCONNU"));
  try(var document=Loader.loadPDF(DefaultExportService.pdf(rows))) {
   assertTrue(document.getNumberOfPages()>=3);var text=new PDFTextStripper().getText(document);
   assertTrue(text.contains("SIMULATION HYPOTHETIQUE"));assertTrue(text.contains("12.5"));assertTrue(text.contains("criterion.109"));assertTrue(text.contains("NON OFFICIEL"));
  }
 }
 @Test void csvNeutralizesFormulasEvenAfterWhitespaceAndKeepsQuotes() {
  for(String text:List.of("=1+1","+SUM(1)","-1+2","@cmd","  =cmd","\tformula","\rcommand","\uFEFF=cmd"))
   assertTrue(DefaultExportService.safeCsvCell(text).startsWith("\"'"),text);
  assertEquals("\"a\"\"b,c\"",DefaultExportService.safeCsvCell("a\"b,c"));
  assertTrue(new String(DefaultExportService.csv(List.of(new Cell("note","Éligibilité"))),StandardCharsets.UTF_8).contains("Éligibilité"));
 }
 @Test void xlsxContainsActualStringCellsAndNeverFormulaCells() throws Exception {
  try(var wb=new XSSFWorkbook(new ByteArrayInputStream(DefaultExportService.xlsx(List.of(new Cell("source","=WEBSERVICE(\"evil\")")))))) {
   assertEquals(org.apache.poi.ss.usermodel.CellType.STRING,wb.getSheetAt(0).getRow(1).getCell(1).getCellType());
   assertEquals("=WEBSERVICE(\"evil\")",wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
  }
 }
}
