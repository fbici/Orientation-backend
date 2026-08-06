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

    // Donnees officielles des filieres du Benin
    private static final List<Map<String, Object>> FILIERES = List.of(
        Map.of("code", "MED", "name", "Medecine Generale", "faculty", "FSS", "university", "UAC", "duration", 7, "series", List.of("C", "D"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 14.0, "physique", 13.0, "svt", 14.0, "francais", 10.0, "total", 13.5),
        Map.of("code", "PHAR", "name", "Pharmacie", "faculty", "FSS", "university", "UAC", "duration", 6, "series", List.of("C", "D"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 12.0, "physique", 12.0, "svt", 13.0, "francais", 10.0, "total", 12.5),
        Map.of("code", "GCIV", "name", "Genie Civil", "faculty", "EPAC", "university", "UAC", "duration", 5, "series", List.of("C"), "bourse", 10, "fpp", 25, "fep", 65, "maths", 14.0, "physique", 13.0, "svt", 8.0, "francais", 8.0, "total", 13.0),
        Map.of("code", "GINF", "name", "Genie Informatique", "faculty", "EPAC", "university", "UAC", "duration", 5, "series", List.of("C", "D"), "bourse", 10, "fpp", 25, "fep", 65, "maths", 13.0, "physique", 12.0, "svt", 8.0, "francais", 8.0, "total", 12.0),
        Map.of("code", "GELC", "name", "Genie Electrique", "faculty", "EPAC", "university", "UAC", "duration", 5, "series", List.of("C"), "bourse", 10, "fpp", 25, "fep", 65, "maths", 13.0, "physique", 13.0, "svt", 8.0, "francais", 8.0, "total", 12.5),
        Map.of("code", "GLOG", "name", "Genie Logiciel", "faculty", "IFRI", "university", "UAC", "duration", 3, "series", List.of("C", "D"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 12.0, "physique", 10.0, "svt", 8.0, "francais", 8.0, "total", 11.0),
        Map.of("code", "RTEL", "name", "Reseaux et Telecom", "faculty", "IFRI", "university", "UAC", "duration", 3, "series", List.of("C", "D"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 11.0, "physique", 10.0, "svt", 8.0, "francais", 8.0, "total", 10.5),
        Map.of("code", "MATH", "name", "Mathematiques", "faculty", "FAST", "university", "UAC", "duration", 3, "series", List.of("C"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 14.0, "physique", 11.0, "svt", 8.0, "francais", 8.0, "total", 12.0),
        Map.of("code", "PHYS", "name", "Physique", "faculty", "FAST", "university", "UAC", "duration", 3, "series", List.of("C"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 12.0, "physique", 14.0, "svt", 8.0, "francais", 8.0, "total", 12.0),
        Map.of("code", "CHIM", "name", "Chimie", "faculty", "FAST", "university", "UAC", "duration", 3, "series", List.of("C", "D"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 11.0, "physique", 11.0, "svt", 10.0, "francais", 8.0, "total", 10.5),
        Map.of("code", "BIOL", "name", "Biologie", "faculty", "FAST", "university", "UAC", "duration", 3, "series", List.of("D"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 10.0, "physique", 10.0, "svt", 14.0, "francais", 8.0, "total", 11.0),
        Map.of("code", "ECON", "name", "Economie", "faculty", "FASEG", "university", "UAC", "duration", 3, "series", List.of("A", "B", "C", "D"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 10.0, "physique", 8.0, "svt", 8.0, "francais", 12.0, "total", 10.0),
        Map.of("code", "GEST", "name", "Gestion", "faculty", "FASEG", "university", "UAC", "duration", 3, "series", List.of("A", "B", "G2"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 10.0, "physique", 8.0, "svt", 8.0, "francais", 12.0, "total", 10.0),
        Map.of("code", "DPRV", "name", "Droit Prive", "faculty", "FADESP", "university", "UAC", "duration", 3, "series", List.of("A", "B"), "bourse", 10, "fpp", 25, "fep", 65, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 14.0, "total", 11.0),
        Map.of("code", "DPUB", "name", "Droit Public", "faculty", "FADESP", "university", "UAC", "duration", 3, "series", List.of("A", "B"), "bourse", 10, "fpp", 25, "fep", 65, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 14.0, "total", 11.0),
        Map.of("code", "SPOL", "name", "Science Politique", "faculty", "FADESP", "university", "UAC", "duration", 3, "series", List.of("A", "B"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 13.0, "total", 10.5),
        Map.of("code", "LETT", "name", "Lettres Modernes", "faculty", "FLASH", "university", "UAC", "duration", 3, "series", List.of("A"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 15.0, "total", 11.0),
        Map.of("code", "PHIL", "name", "Philosophie", "faculty", "FLASH", "university", "UAC", "duration", 3, "series", List.of("A"), "bourse", 25, "fpp", 30, "fep", 45, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 14.0, "total", 10.5),
        Map.of("code", "SOCIO", "name", "Sociologie", "faculty", "FLASH", "university", "UAC", "duration", 3, "series", List.of("A", "B"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 13.0, "total", 10.0),
        Map.of("code", "LANG", "name", "Langues Etrangeres", "faculty", "FLASH", "university", "UAC", "duration", 3, "series", List.of("A"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 8.0, "physique", 8.0, "svt", 8.0, "francais", 14.0, "total", 10.5),
        Map.of("code", "AGRO", "name", "Agronomie", "faculty", "UP", "university", "Universite de Parakou", "duration", 3, "series", List.of("C", "D"), "bourse", 25, "fpp", 30, "fep", 45, "maths", 10.0, "physique", 10.0, "svt", 13.0, "francais", 8.0, "total", 10.5),
        Map.of("code", "MEDUP", "name", "Medecine (Parakou)", "faculty", "FSS", "university", "Universite de Parakou", "duration", 7, "series", List.of("C", "D"), "bourse", 15, "fpp", 30, "fep", 55, "maths", 13.0, "physique", 12.0, "svt", 13.0, "francais", 10.0, "total", 12.5),
        Map.of("code", "INFO", "name", "Informatique", "faculty", "UNSTIM", "university", "UNSTIM", "duration", 3, "series", List.of("C", "D"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 12.0, "physique", 11.0, "svt", 8.0, "francais", 8.0, "total", 11.0),
        Map.of("code", "TCOM", "name", "Techniques de Communication", "faculty", "UNSTIM", "university", "UNSTIM", "duration", 3, "series", List.of("A", "B", "C"), "bourse", 20, "fpp", 30, "fep", 50, "maths", 10.0, "physique", 8.0, "svt", 8.0, "francais", 13.0, "total", 10.0)
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
            if (matchSerie && matchUni) {
                result.add(f);
            }
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
        double moyenne = toDouble(request.getOrDefault("moyenne", 0));

        List<Map<String, Object>> eligible = new ArrayList<>();
        List<Map<String, Object>> risky = new ArrayList<>();
        List<Map<String, Object>> notEligible = new ArrayList<>();

        for (var f : FILIERES) {
            List<?> series = (List<?>) f.get("series");
            if (!series.contains(serie.toUpperCase())) continue;

            double seuilMaths = toDouble(f.get("maths"));
            double seuilPhysique = toDouble(f.get("physique"));
            double seuilSvt = toDouble(f.get("svt"));
            double seuilFrancais = toDouble(f.get("francais"));
            double seuilTotal = toDouble(f.get("total"));

            // Calculer la moyenne ponderee
            double moyennePonderee = calculerMoyennePonderee(maths, physique, svt, francais, f);
            
            Map<String, Object> entry = new HashMap<>(f);
            entry.put("moyennePonderee", BigDecimal.valueOf(moyennePonderee).setScale(2, RoundingMode.HALF_UP).doubleValue());
            entry.put("seuilBourse", seuilTotal + 2.0);
            entry.put("seuilFpp", seuilTotal);
            entry.put("seuilFep", seuilTotal - 2.0);

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
            } else {
                entry.put("status", "NON_ELIGIBLE");
                notEligible.add(entry);
            }
        }

        // Trier par moyenne ponderee
        eligible.sort((a, b) -> Double.compare(toDouble(b.get("moyennePonderee")), toDouble(a.get("moyennePonderee"))));
        risky.sort((a, b) -> Double.compare(toDouble(b.get("moyennePonderee")), toDouble(a.get("moyennePonderee"))));

        Map<String, Object> response = new HashMap<>();
        response.put("serie", serie);
        response.put("notes", Map.of("maths", maths, "physique", physique, "svt", svt, "francais", francais));
        response.put("eligible", eligible);
        response.put("risky", risky);
        response.put("notEligible", notEligible);
        response.put("totalFilieres", FILIERES.size());
        response.put("nbEligible", eligible.size());
        response.put("nbRisky", risky.size());
        response.put("conseil", genererConseil(serie, maths, physique, svt, francais, eligible.size()));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Fiches detaillees des universites du Benin")
    @GetMapping("/universites")
    public ResponseEntity<List<Map<String, Object>>> getUniversites() {
        List<Map<String, Object>> universites = List.of(
            Map.of("nom", "Universite d'Abomey-Calavi (UAC)", "acronyme", "UAC", "ville", "Abomey-Calavi", "type", "Publique", "nbFacultes", 8, "description", "La plus grande et la plus ancienne universite du Benin. 8 facultes, plus de 50 filieres.", "site", "www.uac.bj", "facultes", List.of("FSS", "EPAC", "FAST", "FASEG", "FADESP", "FLASH", "IFRI", "ENEAM")),
            Map.of("nom", "Universite de Parakou (UP)", "acronyme", "UP", "ville", "Parakou", "type", "Publique", "nbFacultes", 4, "description", "Universite du nord du Benin. Medecine, agronomie, droit, lettres.", "site", "www.up.bj", "facultes", List.of("FSS", "FLSH", "FAST", "FASEG")),
            Map.of("nom", "UNSTIM", "acronyme", "UNSTIM", "ville", "Abomey", "type", "Publique", "nbFacultes", 3, "description", "Universite des sciences, technologies, ingenierie et mathematiques.", "site", "www.unstim.bj", "facultes", List.of("FAST", "IUT", "FLSH")),
            Map.of("nom", "Universite Nationale d'Agriculture (UNA)", "acronyme", "UNA", "ville", "Ketou", "type", "Publique", "nbFacultes", 3, "description", "Specialisee en agronomie et developpement rural.", "site", "www.una.bj", "facultes", List.of("FAST", "FASEP", "IUT")),
            Map.of("nom", "IUT de Lokossa", "acronyme", "IUT-L", "ville", "Lokossa", "type", "Publique", "nbFacultes", 3, "description", "Formations professionnalisantes en gestion, informatique, genie civil.", "site", "www.iut-lokossa.bj", "facultes", List.of("TC", "GE", "INFO"))
        );
        return ResponseEntity.ok(universites);
    }

    @Operation(summary = "Informations sur les series du bac")
    @GetMapping("/series")
    public ResponseEntity<List<Map<String, Object>>> getSeries() {
        return ResponseEntity.ok(List.of(
            Map.of("code", "C", "nom", "Serie C (Scientifique)", "description", "Ouvre le plus de portes : medecine, ingenierie, sciences, economie, droit", "matieres", List.of("Maths", "Physique", "SVT", "Francais")),
            Map.of("code", "D", "nom", "Serie D (Sciences Naturelles)", "description", "Avantage pour medecine, pharmacie, agronomie, biologie", "matieres", List.of("Maths", "Physique", "SVT", "Francais")),
            Map.of("code", "A", "nom", "Serie A (Litteraire)", "description", "Droit, lettres, philosophie, sociologie, langues", "matieres", List.of("Francais", "Philosophie", "Langues", "Histoire-Geo")),
            Map.of("code", "B", "nom", "Serie B (Economique)", "description", "Economie, gestion, droit des affaires, commerce international", "matieres", List.of("Maths", "Economie", "Francais", "Langues")),
            Map.of("code", "G2", "nom", "Serie G2 (Gestion)", "description", "Comptabilite, techniques commerciales, management", "matieres", List.of("Maths", "Gestion", "Economie", "Droit"))
        ));
    }

    // Methodes utilitaires

    private double calculerMoyennePonderee(double maths, double physique, double svt, double francais, Map<String, Object> filiere) {
        double coeffMaths = toDouble(filiere.get("maths")) > 0 ? toDouble(filiere.get("maths")) : 1;
        double coeffPhysique = toDouble(filiere.get("physique")) > 0 ? toDouble(filiere.get("physique")) : 1;
        double coeffSvt = toDouble(filiere.get("svt")) > 0 ? toDouble(filiere.get("svt")) : 1;
        double coeffFrancais = toDouble(filiere.get("francais")) > 0 ? toDouble(filiere.get("francais")) : 1;
        
        double total = (maths * coeffMaths + physique * coeffPhysique + svt * coeffSvt + francais * coeffFrancais) 
                       / (coeffMaths + coeffPhysique + coeffSvt + coeffFrancais);
        return total;
    }

    private String getFinancement(double moyenne, Map<String, Object> filiere) {
        double seuil = toDouble(filiere.get("total"));
        if (moyenne >= seuil + 2.0) return "BOURSE";
        if (moyenne >= seuil) return "FPP";
        return "FEP";
    }

    private String genererConseil(String serie, double maths, double physique, double svt, double francais, int nbEligible) {
        if (nbEligible == 0) {
            return "Aucune filiere trouvee pour votre profil. Essayez le FEP pour augmenter vos chances.";
        }
        if (nbEligible <= 3) {
            return "Peu de filieres accessibles. Concentrez-vous sur celles-ci et envisagez le FPP.";
        }
        return "Bon profil ! Vous avez " + nbEligible + " filieres accessibles. Priorisez celles avec bourse.";
    }

    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0; }
    }
}
