package com.orientation.orientationapp.docintel.document.dto.request;
import jakarta.validation.constraints.*;
import java.util.Map;
public record AddFactRequest(@NotBlank @Size(max=40) String kind,@Min(1) int pageNumber,
 @NotBlank @Size(max=10000) String passage,@NotNull Map<String,Object> values) {}
