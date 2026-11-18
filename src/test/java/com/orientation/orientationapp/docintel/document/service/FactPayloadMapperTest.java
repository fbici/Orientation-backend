package com.orientation.orientationapp.docintel.document.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository;
import com.orientation.orientationapp.modules.university.entity.*;
import com.orientation.orientationapp.modules.university.repository.*;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.modules.orientation.repository.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FactPayloadMapperTest {
 @Test void optionalCityDoesNotBlockUniversityOrCampusReview() {
  try(var factory=Validation.buildDefaultValidatorFactory()) {
   var countries=mock(CountryRepository.class); var cities=mock(CityRepository.class);
   var catalogue=mock(AcademicCatalogService.class);
   var mapper=new FactPayloadMapper(new ObjectMapper(),factory.getValidator(),mock(ExtractedFactRepository.class),catalogue,countries,cities,mock(AcademicYearRepository.class),mock(SubjectRepository.class));
   UUID countryId=UUID.randomUUID(), universityId=UUID.randomUUID();
   when(countries.findById(countryId)).thenReturn(Optional.of(new Country()));
   when(catalogue.university(universityId)).thenReturn(new University());
   var fact=new ExtractedFact(); fact.setKind("UNIVERSITY");
   assertDoesNotThrow(()->mapper.request(fact,Map.of("name","Universite test","countryId",countryId),false));
   fact.setKind("CAMPUS");
   assertDoesNotThrow(()->mapper.request(fact,Map.of("name","Campus test","universityId",universityId),false));
   verifyNoInteractions(cities);
   verify(catalogue).university(universityId);
  }
 }
 @Test void suppliedCityMustExistAndBelongToUniversityCountry() {
  try(var factory=Validation.buildDefaultValidatorFactory()) {
   var countries=mock(CountryRepository.class); var cities=mock(CityRepository.class);
   var mapper=new FactPayloadMapper(new ObjectMapper(),factory.getValidator(),mock(ExtractedFactRepository.class),mock(AcademicCatalogService.class),countries,cities,mock(AcademicYearRepository.class),mock(SubjectRepository.class));
   UUID countryId=UUID.randomUUID(), cityId=UUID.randomUUID();
   when(countries.findById(countryId)).thenReturn(Optional.of(new Country()));
   var fact=new ExtractedFact(); fact.setKind("UNIVERSITY");
   var values=Map.<String,Object>of("name","Universite test","countryId",countryId,"cityId",cityId);
   assertThrows(ResponseStatusException.class,()->mapper.request(fact,values,false));
   var otherCountry=new Country(); otherCountry.setId(UUID.randomUUID());
   var city=new City(); city.setCountry(otherCountry);
   when(cities.findById(cityId)).thenReturn(Optional.of(city));
   assertThrows(ResponseStatusException.class,()->mapper.request(fact,values,false));
  }
 }
}
