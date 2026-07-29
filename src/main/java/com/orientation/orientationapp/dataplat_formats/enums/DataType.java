package com.orientation.orientationapp.dataplat_formats.enums;

import lombok.Getter;

@Getter
public enum DataType {
    UNIVERSITIES("UniversitÃ©s", "DonnÃ©es d'universitÃ©s"),
    FACULTIES("FacultÃ©s", "DonnÃ©es de facultÃ©s"),
    PROGRAMS("Programmes", "DonnÃ©es de programmes/filiÃ¨res"),
    SUBJECTS("MatiÃ¨res", "DonnÃ©es de matiÃ¨res"),
    CRITERIA("CritÃ¨res", "CritÃ¨res d'admission"),
    SCHOLARSHIPS("Bourses", "DonnÃ©es de bourses"),
    SCHOLARSHIP_CRITERIA("CritÃ¨res de bourse", "CritÃ¨res d'Ã©ligibilitÃ© aux bourses"),
    TRANSCRIPTS("RelevÃ©s de notes", "RelevÃ©s de notes de candidats"),
    GUIDE_VERSIONS("Versions de guide", "Versions du guide d'orientation"),
    COUNTRIES("Pays", "DonnÃ©es de pays"),
    CITIES("Villes", "DonnÃ©es de villes"),
    GRADE_SCALES("Ã‰chelles de notes", "Ã‰chelles de conversion");

    private final String label;
    private final String description;

    DataType(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
