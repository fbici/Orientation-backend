package com.orientation.orientationapp.dataplat_import.service.impl;

import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.entity.University;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import com.orientation.orientationapp.modules.university.repository.UniversityRepository;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_import.service.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityMapper implements EntityMapper {

    private final UniversityRepository universityRepository;
    private final CountryRepository countryRepository;

    @Override
    public DataType getDataType() {
        return DataType.UNIVERSITIES;
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

                University university = universityRepository.findByNameAndCountryId(name, country.getId())
                        .map(existing -> {
                            existing.setShortName(getStringValue(row, "code"));
                            return existing;
                        })
                        .orElseGet(() -> {
                            University u = new University();
                            u.setName(name);
                            u.setCountry(country);
                            return u;
                        });

                university.setAddress(getStringValue(row, "address"));
                university.setPhone(getStringValue(row, "phone"));
                university.setEmail(getStringValue(row, "email"));
                university.setWebsite(getStringValue(row, "website"));
                university.setActive(parseBoolean(row.get("active"), true));

                String ranking = getStringValue(row, "ranking");
                if (ranking != null) {
                    university.setRanking(Integer.parseInt(ranking));
                }

                universityRepository.save(university);
                count++;
            } catch (Exception e) {
                log.error("Failed to import university: {}", row, e);
            }
        }
        log.info("Imported {} universities", count);
        return count;
    }

    @Override
    public Class<?> getEntityClass() {
        return University.class;
    }

    @Override
    public Class<?> getRepositoryClass() {
        return UniversityRepository.class;
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
