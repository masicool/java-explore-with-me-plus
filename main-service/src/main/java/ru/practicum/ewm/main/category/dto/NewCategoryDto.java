package ru.practicum.ewm.main.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCategoryDto {
    @NotBlank(message = "Field 'name' cannot be null, empty or blank")
    @Size(min = 1, max = 50, message = "Length of field 'name' should be in the range from 1 to 50")
    String name;
}
