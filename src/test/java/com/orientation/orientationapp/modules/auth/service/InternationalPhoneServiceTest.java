package com.orientation.orientationapp.modules.auth.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.orientation.orientationapp.exception.BusinessException;
class InternationalPhoneServiceTest {
    private final InternationalPhoneService service=new InternationalPhoneService();
    @Test void preservesOptionalNumberAndFormatsInternationalContacts() {
        assertNull(service.normalize(null));assertNull(service.normalize("   "));
        assertEquals("+33612345678",service.normalize("+33 6 12 34 56 78"));
        assertEquals("+12025550123",service.normalize("+1 (202) 555-0123"));
        assertEquals("+2348031234567",service.normalize("+234 803 123 4567"));
        assertEquals("+5511987654321",service.normalize("+55 11 98765-4321"));
        assertEquals("+919876543210",service.normalize("+91 98765 43210"));
    }
    @Test void beninUsesTheCurrentTenDigitPlan() {
        assertEquals("+2290197123456",service.normalize("+229 01 97 12 34 56"));
        assertThrows(BusinessException.class,()->service.normalize("+22997123456"));
    }
    @Test void refusesAmbiguousTruncatedUnknownAndExtendedNumbers() {
        for(String invalid:new String[]{"0612345678","+33 6","+999123456789","+12025550123 ext 9","+331234567890123456","+1CALLNOW"})
            assertThrows(BusinessException.class,()->service.normalize(invalid));
    }
}
