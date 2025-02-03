package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatchCompilationDto {
    long id;
    @Size(max = 50, message = "Length of field 'title' should be in the range from 0 to 50")
    String title;
    Boolean pinned;
    List<Long> events;
}
