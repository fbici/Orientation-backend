package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.modules.university.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Country c where c.id=:id")
    Optional<Country> lock(@org.springframework.data.repository.query.Param("id") UUID id);
    Optional<Country> findByCode(String code);
    Optional<Country> findByName(String name);
    boolean existsByCode(String code);
}
