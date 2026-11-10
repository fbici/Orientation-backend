package com.orientation.orientationapp.modules.university.dto;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
@Getter @Setter
public class CatalogFilter {
 private String search;
 private Boolean registryOnly;
 private UUID countryId,cityId,universityId,campusId,facultyId,academicYearId;
 private String status,type,studyField,language;
 private Boolean active;
}
