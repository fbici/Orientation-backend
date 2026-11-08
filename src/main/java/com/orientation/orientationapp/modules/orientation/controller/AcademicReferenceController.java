package com.orientation.orientationapp.modules.orientation.controller;
import com.orientation.orientationapp.modules.orientation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.row;
@RestController @RequiredArgsConstructor @Transactional(readOnly=true)
public class AcademicReferenceController {
 private final AcademicYearRepository years;private final SubjectRepository subjects;private final GradeScaleRepository scales;
 @GetMapping("/academic-years") public List<Map<String,Object>> years(){return years.findAll().stream().filter(y->!Boolean.TRUE.equals(y.getDeleted())).map(y->row("id",y.getId(),"label",y.getLabel(),"startDate",y.getStartDate(),"endDate",y.getEndDate(),"active",y.getActive())).toList();}
 @GetMapping("/subjects") public List<Map<String,Object>> subjects(@RequestParam(required=false)UUID countryId,@RequestParam(required=false)UUID academicYearId){return subjects.findAll().stream().filter(s->!Boolean.TRUE.equals(s.getDeleted())).filter(s->countryId==null||s.getGradeScale().getCountry().getId().equals(countryId)).filter(s->academicYearId==null||s.getGradeScale().getAcademicYear().getId().equals(academicYearId)).map(s->row("id",s.getId(),"name",s.getName(),"code",s.getCode(),"gradeScaleId",s.getGradeScale().getId(),"countryId",s.getGradeScale().getCountry().getId(),"academicYearId",s.getGradeScale().getAcademicYear().getId())).toList();}
 @GetMapping("/grade-scales") public List<Map<String,Object>> scales(){return scales.findAll().stream().filter(s->!Boolean.TRUE.equals(s.getDeleted())).map(s->row("id",s.getId(),"name",s.getName(),"countryId",s.getCountry().getId(),"academicYearId",s.getAcademicYear().getId(),"minScore",s.getMinScore(),"maxScore",s.getMaxScore(),"passingScore",s.getPassingScore())).toList();}
}
