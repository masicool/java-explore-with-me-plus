package ru.practicum.ewm.main.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewUserRequest {
    @NotBlank(message = "Field 'email' cannot be null, empty or blank")
    @Size(min = 6, max = 254, message = "Length of field 'email' should be in the range from 6 ещ 254")
    @Email(message = "Wrong email format")
    String email;

    @NotBlank(message = "Field 'name' cannot be null, empty or blank")
    @Size(min = 2, max = 250, message = "Length of field 'name' should be in the range from 2 to 250")
    String name;
}
