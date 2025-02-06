package ru.practicum.ewm.main.event.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.main.event.model.AdminStateAction;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequestDto extends UpdateEventBaseRequestDto {
    AdminStateAction stateAction;
}
