package com.orientation.orientationapp.dataplat_import.service.impl;

import com.orientation.orientationapp.modules.orientation.entity.GradeScale;
import com.orientation.orientationapp.modules.orientation.entity.Subject;
import com.orientation.orientationapp.modules.orientation.repository.GradeScaleRepository;
import com.orientation.orientationapp.modules.orientation.repository.SubjectRepository;
import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_import.service.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectMapper implements EntityMapper {

    private final SubjectRepository subjectRepository;
    private final CountryRepository countryRepository;
    private final GradeScaleRepository gradeScaleRepository;

    @Override
    public DataType getDataType() {
        return DataType.SUBJECTS;
    }

    @Override
    @Transactional
    public int mapAndPersist(List<Map<String, Object>> rows) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            try {
                String name = getStringValue(row, "name");
                String countryCode = getStringValue(row, "country_code");

                if (name == null || countryCode == null) continue;

                Country country = countryRepository.findByCode(countryCode).orElse(null);
                if (country == null) {
                    log.warn("Country not found: {}", countryCode);
                    continue;
                }

                UUID academicYearId = getCurrentAcademicYearId();
                if (academicYearId == null) continue;

                GradeScale gradeScale = gradeScaleRepository.findByCountryIdAndAcademicYearId(country.getId(), academicYearId).orElse(null);
                if (gradeScale == null) {
                    log.warn("Grade scale not found for country: {}", countryCode);
                    continue;
                }

                Subject subject = subjectRepository.findByNameAndGradeScaleId(name, gradeScale.getId())
                        .map(existing -> {
                            existing.setCode(getStringValue(row, "code"));
                            return existing;
                        })
                        .orElseGet(() -> {
                            Subject s = new Subject();
                            s.setName(name);
                            s.setGradeScale(gradeScale);
                            return s;
                        });

                subject.setCategory(getStringValue(row, "category"));
                subject.setActive(parseBoolean(row.get("active"), true));

                String coeff = getStringValue(row, "coefficient");
                if (coeff != null) {
                    subject.setCoefficient(new BigDecimal(coeff));
                }

                subject.setCore(parseBoolean(row.get("core"), true));

                subjectRepository.save(subject);
                count++;
            } catch (Exception e) {
                log.error("Failed to import subject: {}", row, e);
            }
        }
        log.info("Imported {} subjects", count);
        return count;
    }

    @Override
    public Class<?> getEntityClass() {
        return Subject.class;
    }

    @Override
    public Class<?> getRepositoryClass() {
        return SubjectRepository.class;
    }

    private UUID getCurrentAcademicYearId() {
        return null;
    }

    private String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private boolean parseBoolean(Object value, boolean defaultValue) {
        if (value == null) return defaultValue;
        String str = value.toString().trim().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}
