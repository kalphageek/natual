package me.whiteship.natural.event;

import me.whiteship.natural.common.Description;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class EventValidatorTest {

    @Description("Validate valid event")
    @Test
    public void testValidation() {
        // Given
        Event event = new Event();
        event.setBeginEnrollmentDateTime(LocalDateTime.of(2018, 10, 19, 8, 0));
        event.setCloseEnrollmentDateTime(LocalDateTime.of(2018, 10, 20, 8, 0));
        event.setBeginEventDateTime(LocalDateTime.of(2018, 10, 21, 8, 0));
        event.setEndEventDateTime(LocalDateTime.of(2018, 10, 22, 8, 0));

        EventValidator eventValidator = new EventValidator();
        Errors errors = new BeanPropertyBindingResult(event, "event");

        // When
        eventValidator.validate(event, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }

    @Description("Validate invalid event")
    @Test
    public void testValidationFail() {
        // Given
        Event event = new Event();
        event.setBeginEnrollmentDateTime(LocalDateTime.of(2018, 10, 22, 8, 0));
        event.setCloseEnrollmentDateTime(LocalDateTime.of(2018, 10, 21, 8, 0));
        event.setBeginEventDateTime(LocalDateTime.of(2018, 10, 20, 8, 0));
        event.setEndEventDateTime(LocalDateTime.of(2018, 10, 19, 8, 0));

        EventValidator eventValidator = new EventValidator();
        Errors errors = new BeanPropertyBindingResult(event, "event");

        // When
        eventValidator.validate(event, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldErrors("endEventDateTime").size()).isEqualTo(1);
        assertThat(errors.getFieldErrors("beginEventDateTime").size()).isEqualTo(1);
        assertThat(errors.getFieldErrors("closeEnrollmentDateTime").size()).isEqualTo(1);
    }

}