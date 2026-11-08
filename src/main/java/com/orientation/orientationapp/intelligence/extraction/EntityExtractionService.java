package com.orientation.orientationapp.intelligence.extraction;
import lombok.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.regex.Pattern;

/** Conservative extraction into source-backed drafts. No draft is an official rule. */
@Service
public class EntityExtractionService {
 public record FactDraft(String kind,int pageNumber,String passage,Map<String,Object> values) {}
 private String plain(String value) {
  return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);
 }
 public List<FactDraft> extractPage(String text,int page) {
  var result=new ArrayList<FactDraft>();
  for(String line:text.split("\\R")) {
   String passage=line.strip();
   if(passage.isEmpty() || passage.length()>10000)continue;
   String normalized=plain(passage);String first=passage.split(";",2)[0].strip();
   String name=first.contains(":")?first.substring(first.indexOf(':')+1).strip():first;
   if(normalized.matches("^(universite|university)\\b.*")) add(result,"UNIVERSITY",page,passage,map("name",name));
   else if(normalized.matches("^campus\\b.*")) add(result,"CAMPUS",page,passage,map("name",name));
   else if(normalized.matches("^(faculte|faculty|ecole|institut)\\b.*")) add(result,"FACULTY",page,passage,map("name",name));
   else if(normalized.matches("^(programme|program|filiere|formation|licence|license|master|doctorat|bts|dut)\\b.*")) {
    var data=map("name",name);
    String normalizedName=plain(name);
    if(normalizedName.startsWith("licence professionnelle"))data.put("type","LICENSE_PRO");
    else if(normalizedName.startsWith("licence") || normalizedName.startsWith("license"))data.put("type","LICENSE");
    else if(normalizedName.startsWith("master"))data.put("type","MASTER");
    else if(normalizedName.startsWith("doctorat"))data.put("type","DOCTORATE");
    else if(normalizedName.startsWith("bts"))data.put("type","BTS");
    else if(normalizedName.startsWith("dut"))data.put("type","DUT");
    var duration=Pattern.compile("(?:duree|duration)\\s*[:=-]?\\s*(\\d{1,2})\\s*(?:ans?|years?)\\b").matcher(normalized);
    if(duration.find())data.put("duration",Integer.valueOf(duration.group(1)));
    var language=Pattern.compile("(?:langue|language)\\s*[:=]\\s*([^;]+)",Pattern.CASE_INSENSITIVE).matcher(passage);
    if(language.find())data.put("language",language.group(1).strip());
    var field=Pattern.compile("(?:domaine|field)\\s*[:=]\\s*([^;]+)",Pattern.CASE_INSENSITIVE).matcher(passage);
    if(field.find())data.put("studyField",field.group(1).strip());
    add(result,"PROGRAM",page,passage,data);
   }
   if(normalized.matches("^(bourse|scholarship|aide financiere|allocation)\\b.*")) {
    var data=map("name",name);amount(passage,data);add(result,"SCHOLARSHIP",page,passage,data);
   }
   var minimum=Pattern.compile("(?:moyenne\\s+(?:minimale|minimum)|minimum\\s+(?:de\\s+)?moyenne|average\\s+minimum)\\s*[:>=-]*\\s*(\\d{1,2}(?:[.,]\\d{1,2})?)").matcher(normalized);
   if(minimum.find())add(result,"ADMISSION_CRITERION",page,passage,map("criterionType","MIN_AVERAGE","operator","GREATER_THAN_OR_EQUAL","minValue",new BigDecimal(minimum.group(1).replace(',','.')),"description",passage));
   if(normalized.matches("^(pays|country)\\s*[:=].*"))add(result,"COUNTRY",page,passage,map("name",name));
   var year=Pattern.compile("\\b((?:19|20)\\d{2})\\s*[-/]\\s*((?:19|20)\\d{2})\\b").matcher(passage);
   if(year.find() && normalized.matches(".*(annee|academ|year|guide).*"))add(result,"ACADEMIC_YEAR",page,passage,map("label",year.group(1)+"-"+year.group(2)));
   if(normalized.matches(".*\\b(bac|baccalaureat|serie)\\b.*(requis|exige|accepte|:).*"))add(result,"BAC_REQUIREMENT",page,passage,map("description",passage));
   if(normalized.matches(".*\\b(diplome|degree)\\b.*(requis|exige|:).*"))add(result,"DIPLOMA_REQUIREMENT",page,passage,map("description",passage));
   if(normalized.matches(".*\\b(matieres?|subjects?)\\b.*(obligatoire|requis|exige|:).*"))add(result,"SUBJECT_REQUIREMENT",page,passage,map("description",passage));
   if(normalized.matches(".*\\b(documents?|pieces?)\\b.*(exiges?|requis|fournir|:).*"))add(result,"DOCUMENT_REQUIREMENT",page,passage,map("description",passage));
   if(normalized.matches("^(frais|tuition|scolarite|inscription)\\b.*")) {
    var data=map("description",passage);amount(passage,data);add(result,"FEE",page,passage,data);
   }
   if(normalized.matches(".*\\b(date limite|deadline|candidature|rentree)\\b.*")) {
    var data=map("description",passage);
    var iso=Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b").matcher(passage);
    if(iso.find()) {try {data.put("date",java.time.LocalDate.parse(iso.group(1)).toString());}catch(java.time.DateTimeException ignored){}}
    add(result,"DATE",page,passage,data);
   }
   if(normalized.matches("^(langue|language|domaine|field|diplome|degree|niveau|level|duree|duration)\\s*[:=].*"))
    add(result,"ACADEMIC_ATTRIBUTE",page,passage,map("description",passage));
  }
  return result;
 }
 private void amount(String text,Map<String,Object> data) {
  var m=Pattern.compile("(\\d+(?:[ .,]\\d{3})*(?:[.,]\\d{1,2})?)\\s*(EUR|USD|XOF|XAF|FCFA)\\b",Pattern.CASE_INSENSITIVE).matcher(text);
  if(m.find()) {
   String number=m.group(1).replace(" ","");
   // Ambiguous separators remain in the passage for human review.
   if(number.matches("\\d+(?:[.,]\\d{1,2})?")) {
    data.put("amount",new BigDecimal(number.replace(',','.')));
    if(!m.group(2).equalsIgnoreCase("FCFA"))data.put("currency",m.group(2).toUpperCase(Locale.ROOT));
   }
  }
 }
 private Map<String,Object> map(Object... entries) {
  var result=new LinkedHashMap<String,Object>();for(int i=0;i<entries.length;i+=2)result.put((String)entries[i],entries[i+1]);return result;
 }
 private void add(List<FactDraft> facts,String kind,int page,String passage,Map<String,Object> values) {
  facts.add(new FactDraft(kind,page,passage,values));
 }
 public ExtractedEntities extract(String text,String documentType) {
  var entities=new ExtractedEntities();entities.setRawText(text);entities.setDocumentType(documentType);
  for(var fact:extractPage(text,1)) {
   if(fact.kind().equals("UNIVERSITY")){var u=new ExtractedUniversity();u.setName((String)fact.values().get("name"));entities.getUniversities().add(u);}
   if(fact.kind().equals("PROGRAM")){var p=new ExtractedProgram();p.setName((String)fact.values().get("name"));p.setType((String)fact.values().get("type"));entities.getPrograms().add(p);}
   if(fact.kind().equals("SCHOLARSHIP")){var s=new ExtractedScholarship();s.setName((String)fact.values().get("name"));s.setAmount((BigDecimal)fact.values().get("amount"));s.setCurrency((String)fact.values().get("currency"));entities.getScholarships().add(s);}
   if(fact.kind().equals("ADMISSION_CRITERION")){var c=new ExtractedCriterion();c.setType((String)fact.values().get("criterionType"));c.setOperator((String)fact.values().get("operator"));c.setMinValue((BigDecimal)fact.values().get("minValue"));entities.getCriteria().add(c);}
  }
  return entities;
 }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedEntities {
        private String rawText;
        private String documentType;
        @Builder.Default private List<ExtractedUniversity> universities = new ArrayList<>();
        @Builder.Default private List<ExtractedProgram> programs = new ArrayList<>();
        @Builder.Default private List<ExtractedCriterion> criteria = new ArrayList<>();
        @Builder.Default private List<ExtractedScholarship> scholarships = new ArrayList<>();
        @Builder.Default private List<ExtractedSubject> subjects = new ArrayList<>();
        @Builder.Default private List<ExtractedFee> fees = new ArrayList<>();
        @Builder.Default private List<ExtractedDeadline> deadlines = new ArrayList<>();
        @Builder.Default private List<String> languages = new ArrayList<>();
        @Builder.Default private List<String> conditions = new ArrayList<>();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedUniversity {
        private String name;
        private String country;
        private String city;
        private String website;
        private String ranking;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedProgram {
        private String name;
        private String type;
        private String duration;
        private String language;
        private String university;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedCriterion {
        private String type;
        private String operator;
        private java.math.BigDecimal minValue;
        private java.math.BigDecimal maxValue;
        private Boolean mandatory;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedScholarship {
        private String name;
        private java.math.BigDecimal amount;
        private String currency;
        private String criteria;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedSubject {
        private String name;
        private BigDecimal coefficient;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedFee {
        private String type;
        private BigDecimal amount;
        private String currency;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedDeadline {
        private String date;
        private String description;
    }
}
