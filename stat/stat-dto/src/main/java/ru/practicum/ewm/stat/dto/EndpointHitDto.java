package ru.practicum.ewm.stat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {
    @NotBlank(message = "Field 'app' cannot be null, empty or blank")
    private String app;

    @NotBlank(message = "Field 'uri' cannot be null, empty or blank")
    private String uri;

    @NotBlank(message = "Field 'ip' cannot be null, empty or blank")
    private String ip;

    @NotNull(message = "Field 'timestamp' cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
