package com.orientation.orientationapp.modules.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/benin/orientation")
@Tag(name = "Benin Orientation", description = "Orientation universitaire specifique au Benin")
public class BeninOrientationController {

    // Helper pour creer des maps avec plus de 10 entrees
    private static Map<String, Object> filiere(String code, String name, String faculty, String university,
            int duration, List<String> series, int bourse, int fpp, int fep,
            double maths, double physique, double svt, double francais, double total) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code); m.put("name", name); m.put("faculty", faculty); m.put("university", university);
        m.put("duration", duration); m.put("series", series); m.put("bourse", bourse); m.put("fpp", fpp);
        m.put("fep", fep); m.put("maths", maths); m.put("physique", physique); m.put("svt", svt);
        m.put("francais", francais); m.put("total", total);
        return m;
    }

    private static final List<Map<String, Object>> FILIERES = List.of(
        filiere("MED", "Medecine Generale", "FSS", "UAC", 7, List.of("C","D"), 15, 30, 55, 14.0, 13.0, 14.0, 10.0, 13.5),
        filiere("PHAR", "Pharmacie", "FSS", "UAC", 6, List.of("C","D"), 15, 30, 55, 12.0, 12.0, 13.0, 10.0, 12.5),
        filiere("GCIV", "Genie Civil", "EPAC", "UAC", 5, List.of("C"), 10, 25, 65, 14.0, 13.0, 8.0, 8.0, 13.0),
        filiere("GINF", "Genie Informatique", "EPAC", "UAC", 5, List.of("C","D"), 10, 25, 65, 13.0, 12.0, 8.0, 8.0, 12.0),
        filiere("GELC", "Genie Electrique", "EPAC", "UAC", 5, List.of("C"), 10, 25, 65, 13.0, 13.0, 8.0, 8.0, 12.5),
        filiere("GLOG", "Genie Logiciel", "IFRI", "UAC", 3, List.of("C","D"), 15, 30, 55, 12.0, 10.0, 8.0, 8.0, 11.0),
        filiere("RTEL", "Reseaux et Telecom", "IFRI", "UAC", 3, List.of("C","D"), 15, 30, 55, 11.0, 10.0, 8.0, 8.0, 10.5),
        filiere("MATH", "Mathematiques", "FAST", "UAC", 3, List.of("C"), 20, 30, 50, 14.0, 11.0, 8.0, 8.0, 12.0),
        filiere("PHYS", "Physique", "FAST", "UAC", 3, List.of("C"), 20, 30, 50, 12.0, 14.0, 8.0, 8.0, 12.0),
        filiere("CHIM", "Chimie", "FAST", "UAC", 3, List.of("C","D"), 20, 30, 50, 11.0, 11.0, 10.0, 8.0, 10.5),
        filiere("BIOL", "Biologie", "FAST", "UAC", 3, List.of("D"), 20, 30, 50, 10.0, 10.0, 14.0, 8.0, 11.0),
        filiere("ECON", "Economie", "FASEG", "UAC", 3, List.of("A","B","C","D"), 15, 30, 55, 10.0, 8.0, 8.0, 12.0, 10.0),
        filiere("GEST", "Gestion", "FASEG", "UAC", 3, List.of("A","B","G2"), 15, 30, 55, 10.0, 8.0, 8.0, 12.0, 10.0),
        filiere("DPRV", "Droit Prive", "FADESP", "UAC", 3, List.of("A","B"), 10, 25, 65, 8.0, 8.0, 8.0, 14.0, 11.0),
        filiere("DPUB", "Droit Public", "FADESP", "UAC", 3, List.of("A","B"), 10, 25, 65, 8.0, 8.0, 8.0, 14.0, 11.0),
        filiere("SPOL", "Science Politique", "FADESP", "UAC", 3, List.of("A","B"), 15, 30, 55, 8.0, 8.0, 8.0, 13.0, 10.5),
        filiere("LETT", "Lettres Modernes", "FLASH", "UAC", 3, List.of("A"), 20, 30, 50, 8.0, 8.0, 8.0, 15.0, 11.0),
        filiere("PHIL", "Philosophie", "FLASH", "UAC", 3, List.of("A"), 25, 30, 45, 8.0, 8.0, 8.0, 14.0, 10.5),
        filiere("SOCIO", "Sociologie", "FLASH", "UAC", 3, List.of("A","B"), 20, 30, 50, 8.0, 8.0, 8.0, 13.0, 10.0),
        filiere("LANG", "Langues Etrangeres", "FLASH", "UAC", 3, List.of("A"), 20, 30, 50, 8.0, 8.0, 8.0, 14.0, 10.5),
        filiere("AGRO", "Agronomie", "UP", "Universite de Parakou", 3, List.of("C","D"), 25, 30, 45, 10.0, 10.0, 13.0, 8.0, 10.5),
        filiere("MEDUP", "Medecine (Parakou)", "FSS", "Universite de Parakou", 7, List.of("C","D"), 15, 30, 55, 13.0, 12.0, 13.0, 10.0, 12.5),
        filiere("INFO", "Informatique", "UNSTIM", "UNSTIM", 3, List.of("C","D"), 20, 30, 50, 12.0, 11.0, 8.0, 8.0, 11.0),
        filiere("TCOM", "Techniques de Communication", "UNSTIM", "UNSTIM", 3, List.of("A","B","C"), 20, 30, 50, 10.0, 8.0, 8.0, 13.0, 10.0)
    );

    @Operation(summary = "Liste de toutes les filieres du Benin avec criteres")
    @GetMapping("/filieres")
    public ResponseEntity<List<Map<String, Object>>> getFilieres(
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String university) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var f : FILIERES) {
            boolean matchSerie = serie == null || ((List<?>) f.get("series")).contains(serie.toUpperCase());
            boolean matchUni = university == null || f.get("university").toString().toLowerCase().contains(university.toLowerCase());
            if (matchSerie && matchUni) result.add(f);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Simulation d'orientation - entrez vos notes")
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulate(@RequestBody Map<String, Object> request) {
        String serie = (String) request.getOrDefault("serie", "");
        double maths = toDouble(request.getOrDefault("maths", 0));
        double physique = toDouble(request.getOrDefault("physique", 0));
        double svt = toDouble(request.getOrDefault("svt", 0));
        double francais = toDouble(request.getOrDefault("francais", 0));

        List<Map<String, Object>> eligible = new ArrayList<>();
        List<Map<String, Object>> risky = new ArrayList<>();

        for (var f : FILIERES) {
            List<?> series = (List<?>) f.get("series");
            if (!series.contains(serie.toUpperCase())) continue;

            double seuilMaths = toDouble(f.get("maths"));
            double seuilPhysique = toDouble(f.get("physique"));
            double seuilSvt = toDouble(f.get("svt"));
            double seuilFrancais = toDouble(f.get("francais"));
            double seuilTotal = toDouble(f.get("total"));

            double moyennePonderee = calculerMoyennePonderee(maths, physique, svt, francais, f);
            Map<String, Object> entry = new HashMap<>(f);
            entry.put("moyennePonderee", BigDecimal.valueOf(moyennePonderee).setScale(2, RoundingMode.HALF_UP).doubleValue());

            boolean mathsOk = maths >= seuilMaths;
            boolean physiqueOk = physique >= seuilPhysique || serie.equals("A") || serie.equals("B");
            boolean svtOk = svt >= seuilSvt || serie.equals("A") || serie.equals("B");
            boolean francaisOk = francais >= seuilFrancais;
            boolean totalOk = moyennePonderee >= seuilTotal;

            if (mathsOk && physiqueOk && svtOk && francaisOk && totalOk) {
                entry.put("status", "ELIGIBLE");
                entry.put("financement", getFinancement(moyennePonderee, f));
                eligible.add(entry);
            } else if (moyennePonderee >= seuilTotal - 2.0) {
                entry.put("status", "RISQUE");
                entry.put("financement", "FEP");
                risky.add(entry);
            }
        }

        eligible.sort((a, b) -> Double.compare(toDouble(b.get("moyennePonderee")), toDouble(a.get("moyennePonderee"))));
        risky.sort((a, b) -> Double.compare(toDouble(b.get("moyennePonderee")), toDouble(a.get("moyennePonderee"))));

        Map<String, Object> notes = new HashMap<>();
        notes.put("maths", maths); notes.put("physique", physique); notes.put("svt", svt); notes.put("francais", francais);

        Map<String, Object> response = new HashMap<>();
        response.put("serie", serie);
        response.put("notes", notes);
        response.put("eligible", eligible);
        response.put("risky", risky);
        response.put("nbEligible", eligible.size());
        response.put("nbRisky", risky.size());
        response.put("conseil", genererConseil(serie, eligible.size()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Fiches detaillees des universites du Benin")
    @GetMapping("/universites")
    public ResponseEntity<List<Map<String, Object>>> getUniversites() {
        List<Map<String, Object>> universites = new ArrayList<>();
        universites.add(buildUni("Universite d'Abomey-Calavi (UAC)", "UAC", "Abomey-Calavi", "Publique", 8, "La plus grande universite du Benin. 8 facultes, 50+ filieres.", "www.uac.bj", List.of("FSS","EPAC","FAST","FASEG","FADESP","FLASH","IFRI","ENEAM")));
        universites.add(buildUni("Universite de Parakou (UP)", "UP", "Parakou", "Publique", 4, "Universite du nord. Medecine, agronomie, droit.", "www.up.bj", List.of("FSS","FLSH","FAST","FASEG")));
        universites.add(buildUni("UNSTIM", "UNSTIM", "Abomey", "Publique", 3, "Sciences, technologies, ingenierie et mathematiques.", "www.unstim.bj", List.of("FAST","IUT","FLSH")));
        universites.add(buildUni("Universite Nationale d'Agriculture", "UNA", "Ketou", "Publique", 3, "Agronomie et developpement rural.", "www.una.bj", List.of("FAST","FASEP","IUT")));
        universites.add(buildUni("IUT de Lokossa", "IUT-L", "Lokossa", "Publique", 3, "Formations professionnalisantes.", "www.iut-lokossa.bj", List.of("TC","GE","INFO")));
        return ResponseEntity.ok(universites);
    }

    @Operation(summary = "Informations sur les series du bac")
    @GetMapping("/series")
    public ResponseEntity<List<Map<String, Object>>> getSeries() {
        List<Map<String, Object>> series = new ArrayList<>();
        series.add(buildSerie("C", "Serie C (Scientifique)", "Ouvre le plus de portes : medecine, ingenierie, sciences", List.of("Maths","Physique","SVT","Francais")));
        series.add(buildSerie("D", "Serie D (Sciences Naturelles)", "Medecine, pharmacie, agronomie, biologie", List.of("Maths","Physique","SVT","Francais")));
        series.add(buildSerie("A", "Serie A (Litteraire)", "Droit, lettres, philosophie, sociologie, langues", List.of("Francais","Philosophie","Langues","Histoire-Geo")));
        series.add(buildSerie("B", "Serie B (Economique)", "Economie, gestion, droit des affaires", List.of("Maths","Economie","Francais","Langues")));
        series.add(buildSerie("G2", "Serie G2 (Gestion)", "Comptabilite, techniques commerciales, management", List.of("Maths","Gestion","Economie","Droit")));
        return ResponseEntity.ok(series);
    }

    private Map<String, Object> buildUni(String nom, String acro, String ville, String type, int nb, String desc, String site, List<String> fac) {
        Map<String, Object> m = new HashMap<>();
        m.put("nom", nom); m.put("acronyme", acro); m.put("ville", ville); m.put("type", type);
        m.put("nbFacultes", nb); m.put("description", desc); m.put("site", site); m.put("facultes", fac);
        return m;
    }

    private Map<String, Object> buildSerie(String code, String nom, String desc, List<String> mat) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code); m.put("nom", nom); m.put("description", desc); m.put("matieres", mat);
        return m;
    }

    private double calculerMoyennePonderee(double maths, double physique, double svt, double francais, Map<String, Object> f) {
        double cm = Math.max(toDouble(f.get("maths")), 1);
        double cp = Math.max(toDouble(f.get("physique")), 1);
        double cs = Math.max(toDouble(f.get("svt")), 1);
        double cf = Math.max(toDouble(f.get("francais")), 1);
        return (maths * cm + physique * cp + svt * cs + francais * cf) / (cm + cp + cs + cf);
    }

    private String getFinancement(double moyenne, Map<String, Object> f) {
        double seuil = toDouble(f.get("total"));
        if (moyenne >= seuil + 2.0) return "BOURSE";
        if (moyenne >= seuil) return "FPP";
        return "FEP";
    }

    private String genererConseil(String serie, int nb) {
        if (nb == 0) return "Aucune filiere trouvee. Essayez le FEP ou changez de serie.";
        if (nb <= 3) return "Peu de filieres. Concentrez-vous sur celles-ci et envisagez le FPP.";
        return "Bon profil ! " + nb + " filieres accessibles. Priorisez celles avec bourse.";
    }

    private double toDouble(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0; }
    }
}
