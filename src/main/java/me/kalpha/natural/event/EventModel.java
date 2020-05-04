package me.kalpha.natural.event;

import lombok.Getter;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
public class EventModel extends EntityModel<Event> {

    public EventModel(Event event, Link... links) {
        super(event, links);
        //add(linkTo(EventController.class).withRel("get-events"));
        //add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
        add(linkTo(methodOn(EventController.class).get(event.getId(), null)).withSelfRel());
    }

}
