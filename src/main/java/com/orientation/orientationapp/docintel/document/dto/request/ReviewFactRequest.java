package com.orientation.orientationapp.docintel.document.dto.request;
import jakarta.validation.constraints.*;
import java.util.Map;
public record ReviewFactRequest(@NotNull Long version,@NotBlank @Pattern(regexp="TO_REVIEW|VALIDATED|REJECTED") String status,
 @NotNull Map<String,Object> values,@Size(max=40) String kind,@Size(max=4000) String note) {}
