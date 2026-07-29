package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.modules.university.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
    Optional<Country> findByCode(String code);
    Optional<Country> findByName(String name);
    boolean existsByCode(String code);
}
