package tesk_task_trood.config;

import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.internal.EmulatorCredentials;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.io.IOException;

@TestConfiguration
public class TestApplicationConfiguration {

    @Bean
    public FirebaseApp connectFirebase(Environment environment) throws IOException {
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setEmulatorHost("localhost:8014").build();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(new EmulatorCredentials())
                .setFirestoreOptions(firestoreOptions)
                .setProjectId("test-task-trood")
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth getFirebaseAuth(FirebaseApp firebaseApp) {
        return Mockito.mock(FirebaseAuth.class);
    }
}
