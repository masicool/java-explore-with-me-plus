package ru.practicum.ewm.main.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.main.event.model.EventSortAction;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class FindAllEventsPublicParamEntity {
    String text;
    List<Long> categories;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    boolean onlyAvailable;
    EventSortAction sort;
    int from;
    int size;
}
