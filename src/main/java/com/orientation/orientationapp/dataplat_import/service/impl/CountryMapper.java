package com.orientation.orientationapp.dataplat_import.service.impl;

import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
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
public class CountryMapper implements EntityMapper {

    private final CountryRepository countryRepository;

    @Override
    public DataType getDataType() {
        return DataType.COUNTRIES;
    }

    @Override
    @Transactional
    public int mapAndPersist(List<Map<String, Object>> rows) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            try {
                String name = getStringValue(row, "name");
                String code = getStringValue(row, "code");

                if (name == null || code == null) continue;

                Country country = countryRepository.findByCode(code)
                        .map(existing -> {
                            existing.setName(name);
                            return existing;
                        })
                        .orElseGet(Country::new);

                if (country.getId() == null) {
                    country.setName(name);
                }
                country.setName(name);
                country.setCode(code);
                country.setOfficialName(getStringValue(row, "official_name"));
                country.setPhoneCode(getStringValue(row, "phone_code"));
                country.setCurrency(getStringValue(row, "currency"));
                country.setActive(parseBoolean(row.get("active"), true));

                countryRepository.save(country);
                count++;
            } catch (Exception e) {
                log.error("Failed to import country: {}", row, e);
            }
        }
        log.info("Imported {} countries", count);
        return count;
    }

    @Override
    public Class<?> getEntityClass() {
        return Country.class;
    }

    @Override
    public Class<?> getRepositoryClass() {
        return CountryRepository.class;
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
