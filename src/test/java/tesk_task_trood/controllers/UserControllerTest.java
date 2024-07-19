package tesk_task_trood.controllers;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tesk_task_trood.config.Config;
import tesk_task_trood.config.TestApplicationConfiguration;
import tesk_task_trood.config.TestSecurityConfig;
import tesk_task_trood.dto.AddScoreDTO;
import tesk_task_trood.dto.UserCreatedResponse;
import tesk_task_trood.dto.UserListResponse;
import tesk_task_trood.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@ImportAutoConfiguration(classes = {Config.class, TestApplicationConfiguration.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    public void beforeEach() throws ExecutionException, InterruptedException {
        firestore.recursiveDelete(firestore.collection("users")).get();
        firestore.recursiveDelete(firestore.collection("ratings")).get();
        firestore.recursiveDelete(firestore.collection("topratings")).get();
    }

    @Test
    public void shouldDenyAccessForNonAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/user/email")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser("user_uid")
    public void shouldGiveAccessForAuthenticatedUsers() throws Exception {
        configureAuthentication();
        mockMvc.perform(get("/user/email")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser("user_uid")
    public void shouldReturnExistingUser() throws Exception {
        configureAuthentication();
        firestore.collection("users").document("user@email.com").set(Map.of("email", "user@email.com", "name", "user", "averageRating", 1.5)).get();
        mockMvc.perform(get("/user/email")).andExpect(status().isOk()).andExpect(result -> {
            User responseUser = new Gson().fromJson(result.getResponse().getContentAsString(), User.class);
            assertEquals("user@email.com", responseUser.getEmail());
            assertEquals("user", responseUser.getName());
            assertEquals(1.5, responseUser.getAverageRating());
        });
    }

    @Test
    @WithMockUser("user_uid")
    public void shouldReturnAllUsers() throws Exception {
        configureAuthentication();
        firestore.collection("users").document("user1@email.com").set(Map.of("email", "user1@email.com", "name", "user1", "averageRating", 1.5)).get();
        firestore.collection("users").document("user2@email.com").set(Map.of("email", "user2@email.com", "name", "user2", "averageRating", 3.5)).get();
        firestore.collection("users").document("user3@email.com").set(Map.of("email", "user3@email.com", "name", "user3", "averageRating", 4.5)).get();
        mockMvc.perform(get("/user/")).andExpect(status().isOk()).andExpect(result -> {
            List<User> responseList = new Gson().fromJson(result.getResponse().getContentAsString(), UserListResponse.class).getList();
            assertEquals(3, responseList.size());
            assertEquals("user1", responseList.get(0).getName());
            assertEquals("user2", responseList.get(1).getName());
            assertEquals("user3", responseList.get(2).getName());
            assertEquals("user1@email.com", responseList.get(0).getEmail());
            assertEquals("user2@email.com", responseList.get(1).getEmail());
            assertEquals("user3@email.com", responseList.get(2).getEmail());
            assertEquals(1.5, responseList.get(0).getAverageRating());
            assertEquals(3.5, responseList.get(1).getAverageRating());
            assertEquals(4.5, responseList.get(2).getAverageRating());
        });
    }

    @Test
    @WithMockUser("user_uid")
    public void shouldReturnUserAverageRating() throws Exception {
        configureAuthentication();
        firestore.collection("ratings").document("user@email.com").set(Map.of("user2@email.com", 2, "user3@email.com", 4)).get();
        mockMvc.perform(get("/user/ratings/average")).andExpect(status().isOk()).andExpect(result -> {
            double responseRating = new Gson().fromJson(result.getResponse().getContentAsString(), Double.class);
            assertEquals(3, responseRating);
        });
    }

    @Test
    @WithMockUser("user_uid")
    public void shouldNotAddScoreToTheUserIfValidationIsNotPassed() throws Exception {
        configureAuthentication();
        mockMvc.perform(post("/user/ratings").contentType(MediaType.APPLICATION_JSON).content(
                new Gson().toJson(new AddScoreDTO("recipient@email.com", 6))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser("user_uid")
    public void shouldAddScoreToTheUserIfValidationIsPassed() throws Exception {
        configureAuthentication();
        firestore.collection("users").document("recipient@email.com").set(Map.of("email", "recipient@email.com", "name", "recipient", "averageRating", 0.0));
        mockMvc.perform(post("/user/ratings").contentType(MediaType.APPLICATION_JSON).content(
                        new Gson().toJson(new AddScoreDTO("recipient@email.com", 3))))
                .andExpect(status().isOk());
        Map<String, Object> ratings = firestore.collection("ratings").document("recipient@email.com").get().get().getData();
        assert ratings != null;
        assertEquals(3, Double.parseDouble(ratings.get("user@email.com").toString()));
    }


    @Test
    @WithMockUser("user_uid")
    public void shouldCreateTopRatingCollection() throws Exception {
        configureAuthentication();
        firestore.collection("users").document("user1@email.com").set(Map.of("email", "user1@email.com", "name", "user1", "averageRating", 3)).get();
        firestore.collection("users").document("user2@email.com").set(Map.of("email", "user2@email.com", "name", "user2", "averageRating", 4)).get();
        firestore.collection("users").document("user3@email.com").set(Map.of("email", "user3@email.com", "name", "user3", "averageRating", 5)).get();
        firestore.collection("users").document("user4@email.com").set(Map.of("email", "user4@email.com", "name", "user4", "averageRating", 4)).get();
        firestore.collection("users").document("user5@email.com").set(Map.of("email", "user5@email.com", "name", "user5", "averageRating", 4)).get();
        firestore.collection("users").document("user6@email.com").set(Map.of("email", "user6@email.com", "name", "user6", "averageRating", 3.5)).get();
        firestore.collection("users").document("user7@email.com").set(Map.of("email", "user7@email.com", "name", "user7", "averageRating", 4.5)).get();
        firestore.collection("users").document("user8@email.com").set(Map.of("email", "user8@email.com", "name", "user8", "averageRating", 3.5)).get();
        firestore.collection("users").document("user9@email.com").set(Map.of("email", "user9@email.com", "name", "user9", "averageRating", 4.5)).get();
        firestore.collection("users").document("user10@email.com").set(Map.of("email", "user10@email.com", "name", "user10", "averageRating", 5)).get();
        firestore.collection("users").document("user11@email.com").set(Map.of("email", "user11@email.com", "name", "user11", "averageRating", 1.5)).get();
        firestore.collection("users").document("user12@email.com").set(Map.of("email", "user12@email.com", "name", "user12", "averageRating", 2.5)).get();
        firestore.collection("users").document("user13@email.com").set(Map.of("email", "user13@email.com", "name", "user13", "averageRating", 2.5)).get();
        mockMvc.perform(get("/user/toprating")).andExpect(status().isOk());
        DocumentSnapshot documentSnapshot = firestore.collection("topratings").document("toplist").get().get();
        assertEquals(10, Objects.requireNonNull(documentSnapshot.getData()).size());
        assertEquals(0,documentSnapshot.getData().values().stream().mapToDouble(value -> Double.parseDouble(value.toString())).filter(x-> x<3).count());
    }


    @Test
    @WithMockUser("user_uid")
    public void shouldAddNewUserToFB() throws Exception {
        configureAuthentication();
        mockMvc.perform(post("/user/")).andExpect(status().isOk()).andExpect(result -> {
            UserCreatedResponse userResponse = new Gson().fromJson(result.getResponse().getContentAsString(), UserCreatedResponse.class);
            assertEquals("user@email.com", userResponse.getEmail());
        });
    }



    private void configureAuthentication() throws FirebaseAuthException {
        UserRecord userRecord = Mockito.mock(UserRecord.class);
        Mockito.when(userRecord.getEmail()).thenReturn("user@email.com");
        Mockito.when(userRecord.getUid()).thenReturn("user_uid");
        Mockito.when(userRecord.getDisplayName()).thenReturn("user");
        Mockito.when(firebaseAuth.getUser("user_uid")).thenReturn(userRecord);
    }

}
