package com.orientation.orientationapp.intelligence.extraction;

import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service d'extraction d'entités à partir de texte OCR.
 *
 * Extrait automatiquement :
 * - Universités (noms, pays, villes)
 * - Programmes (noms, types, durée, langue)
 * - Critères d'admission (notes, matières, documents)
 * - Bourses (nom, montant, critères)
 * - Matières (noms, coefficients)
 * - Frais (inscription, scolarité)
 * - Calendriers (dates limites, rentrées)
 * - Conditions (langue, visa, logement)
 */
@Service
public class EntityExtractionService {

    /**
     * Extrait toutes les entités d'un texte donné.
     *
     * @param text texte extrait par OCR
     * @param documentType type de document (guide, règlement, brochure, etc.)
     * @return entités extraites structurées
     */
    public ExtractedEntities extract(String text, String documentType) {
        ExtractedEntities entities = new ExtractedEntities();
        entities.setRawText(text);
        entities.setDocumentType(documentType);

        // Extraction des universités
        entities.setUniversities(extractUniversities(text));

        // Extraction des programmes
        entities.setPrograms(extractPrograms(text));

        // Extraction des critères d'admission
        entities.setCriteria(extractCriteria(text));

        // Extraction des bourses
        entities.setScholarships(extractScholarships(text));

        // Extraction des matières
        entities.setSubjects(extractSubjects(text));

        // Extraction des frais
        entities.setFees(extractFees(text));

        // Extraction des dates
        entities.setDeadlines(extractDeadlines(text));

        // Extraction des langues
        entities.setLanguages(extractLanguages(text));

        // Extraction des conditions
        entities.setConditions(extractConditions(text));

        return entities;
    }

