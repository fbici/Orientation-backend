package com.orientation.orientationapp.modules.scholarship.service;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.common.enums.ScholarshipStatus;
import java.time.LocalDate;
public final class ScholarshipAvailability {
 private ScholarshipAvailability(){}
 public static Boolean open(Scholarship s,LocalDate now) {
  if(!Boolean.TRUE.equals(s.getVerified())||!Boolean.TRUE.equals(s.getActive())||
      s.getStatus()!=ScholarshipStatus.ACTIVE||s.getRemainingSlots()!=null&&s.getRemainingSlots()<=0)return false;
  if(s.getValidFrom()==null||s.getValidUntil()==null||s.getDeadline()==null)return null;
  return !s.getValidFrom().isAfter(now)&&!s.getValidUntil().isBefore(now)&&!s.getDeadline().isBefore(now);
 }
}
