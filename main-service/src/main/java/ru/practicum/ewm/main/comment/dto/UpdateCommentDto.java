package ru.practicum.ewm.main.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCommentDto {
    @NotBlank(message = "Field 'text' cannot be null, empty or blank")
    @Size(min = 2, max = 1000, message = "Length of field 'annotation' should be in the range from 2 to 1000")
    String text;
}
