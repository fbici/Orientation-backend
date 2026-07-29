package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.modules.university.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    List<City> findByCountryId(UUID countryId);
    Optional<City> findByNameAndCountryId(String name, UUID countryId);
    boolean existsByNameAndCountryId(String name, UUID countryId);
}
