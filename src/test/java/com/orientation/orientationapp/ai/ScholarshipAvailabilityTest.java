package com.orientation.orientationapp.ai;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.service.ScholarshipAvailability;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class ScholarshipAvailabilityTest {
 final LocalDate today=LocalDate.of(2026,11,6);
 Scholarship open(){return Scholarship.builder().verified(true).validFrom(today.minusDays(1)).validUntil(today.plusDays(1)).deadline(today).remainingSlots(1).build();}
 @Test void deadlineIsInclusive(){assertEquals(true,ScholarshipAvailability.open(open(),today));}
 @Test void expiredAndFutureAndExhaustedAreClosed(){var s=open();s.setDeadline(today.minusDays(1));assertEquals(false,ScholarshipAvailability.open(s,today));s=open();s.setValidFrom(today.plusDays(1));assertEquals(false,ScholarshipAvailability.open(s,today));s=open();s.setRemainingSlots(0);assertEquals(false,ScholarshipAvailability.open(s,today));}
 @Test void missingDatesNeverMeanOpen(){var s=open();s.setDeadline(null);assertNull(ScholarshipAvailability.open(s,today));}
 @Test void unverifiedAndInactiveNeverMeanOpen(){var s=open();s.setVerified(false);assertEquals(false,ScholarshipAvailability.open(s,today));s=open();s.setActive(false);assertEquals(false,ScholarshipAvailability.open(s,today));}
}
