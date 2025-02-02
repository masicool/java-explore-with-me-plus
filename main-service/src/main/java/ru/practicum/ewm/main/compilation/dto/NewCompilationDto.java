package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {
    @NotBlank(message = "Field 'title' cannot be null, empty or blank")
    @Size(min = 1, max = 50, message = "Length of field 'title' should be in the range from 1 to 50")
    String title;
    boolean pinned;
    List<Long> events;
}
