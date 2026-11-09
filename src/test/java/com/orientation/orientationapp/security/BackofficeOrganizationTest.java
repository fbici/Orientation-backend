package com.orientation.orientationapp.security;
import com.orientation.orientationapp.modules.admin.service.impl.OrganizationServiceImpl;
import com.orientation.orientationapp.modules.admin.dto.request.CreateOrganizationRequest;
import com.orientation.orientationapp.modules.auth.entity.Organization;
import com.orientation.orientationapp.modules.auth.repository.OrganizationRepository;
import com.orientation.orientationapp.security.service.ResourceAccess;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
class BackofficeOrganizationTest {
 @Test void createPreservesTheExplicitCodeAndContact(){
  var repository=mock(OrganizationRepository.class);var access=mock(ResourceAccess.class);when(access.global()).thenReturn(true);when(repository.save(any())).thenAnswer(i->i.getArgument(0));
  var request=CreateOrganizationRequest.builder().name("Synthetic institution").code("SYNTH").email("fixture@example.invalid").build();
  var result=new OrganizationServiceImpl(repository,access).create(request);assertEquals("SYNTH",result.getCode());assertEquals("fixture@example.invalid",result.getEmail());
 }
 @Test void updateKeepsItsCodeAndContact(){
  var repository=mock(OrganizationRepository.class);var access=mock(ResourceAccess.class);when(access.global()).thenReturn(true);var id=UUID.randomUUID();var org=Organization.builder().name("Synthetic").code("SYNTH").active(true).build();when(repository.findById(id)).thenReturn(Optional.of(org));when(repository.save(any())).thenAnswer(i->i.getArgument(0));
  var request=CreateOrganizationRequest.builder().name("Synthetic updated").code("SYNTH").email("fixture@example.invalid").build();
  var result=new OrganizationServiceImpl(repository,access).update(id,request);assertEquals("SYNTH",result.getCode());assertEquals("fixture@example.invalid",result.getEmail());
 }
}
