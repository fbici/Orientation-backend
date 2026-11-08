package com.orientation.orientationapp.modules.user.dto;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;
public record CandidateResponse(UUID id,String email,String firstName,String lastName,String phone,
        LocalDate dateOfBirth,String gender,UUID countryId,UUID cityId,String highSchool,Integer bacYear,
        String bacType,BigDecimal bacAverage,String bacMention,String profilePictureUrl,String status,
        Boolean verified,Instant createdAt,Instant updatedAt) {
    public static CandidateResponse from(Candidate c) {
        return new CandidateResponse(c.getId(),c.getEmail(),c.getFirstName(),c.getLastName(),c.getPhone(),
            c.getDateOfBirth(),c.getGender()==null?null:c.getGender().name(),
            c.getCountry()==null?null:c.getCountry().getId(),c.getCity()==null?null:c.getCity().getId(),
            c.getHighSchool(),c.getBacYear(),c.getBacType()==null?null:c.getBacType().name(),c.getBacAverage(),
            c.getBacMention()==null?null:c.getBacMention().name(),c.getProfilePictureUrl(),c.getStatus().name(),
            c.getVerified(),c.getCreatedAt(),c.getUpdatedAt());
    }
}
