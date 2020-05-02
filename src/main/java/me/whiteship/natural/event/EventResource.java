package me.whiteship.natural.event;

import lombok.Getter;
import me.whiteship.natural.user.User;
import me.whiteship.natural.user.UserAdapter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
public class EventResource extends Resource<Event> {

    public EventResource(Event event, Link... links) {
        super(event, links);
        add(linkTo(EventController.class).withRel("event"));
        add(linkTo(methodOn(EventController.class).getEvent(event.getId(), null)).withSelfRel());
    }

}
