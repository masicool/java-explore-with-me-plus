package ru.practicum.ewm.main.event.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.ewm.main.event.dto.NewEventDto;

import java.time.LocalDateTime;

public class DateTimeValidatorImpl implements ConstraintValidator<DateTimeValidAnnotation, NewEventDto> {
    @Override
    public void initialize(DateTimeValidAnnotation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(NewEventDto value, ConstraintValidatorContext context) {
        LocalDateTime eventDate = value.getEventDate();
        return !eventDate.isBefore(LocalDateTime.now().plusHours(2));
    }
}
