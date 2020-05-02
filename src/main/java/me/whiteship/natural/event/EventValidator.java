package me.whiteship.natural.event;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Event.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Event event = (Event)target;
        validateEndEventDateTime(errors, event);
        validateBeginDateTime(errors, event);
        validateCloseEnrollmentDateTime(errors, event);
    }

    private void validateCloseEnrollmentDateTime(Errors errors, Event event) {
        LocalDateTime closeEnrollmentDateTime = event.getCloseEnrollmentDateTime();
        if (closeEnrollmentDateTime.isBefore(event.getBeginEnrollmentDateTime())) {
            errors.rejectValue("closeEnrollmentDateTime", "wrong.datetime");
        }
    }

    private void validateBeginDateTime(Errors errors, Event event) {
        LocalDateTime beginEventDateTime = event.getBeginEventDateTime();
        if (beginEventDateTime.isBefore(event.getBeginEnrollmentDateTime()) ||
        beginEventDateTime.isBefore(event.getCloseEnrollmentDateTime())) {
            errors.rejectValue("beginEventDateTime", "wrong.datetime");
        }
    }

    private void validateEndEventDateTime(Errors errors, Event event) {
        LocalDateTime endEventDateTime = event.getEndEventDateTime();
        if (endEventDateTime.isBefore(event.getBeginEventDateTime()) ||
                endEventDateTime.isBefore(event.getBeginEnrollmentDateTime()) ||
                endEventDateTime.isBefore(event.getCloseEnrollmentDateTime())) {
            errors.rejectValue("endEventDateTime", "wrong.datetime");
        }
    }
}
