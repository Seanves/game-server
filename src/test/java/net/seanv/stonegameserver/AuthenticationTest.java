package net.seanv.stonegameserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import net.seanv.stonegameserver.repositories.UserRepository;
import net.seanv.stonegameserver.services.AuthenticationService;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthenticationService service;
    private final ObjectMapper mapper = new ObjectMapper();

    private UserAuthDTO testUserAuthDTO;


    @BeforeEach
    public void beforeEach() {
        testUserAuthDTO = new UserAuthDTO("login", "passwordU1!", "nickname");
    }

    @AfterEach
    void afterEach(@Autowired UserRepository userRepository) {
        userRepository.findByLogin("login")
                        .ifPresent(userRepository::delete);
    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }


    @Test
    public void testRegistration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(testUserAuthDTO)))
                        .andExpect(jsonPath("success").value(true));
    }

    @Test
    public void testRegistrationValidation() throws Exception {
        UserAuthDTO[] invalidUserAuthDTOs = {
                new UserAuthDTO("","",""),
                new UserAuthDTO("same_", "same_", "nickname"),
                new UserAuthDTO("login", "same_", "same_"),
                new UserAuthDTO("has space", "passwordU1" ,"nickname"),
                new UserAuthDTO("log", "pU1", "name"), // too short
                new UserAuthDTO("login", "passwordU1", "CAPS_NiCkNaMe"),
                new UserAuthDTO(null, "passwordU1", "nickname"),
                new UserAuthDTO("login", null, "nickname"),
                new UserAuthDTO("login", "passwordU1", null)
        };

        for (UserAuthDTO invalidUserAuthDTO : invalidUserAuthDTOs) {
            mockMvc.perform(MockMvcRequestBuilders.post("/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(invalidUserAuthDTO)))
                            .andExpect(jsonPath("success").value(false));
        }
    }

    @Test
    public void testPasswordValidation() throws Exception {
        // required only english letters, digits and !@#$%^&*-_
        // at least one uppercase letter and digit, length 8-20
        UserAuthDTO[] invalidUserAuthDTOs = {
                new UserAuthDTO("login","qwerty","nickname"),
                new UserAuthDTO("login","qwertyU","nickname"),
                new UserAuthDTO("login","qwerty1","nickname"),
                new UserAuthDTO("login","qwertyU1 ","nickname"),
                new UserAuthDTO("login","qwertyU1Й","nickname"),
                new UserAuthDTO("login","U1longlonglonglonglong","nickname"),
                new UserAuthDTO("login", "pU1(){}[]\"':;?=+/","nickname"),
        };

        for (UserAuthDTO invalidUserAuthDTO : invalidUserAuthDTOs) {
            mockMvc.perform(MockMvcRequestBuilders.post("/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(invalidUserAuthDTO)))
                    .andExpect(jsonPath("success").value(false));
        }
    }

    @Test
    public void testRegistrationFailsForTakenLogin() throws Exception {
        service.register(testUserAuthDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(testUserAuthDTO)))
                        .andExpect(jsonPath("success").value(false))
                        .andExpect(jsonPath("message")
                                .value(Matchers.containsString("Login is already taken")));
    }

    @Test
    public void testLogin() throws Exception {
        service.register(testUserAuthDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(testUserAuthDTO)))
                        .andExpect(jsonPath("success").value(true))
                        .andExpect(jsonPath("token").value(Matchers.notNullValue()));
    }

    @Test
    public void testAuthorizedAccess() throws Exception {
        service.register(testUserAuthDTO);
        String token = service.getNewToken(testUserAuthDTO).token();

        Assertions.assertNotNull(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/userStatus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/userStatus"))
                        .andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    @SneakyThrows
    private String asJsonString(Object object) {
        return mapper.writeValueAsString(object);
    }

}
