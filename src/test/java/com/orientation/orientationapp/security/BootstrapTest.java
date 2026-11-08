package com.orientation.orientationapp.security;
import com.orientation.orientationapp.config.ProdDataInitializer;
import com.orientation.orientationapp.modules.auth.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BootstrapTest {
    @Test void disabledUnlessExplicitlyEnabled() {
        var condition=ProdDataInitializer.class.getAnnotation(ConditionalOnProperty.class);
        assertEquals("app.bootstrap.enabled",condition.name()[0]);
        assertFalse(condition.matchIfMissing());assertEquals("true",condition.havingValue());
    }
    @Test void neverUpdatesExistingAdministratorIncludingDeletedAccounts() {
        var users=mock(UserRepository.class);
        when(users.countAllSuperAdministrators()).thenReturn(1L);
        var bootstrap=new ProdDataInitializer(mock(TenantRepository.class),mock(RoleRepository.class),users,
            mock(org.springframework.security.crypto.password.PasswordEncoder.class));
        assertThrows(IllegalStateException.class,()->bootstrap.run(null));
        verify(users,never()).save(any());
    }
    @Test void missingSecretDoesNotCreateAccount() {
        var users=mock(UserRepository.class);
        var bootstrap=new ProdDataInitializer(mock(TenantRepository.class),mock(RoleRepository.class),users,
            mock(org.springframework.security.crypto.password.PasswordEncoder.class));
        ReflectionTestUtils.setField(bootstrap,"email","operator@example.test");
        ReflectionTestUtils.setField(bootstrap,"password","");
        ReflectionTestUtils.setField(bootstrap,"tenantCode","DEFAULT");
        assertThrows(IllegalStateException.class,()->bootstrap.run(null));
        verify(users,never()).save(any());
    }
}