    // --- Extraction des universités ---
    private List<ExtractedUniversity> extractUniversities(String text) {
        List<ExtractedUniversity> universities = new ArrayList<>();
        // Patterns courants pour les universités
        String[] patterns = {
            "universit[ée]", "facult[ée]", "école", "institut", "iut",
            "grandes? \\u00e9coles?", "polytechnique", "conservatoire"
        };

        // Recherche de noms d'universités dans le texte
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String lower = line.toLowerCase().trim();
            for (String pattern : patterns) {
                if (lower.matches(".*" + pattern + ".*") && line.trim().length() > 10) {
                    ExtractedUniversity uni = new ExtractedUniversity();
                    uni.setName(cleanName(line));
                    uni.setCountry(extractCountryFromContext(line, text));
                    uni.setCity(extractCityFromContext(line, text));
                    universities.add(uni);
                    break;
                }
            }
        }
        return universities;
    }

    // --- Extraction des programmes ---
    private List<ExtractedProgram> extractPrograms(String text) {
        List<ExtractedProgram> programs = new ArrayList<>();
        String[] programKeywords = {
            "licence", "master", "doctorat", "ingénieur", "mastère",
            "bts", "dut", "bachelor", "phd", "mba", "formation",
            "diplôme", "certificat", "spécialisation"
        };

        String[] lines = text.split("\\n");
        for (String line : lines) {
            String lower = line.toLowerCase().trim();
            for (String keyword : programKeywords) {
                if (lower.contains(keyword) && line.trim().length() > 5) {
                    ExtractedProgram prog = new ExtractedProgram();
                    prog.setName(cleanName(line));
                    prog.setType(extractProgramType(lower));
                    prog.setDuration(extractDuration(line));
                    prog.setLanguage(extractLanguageFromLine(line));
                    programs.add(prog);
                    break;
                }
            }
        }
        return programs;
    }

    // --- Extraction des critères d'admission ---
    private List<ExtractedCriterion> extractCriteria(String text) {
        List<ExtractedCriterion> criteria = new ArrayList<>();

        // Recherche de notes minimales
        String[] notePatterns = {"moyenne.*([0-9]+[.,][0-9]+)", "note.*([0-9]+[.,][0-9]+)",
                "minimum.*([0-9]+[.,][0-9]+)", "seuil.*([0-9]+[.,][0-9]+)"};

        for (String pattern : notePatterns) {
            var matcher = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
            while (matcher.find()) {
                ExtractedCriterion crit = new ExtractedCriterion();
                crit.setType("MIN_AVERAGE");
                crit.setOperator("GREATER_THAN_OR_EQUAL");
                crit.setMinValue(new BigDecimal(matcher.group(1).replace(",", ".")));
                crit.setMandatory(true);
                criteria.add(crit);
            }
        }

        // Recherche de types de bac
        if (text.toLowerCase().contains("bac") || text.toLowerCase().contains("baccalauréat")) {
            ExtractedCriterion crit = new ExtractedCriterion();
            crit.setType("BAC_TYPE");
            crit.setOperator("IN_LIST");
            crit.setMandatory(true);
            criteria.add(crit);
        }

        return criteria;
    }

    // --- Extraction des bourses ---
    private List<ExtractedScholarship> extractScholarships(String text) {
        List<ExtractedScholarship> scholarships = new ArrayList<>();
        String[] scholarshipKeywords = {"bourse", "scholarship", "aide financière", "allocation", "be"};

        String[] lines = text.split("\\n");
        for (String line : lines) {
            String lower = line.toLowerCase().trim();
            for (String keyword : scholarshipKeywords) {
                if (lower.contains(keyword) && line.trim().length() > 5) {
                    ExtractedScholarship sch = new ExtractedScholarship();
                    sch.setName(cleanName(line));
                    sch.setAmount(extractAmount(line));
                    scholarships.add(sch);
                    break;
                }
            }
        }
        return scholarships;
    }

    // --- Extraction des matières ---
    private List<ExtractedSubject> extractSubjects(String text) {
        List<ExtractedSubject> subjects = new ArrayList<>();
        String[] subjectNames = {
            "mathématiques", "maths", "physique", "chimie", "svt", "sciences",
            "français", "anglais", "arabe", "philosophie", "histoire", "géographie",
            "informatique", "économie", "droit", "biologie", "géologie"
        };

        for (String name : subjectNames) {
            if (text.toLowerCase().contains(name)) {
                ExtractedSubject sub = new ExtractedSubject();
                sub.setName(capitalize(name));
                subjects.add(sub);
            }
        }
        return subjects;
    }

    // --- Extraction des frais ---
    private List<ExtractedFee> extractFees(String text) {
        List<ExtractedFee> fees = new ArrayList<>();
        String[] feePatterns = {
            "frais.*([0-9]+[.,]?[0-9]*)\\s*(€|euros?|EUR|FCFA|XOF|XAF|\\$|USD)",
            "inscription.*([0-9]+[.,]?[0-9]*)\\s*(€|euros?|EUR|FCFA|XOF|XAF|\\$|USD)",
            "scolarit[ée].*([0-9]+[.,]?[0-9]*)\\s*(€|euros?|EUR|FCFA|XOF|XAF|\\$|USD)"
        };

        for (String pattern : feePatterns) {
            var matcher = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
            while (matcher.find()) {
                ExtractedFee fee = new ExtractedFee();
                fee.setAmount(new BigDecimal(matcher.group(1).replace(",", ".")));
                fee.setCurrency(matcher.group(2));
                fee.setType(pattern.contains("inscription") ? "INSCRIPTION" : "TUITION");
                fees.add(fee);
            }
        }
        return fees;
    }

    // --- Extraction des dates limites ---
    private List<ExtractedDeadline> extractDeadlines(String text) {
        List<ExtractedDeadline> deadlines = new ArrayList<>();
        String[] datePatterns = {
            "([0-9]{1,2})[/\\-\\.]([0-9]{1,2})[/\\-\\.]([0-9]{2,4})",
            "(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)\\s+([0-9]{4})"
        };

        for (String pattern : datePatterns) {
            var matcher = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
            while (matcher.find()) {
                ExtractedDeadline deadline = new ExtractedDeadline();
                deadline.setDate(matcher.group());
                deadlines.add(deadline);
            }
        }
        return deadlines;
    }

    // --- Extraction des langues ---
    private List<String> extractLanguages(String text) {
        List<String> languages = new ArrayList<>();
        String[] langNames = {"français", "anglais", "arabe", "espagnol", "allemand", "portugais", "chinois"};
        for (String lang : langNames) {
            if (text.toLowerCase().contains(lang)) {
                languages.add(capitalize(lang));
            }
        }
        return languages;
    }

    // --- Extraction des conditions ---
    private List<String> extractConditions(String text) {
        List<String> conditions = new ArrayList<>();
        String[] keywords = {"visa", "logement", "assurance", "séjour", "hébergement", "transport"};
        for (String keyword : keywords) {
            if (text.toLowerCase().contains(keyword)) {
                conditions.add(capitalize(keyword));
            }
        }
        return conditions;
    }

    // --- Helpers ---
    private String cleanName(String line) {
        return line.trim().replaceAll("\\s+", " ").substring(0, Math.min(line.trim().length(), 200));
    }

    private String extractCountryFromContext(String line, String fullText) {
        String[] countries = {"France", "Maroc", "Sénégal", "Côte d'Ivoire", "Tunisie", "Canada", "Belgique", "Suisse"};
        for (String country : countries) {
            if (line.contains(country) || fullText.contains(country)) return country;
        }
        return null;
    }

    private String extractCityFromContext(String line, String fullText) {
        String[] cities = {"Paris", "Lyon", "Marseille", "Casablanca", "Rabat", "Dakar", "Abidjan", "Tunis", "Montréal", "Bruxelles"};
        for (String city : cities) {
            if (line.contains(city)) return city;
        }
        return null;
    }

    private String extractProgramType(String text) {
        if (text.contains("licence") || text.contains("bachelor")) return "LICENCE";
        if (text.contains("master") || text.contains("mastère")) return "MASTER";
        if (text.contains("doctorat") || text.contains("phd")) return "DOCTORAT";
        if (text.contains("ingénieur")) return "INGENIEUR";
        if (text.contains("bts") || text.contains("dut")) return "BTS_DUT";
        return "OTHER";
    }

    private String extractDuration(String text) {
        var matcher = java.util.regex.Pattern.compile("([0-9]+)\\s*an", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
        if (matcher.find()) return matcher.group(1) + " ans";
        return null;
    }

    private String extractLanguageFromLine(String text) {
        if (text.toLowerCase().contains("anglais") || text.toLowerCase().contains("english")) return "Anglais";
        if (text.toLowerCase().contains("français") || text.toLowerCase().contains("french")) return "Français";
        if (text.toLowerCase().contains("arabe")) return "Arabe";
        return null;
    }

    private BigDecimal extractAmount(String text) {
        var matcher = java.util.regex.Pattern.compile("([0-9]+[.,]?[0-9]*)\\s*(€|euros?|EUR|FCFA)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
        if (matcher.find()) return new BigDecimal(matcher.group(1).replace(",", "."));
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // --- DTOs ---

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExtractedEntities {
        private String rawText;
        private String documentType;
        private List<ExtractedUniversity> universities = new ArrayList<>();
        private List<ExtractedProgram> programs = new ArrayList<>();
        private List<ExtractedCriterion> criteria = new ArrayList<>();
        private List<ExtractedScholarship> scholarships = new ArrayList<>();
        private List<ExtractedSubject> subjects = new ArrayList<>();
        private List<ExtractedFee> fees = new ArrayList<>();
        private List<ExtractedDeadline> deadlines = new ArrayList<>();
        private List<String> languages = new ArrayList<>();
        private List<String> conditions = new ArrayList<>();
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
        private boolean mandatory;
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
