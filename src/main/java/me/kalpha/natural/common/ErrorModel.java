package me.kalpha.natural.common;

import me.kalpha.natural.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class ErrorModel extends EntityModel<Errors> {

    public ErrorModel(Errors content, Link... links) {
        super(content, links);
        add(linkTo(IndexController.class).withRel("index"));
    }
}
