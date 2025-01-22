package ru.practicum.ewm.main.event.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateTimeValidatorImpl.class)
public @interface DateTimeValidAnnotation {
    String message() default "the date and time cannot be earlier than two hours from the current moment.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
