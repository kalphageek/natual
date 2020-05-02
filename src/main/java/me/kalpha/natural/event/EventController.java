package me.kalpha.natural.event;

import lombok.extern.slf4j.Slf4j;
import me.kalpha.natural.common.ErrorModel;
import me.kalpha.natural.user.CurrentUser;
import me.kalpha.natural.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/events")
@Slf4j
public class EventController {

    private final ModelMapper modelMapper;

    private final EventRepository eventRepository;

    private final EventValidator eventValidator;

    public EventController(ModelMapper modelMapper, EventRepository eventRepository, EventValidator eventValidator) {
        this.modelMapper = modelMapper;
        this.eventRepository = eventRepository;
        this.eventValidator = eventValidator;
    }

    @GetMapping
    public ResponseEntity all(Pageable pageable, PagedResourcesAssembler<Event> assembler, @CurrentUser User currentUser) {
        if (currentUser == null) {
            log.debug("Current user doesn't exist");
        } else {
            log.debug("Current user {} {}", currentUser.getId(), currentUser.getEmail());
        }

        Page<Event> events = eventRepository.findAll(pageable);
        var pagedModel = assembler.toModel(events, e -> new EventModel(e));
        pagedModel.add(linkTo(EventController.class).withRel("events"));
        pagedModel.add(linkTo(methodOn(EventController.class).getEvent(null, null)).withRel("get-an-event"));
        if (currentUser != null) {
            pagedModel.add(linkTo(methodOn(EventController.class).createEvent(null, null, null)).withRel("create-new-event"));
        }
        pagedModel.add(linkToProfile("resources-events-list"));
        return ResponseEntity.ok().body(pagedModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id, @CurrentUser User currentUser) {
        Optional<Event> byId = eventRepository.findById(id);
        if (!byId.isPresent()) {
            return notFoundResponse();
        }

        Event event = byId.get();
        EventModel eventModel = new EventModel(event);
        if (currentUser != null && currentUser.equals(event.getManager())) {
            eventModel.add(linkToUpdate(event));
        }
        eventModel.add(linkToProfile("resources-events-get"));
        return ResponseEntity.ok().body(eventModel);
    }

    @PostMapping
    public ResponseEntity createEvent(@Valid @RequestBody EventDto.CreateOrUpdate eventCreate,
                                      BindingResult errors,
                                      @CurrentUser User currentUser) {
        if (errors.hasErrors()) {
            return badRequestResponse(errors);
        }

        Event event = modelMapper.map(eventCreate, Event.class);
        event.update(currentUser);

        eventValidator.validate(event, errors);
        if (errors.hasErrors()) {
            return badRequestResponse(errors);
        }

        Event newEvent = eventRepository.save(event);
        EventModel eventModel = new EventModel(newEvent);
        eventModel.add(linkToProfile("resources-events-create"));
        eventModel.add(linkToUpdate(newEvent));

        URI newEventLocation = linkTo(methodOn(this.getClass()).getEvent(newEvent.getId(), null)).toUri();
        return ResponseEntity.created(newEventLocation).body(eventModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable Integer id,
                                 @Valid @RequestBody EventDto.CreateOrUpdate eventDto,
                                 BindingResult errors,
                                 @CurrentUser User currentUser) {
        if (errors.hasErrors()) {
            return badRequestResponse(errors);
        }

        Optional<Event> byId = this.eventRepository.findById(id);
        if (!byId.isPresent()) {
            return notFoundResponse();
        }

        Event event = byId.get();
        if (currentUser != null && !currentUser.equals(event.getManager())) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        modelMapper.map(eventDto, event);
        event.update();

        eventValidator.validate(event, errors);
        if (errors.hasErrors()) {
            return badRequestResponse(errors);
        }

        EventModel eventModel = new EventModel(event);
        eventModel.add(linkToProfile("resources-events-update"));
        return ResponseEntity.ok().body(eventModel);
    }

    @DeleteMapping("/{id")
    public ResponseEntity delete(@PathVariable Integer id) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity publish(@PathVariable Integer id) {
        throw new UnsupportedOperationException();
    }

    private ResponseEntity<Object> notFoundResponse() {
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<ErrorModel> badRequestResponse(BindingResult errors) {
        return ResponseEntity.badRequest().body(new ErrorModel(errors));
    }

    private Link linkToProfile(String anchor) {
        var linkValue = "</docs/index.html>; rel=\"profile\";";
        if (anchor != null) {
            linkValue = "</docs/index.html#" + anchor + ">; rel=\"profile\";";
        }
        return Link.valueOf(linkValue);
    }

    private Link linkToUpdate(Event newEvent) {
        return linkTo(methodOn(this.getClass()).update(newEvent.getId(), null, null, null)).withRel("update");
    }
}
