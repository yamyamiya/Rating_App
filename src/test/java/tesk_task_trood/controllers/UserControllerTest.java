package tesk_task_trood.controllers;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tesk_task_trood.config.Config;
import tesk_task_trood.config.TestApplicationConfiguration;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@ImportAutoConfiguration(classes = {Config.class, TestApplicationConfiguration.class})
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
        UserRecord userRecord = Mockito.mock(UserRecord.class);
        Mockito.when(userRecord.getEmail()).thenReturn("user@email.com");
        Mockito.when(userRecord.getUid()).thenReturn("user_uid");
        Mockito.when(firebaseAuth.getUser("user_uid")).thenReturn(userRecord);
        mockMvc.perform(get("/user/email")).andExpect(status().isOk());

        DocumentSnapshot firestoreUser = firestore.collection("users").document("user@email.com").get().get();
        assertEquals(0.0, firestoreUser.getData().get("averageRating"));
    }

}