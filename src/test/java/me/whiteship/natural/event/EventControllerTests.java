package me.whiteship.natural.event;

import me.whiteship.natural.common.AppSecurityProperties;
import me.whiteship.natural.common.BaseControllerTests;
import me.whiteship.natural.common.Description;
import me.whiteship.natural.user.User;
import me.whiteship.natural.user.UserRepository;
import me.whiteship.natural.user.UserRole;
import me.whiteship.natural.user.UserService;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTests extends BaseControllerTests {

    @Autowired
    UserService userService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppSecurityProperties appSecurityProperties;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @Description("Trying to create new event with correct data.")
    @Test
    public void createEvent() throws Exception {
        // Given
        EventDto.CreateOrUpdate eventDto = createEventDto();

        // When & Then
        mockMvc.perform(post("/api/events")
                    .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(eventDto))
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.toString()))
                .andExpect(jsonPath("_links.self.href").isNotEmpty())
                .andExpect(jsonPath("$._links.profile.href").isNotEmpty())
                .andDo(document(
                    "create-event",
                    links(halLinks(),
                        linkWithRel("profile").description("Link to profile"),
                        linkWithRel("self").description("Link to the created event"),
                        linkWithRel("event").description("Link to view all events"),
                        linkWithRel("update").description("Link to update the event")
                    ),
                    getRequestFieldsSnippet(),
                    relaxedResponseFields(
                        fieldWithPath("id").description("id of new event")
                    ),
                    responseHeaders(
                        headerWithName("location").description("new event URL")
                    )
                ))
        ;
    }

    @Description("Trying to create an event with wrong data and fail.")
    @Test
    public void createNewEvent_bindingError() throws Exception {
        Event event = Event.builder().build();

        mockMvc.perform(post("/api/events")
                    .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content[0].defaultMessage").isNotEmpty())
                .andExpect(jsonPath("$.content[0].field").isNotEmpty())
                .andExpect(jsonPath("$.content[0].defaultMessage").isNotEmpty())
                .andExpect(jsonPath("$.content[0].rejectedValue").exists())
                .andExpect(jsonPath("$.content[0].rejectedValue").isEmpty())
                .andDo(document("errors",
                    links(
                        linkWithRel("index").description("Link to index")
                    ),
                    relaxedResponseFields(
                        fieldWithPath("content").description("Error content")
                    )
                ));
    }

    @Description("Getting an event successfully as a user not manager of the event")
    @Test
    public void getEvent() throws Exception {
        // Given
        Event newEvent = this.eventRepository.save(this.createSampleEvent());

        // When & Then
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/events/{id}", newEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.update").doesNotExist())
                .andDo(document("get-event",
                    relaxedLinks(
                        linkWithRel("self").description("link to this event."),
                        linkWithRel("event").description("link to all events."),
                        linkWithRel("profile").description("link to profile.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("identifier of an Event.")
                    ),
                    relaxedResponseFields(
                        fieldWithPath("id").description("id of the event"),
                        fieldWithPath("name").description("name of the event"),
                        fieldWithPath("description").description("name of the event")
                    )
                ))
        ;
    }

    @Description("Getting an event successfully as a manager of the event")
    @Test
    public void getEventAsAManager() throws Exception {
        // Given
        String email = "manager@email.com";
        String originalPassword = "manager";
        User manager = userService.createUser(
                User.builder().email(email).password(originalPassword).roles(Set.of(UserRole.USER)).build()
        );
        Event sampleEvent = this.createSampleEvent();
        sampleEvent.setManager(manager);
        Event newEvent = this.eventRepository.save(sampleEvent);

        // When & Then
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/events/{id}", newEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken(manager, originalPassword))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.update").hasJsonPath())
        ;
    }

    @Description("Trying to get non-existing event.")
    @Test
    public void getEventFail() throws Exception {
        // Given
        int noneExistingId = 1;

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/event/{id}", noneExistingId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document("get-event-fail",
                    pathParameters(
                        parameterWithName("id").description("identifier of an Event.")
                    )
                ))
        ;
    }

    @Description("Trying to get all events.")
    @Test
    public void getEvents() throws Exception {
        // Given
        Event event = this.eventRepository.save(this.createSampleEvent());

        // When & Then
        this.mockMvc.perform(get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.eventList[0].id").value(event.getId()))
                .andExpect(jsonPath("_embedded.eventList[0].name", Matchers.is(event.getName())))
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.events").hasJsonPath())
                .andExpect(jsonPath("_links.get-an-event").hasJsonPath())
                .andExpect(jsonPath("_links.create-new-event").hasJsonPath())
                .andExpect(jsonPath("_embedded.eventList[0].name", Matchers.is(event.getName())))
                .andDo(document("get-events",
                    relaxedLinks(
                        linkWithRel("profile").description("Link to profile"),
                        linkWithRel("self").description("Link to self"),
                        linkWithRel("get-an-event").description("Link to get an event")
                    ),
                    requestParameters(
                        parameterWithName("page").description("page to retrieve, begin with and default is 0").optional(),
                        parameterWithName("size").description("Sie of the page to retrieve, default 20").optional()
                    ),
                    relaxedResponseFields(
                        fieldWithPath("page.number").type(JsonFieldType.NUMBER).description("The number of this page."),
                        fieldWithPath("page.size").type(JsonFieldType.NUMBER).description("The size of this page."),
                        fieldWithPath("page.totalPages").type(JsonFieldType.NUMBER).description("The total number of pages."),
                        fieldWithPath("page.totalElements").type(JsonFieldType.NUMBER).description("The total number of results.")
                    )
                ))
        ;
    }

    @Description("Try to get events without token")
    @Test
    public void getEventAnonymous() throws Exception {
        // Given
        Event event = this.eventRepository.save(this.createSampleEvent());

        // When & Then
        this.mockMvc.perform(get("/api/events"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.eventList[0].id").value(event.getId()))
                .andExpect(jsonPath("_embedded.eventList[0].name").value(event.getName()))
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.events").hasJsonPath())
                .andExpect(jsonPath("_links.get-an-event").hasJsonPath())
                .andExpect(jsonPath("_links.create-new-event").doesNotExist())
        ;
    }

    @Description("Manager can update existing event with correct data.")
    @Test
    public void updateEvent() throws Exception {
        // Given
        String email = "manager@email.com";
        String originalPassword = "manager";
        User manager = userService.createUser(
                User.builder().email(email).password(originalPassword).roles(Set.of(UserRole.USER)).build()
        );

        Event sampleEvent = this.createSampleEvent();
        sampleEvent.setManager(manager);
        Event existingEvent = this.eventRepository.save(sampleEvent);

        String newName = RandomString.make(10);
        EventDto.CreateOrUpdate eventDto = createEventDto();
        eventDto.setName(newName);
        eventDto.setBasePrice(0);
        eventDto.setMaxPrice(0);
        eventDto.setLocation(null);

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", existingEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken(manager, originalPassword)))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(existingEvent.getId()))
                .andExpect(jsonPath("name").value(newName))
                .andExpect(jsonPath("basePrice").value(0))
                .andExpect(jsonPath("maxPrice").value(0))
                .andExpect(jsonPath("location").isEmpty())
                .andExpect(jsonPath("free").value(true))
                .andExpect(jsonPath("offline").value(false))
                .andDo(document("update-event",
                    relaxedLinks(
                        linkWithRel("profile").description("Link to profile")
                    ),
                    getRequestFieldsSnippet(),
                    relaxedResponseFields(
                        fieldWithPath("id").description("id of the event")
                    )
                ))
        ;
    }

    @Description("Trying to update existing event with wrong data")
    @Test
    public void updateEvent_fail() throws Exception {
        // Given
        Event existingEvent = this.eventRepository.save(this.createSampleEvent());
        EventDto.CreateOrUpdate eventDto = createEventDto();
        eventDto.setName(null);

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", existingEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Description("If a user trying to update existing event, it will response 403 Forbidden.")
    @Test
    public void updateEvent_forbidden() throws Exception {
        // Given
        String managerEmail = "manager@email.com";
        String managerPassword = "manager";
        User manager = userService.createUser(
            User.builder().email(managerEmail).password(managerPassword).roles(Set.of(UserRole.USER)).build()
        );

        String userEmail = "anotherUser@email.com";
        String userPassword = "user";
        User user = userService.createUser(
            User.builder().email(userEmail).password(userPassword).roles(Set.of(UserRole.USER)).build()
        );

        Event sampleEvent = this.createSampleEvent();
        sampleEvent.setManager(manager);
        Event existingEvent = this.eventRepository.save(sampleEvent);

        String newName = RandomString.make(10);
        EventDto.CreateOrUpdate eventDto = createEventDto();
        eventDto.setName(newName);
        eventDto.setBasePrice(0);
        eventDto.setMaxPrice(0);
        eventDto.setLocation(null);

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", existingEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(getAccessToken(user, userPassword)))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }



    private RequestFieldsSnippet getRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("name").description("event name"),
                fieldWithPath("description").description("description of the event"),
                fieldWithPath("beginEnrollmentDateTime").description("date and time to begin enrollment."),
                fieldWithPath("closeEnrollmentDateTime").description("date and time to close enrollment."),
                fieldWithPath("beginEventDateTime").description("date and time to begin the event."),
                fieldWithPath("endEventDateTime").description("date and time to end the event."),
                fieldWithPath("location").optional().description("link to the place where the event hold"),
                fieldWithPath("basePrice").optional().description("price of ticket to enroll."),
                fieldWithPath("maxPrice").optional().description("maximum price of ticket to enroll. \n" +
                        "if this value does not provided, " +
                        "then it means non-limited bidding will happen,\n" +
                        "and can't expect how much it would be to enroll the event eventually.\n" +
                        "If both basePrice and maxPrice are null or 0, it means free event."),
                fieldWithPath("limitOfEnrollment").description("number of limit")
        );
    }

    private Event createSampleEvent() {
        return modelMapper.map(this.createEventDto(), Event.class);
    }

    private EventDto.CreateOrUpdate createEventDto() {
        return EventDto.CreateOrUpdate.builder()
                .name("test event")
                .description("testing event apis")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 10, 15, 0, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 3, 23, 59))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 10, 9, 0))
                .endEventDateTime(LocalDateTime.of(2018, 11, 10, 14, 0))
                .location("Inflean")
                .basePrice(50000)
                .maxPrice(10000)
                .build();
    }

    private String bearer(String aceessToken) {
        return "Bearer " + aceessToken;
    }

    private String getAccessToken() throws Exception {
        String email = "User" + System.currentTimeMillis() + "@email.com";
        String password = "pass";

        var user = User.builder()
                .email(email)
                .password(password)
                .roles(Set.of(UserRole.USER))
                .build();

        User newUser = userService.createUser(user);
        return getAccessToken(newUser, password);
    }

    private String getAccessToken(User user, String originalPassword) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", user.getEmail());
        params.add("password", originalPassword);

        // When & Then
        var result = mockMvc.perform(post("/oauth/token")
                .params(params)
                .with(httpBasic(appSecurityProperties.getDefaultClientId(), appSecurityProperties.getDefaultClientSecret()))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(resultString).get("access_token").toString();
    }

}
