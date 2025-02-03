package ru.practicum.ewm.main.compilation.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class GetCompilationsParams {
    Boolean pinned;
    int from;
    int size;
}
