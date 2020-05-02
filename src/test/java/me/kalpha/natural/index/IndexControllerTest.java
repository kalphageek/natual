package me.kalpha.natural.index;

import me.kalpha.natural.common.AppSecurityProperties;
import me.kalpha.natural.common.BaseControllerTests;
import me.kalpha.natural.event.EventRepository;
import me.kalpha.natural.user.UserRepository;
import me.kalpha.natural.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IndexControllerTest extends BaseControllerTests {

    @Test
    public void root() throws Exception {
        this.mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andDo(document("index",
                        links(
                            linkWithRel("events").description("The <<resources-events,Events resource>>")
                        )
                ));
    }

}