package me.kalpha.natural.event;

import lombok.extern.slf4j.Slf4j;
import me.kalpha.natural.common.ErrorModel;
import me.kalpha.natural.user.CurrentUser;
import me.kalpha.natural.user.User;
import me.kalpha.natural.user.UserRole;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
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
        PagedModel pagedModel = assembler.toModel(events, e -> new EventModel(e));
        if (currentUser != null) {
            pagedModel.add(linkTo(methodOn(EventController.class).create(null, null, null)).withRel("create-new-event"));
        }
        pagedModel.add(linkToProfile("resources-events-list"));
        return ResponseEntity.ok().body(pagedModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity get(@PathVariable Integer id, @CurrentUser User currentUser) {
        Optional<Event> byId = eventRepository.findById(id);
        if (!byId.isPresent()) {
            return notFoundResponse();
        }

        Event event = byId.get();
        EventModel eventModel = new EventModel(event);

        eventModel.add(linkTo(EventController.class).withRel("get-events"));
        if (currentUser != null) {
            eventModel.add(linkTo(EventController.class).withRel("create-new-event"));
            if (currentUser.getRoles().contains(UserRole.ADMIN) || currentUser.equals(event.getManager())) {
                eventModel.add(linkToUpdate(event));
                eventModel.add(linkToDelete(event));
            }
        }
        eventModel.add(linkToProfile("resources-events-get"));
        return ResponseEntity.ok().body(eventModel);
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody EventDto.CreateOrUpdate eventCreate,
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
        eventModel.add(linkTo(EventController.class).withRel("get-events"));
        eventModel.add(linkToProfile("resources-events-create"));
        eventModel.add(linkToUpdate(newEvent));
        eventModel.add(linkToDelete(newEvent));

        URI newEventLocation = linkTo(methodOn(this.getClass()).get(newEvent.getId(), null)).toUri();
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
        if (!currentUser.equals(event.getManager()) && !currentUser.getRoles().contains(UserRole.ADMIN)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        modelMapper.map(eventDto, event);
        event.update();

        eventValidator.validate(event, errors);
        if (errors.hasErrors()) {
            return badRequestResponse(errors);
        }
        eventRepository.save(event);

        EventModel eventModel = new EventModel(event);
        eventModel.add(linkTo(EventController.class).withRel("get-events"));
        eventModel.add(linkTo(EventController.class).withRel("create-new-event"));
        eventModel.add(linkToDelete(event));
        eventModel.add(linkToProfile("resources-events-update"));
        return ResponseEntity.ok().body(eventModel);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Integer id,
                                      @CurrentUser User currentUser) {
        Optional<Event> byId = this.eventRepository.findById(id);
        if (!byId.isPresent()) {
            return notFoundResponse();
        }

        Event event = byId.get();
        if (!currentUser.equals(event.getManager()) && !currentUser.getRoles().contains(UserRole.ADMIN)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        eventRepository.deleteById(id);
        EventModel eventModel = new EventModel(event);
        eventModel.add(linkTo(EventController.class).withRel("get-events"));
        eventModel.add(linkTo(EventController.class).withRel("create-new-event"));
        eventModel.add(linkToProfile("resources-events-delete"));

        return ResponseEntity.ok().body(eventModel);
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
        String linkValue = "</docs/index.html>; rel=\"profile\";";
        if (anchor != null) {
            linkValue = "</docs/index.html#" + anchor + ">; rel=\"profile\";";
        }
        return Link.valueOf(linkValue);
    }

    private Link linkToUpdate(Event newEvent) {
        //return linkTo(methodOn(this.getClass()).update(newEvent.getId(), null, null, null)).withRel("update-event");
        return linkTo(this.getClass()).slash(newEvent.getId()).withRel("update-event");
    }

    private Link linkToDelete(Event existEvent) {
        //return linkTo(methodOn(this.getClass()).delete(existEvent.getId(), null)).withRel("delete-event");
        return linkTo(this.getClass()).slash(existEvent.getId()).withRel("delete-event");
    }
}
