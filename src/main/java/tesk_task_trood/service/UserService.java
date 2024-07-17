package tesk_task_trood.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tesk_task_trood.dto.UserCreatedResponse;
import tesk_task_trood.dto.UserListResponse;
import tesk_task_trood.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    Firestore firestore;


    public UserCreatedResponse createUser(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(user.getEmail());
        ApiFuture<WriteResult> apiFuture = docRef.set(user);

        return new UserCreatedResponse(user.getEmail(), apiFuture.get().getUpdateTime().toDate());
    }

    public UserListResponse getAllUsers() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> apiFuture = firestore.collection("users").get();
        List<QueryDocumentSnapshot> list = apiFuture.get().getDocuments();
        List<User> allUsers = list.stream()
                .map(x -> x.toObject(User.class))
                .collect(Collectors.toList());

        return new UserListResponse(allUsers);
    }

    public User getUserByEmail(String email) throws ExecutionException, InterruptedException {

       return firestore.collection("users").document(email).get().get().toObject(User.class);
    }


    public double getUserRating(String email) throws ExecutionException, InterruptedException {

        DocumentReference docRefRatings = Objects.requireNonNull(firestore.collection("ratings").document(email));
        if (docRefRatings.get().get().getData() == null) {
            return 0;
        }

        Collection<Object> ratings = Objects.requireNonNull(docRefRatings.get().get().getData()).values();

        return ratings.stream().mapToDouble(value -> Double.parseDouble(value.toString())).average().orElse(0);
    }

    public void addScoreToTheUserByUserEmail(String recipientEmail, String senderEmail, int score) throws ExecutionException, InterruptedException {
        DocumentReference docRefRecipientUser = firestore.collection("users")
                .document(recipientEmail);
        if (docRefRecipientUser.get().get().getData() == null) {
            throw new IllegalArgumentException("Not existing recipient user.");
        }
        DocumentReference docRefSenderUser = firestore.collection("users")
                .document(senderEmail);
        if (docRefSenderUser.get().get().getData() == null) {
            throw new IllegalArgumentException("Not existing sender user.");
        }

        DocumentReference docRefRatings = firestore.collection("ratings")
                .document(recipientEmail);
        Map<String, Object> existingRating = docRefRatings.get().get().getData();
        if (existingRating == null) {
            docRefRatings.set(Map.of(senderEmail, score));

            User updatedUser = Objects.requireNonNull(docRefRecipientUser.get().get().toObject(User.class));
            updatedUser.setAverageRating((double) score);
            docRefRecipientUser.set(updatedUser);
        } else {
            existingRating.put(senderEmail, score);
            docRefRatings.set(existingRating);

            User updatedUser = Objects.requireNonNull(docRefRecipientUser.get().get().toObject(User.class));
            updatedUser.setAverageRating(existingRating.values().stream().mapToDouble(value -> Double.parseDouble(value.toString())).average().orElse(0));
            docRefRecipientUser.set(updatedUser);
        }
        calculateTopUsers();

    }

    public void calculateTopUsers() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> query = firestore.collection("users").orderBy("averageRating", Query.Direction.DESCENDING).limit(10).get().get().getDocuments();

        Map<String, Object> result = query.stream().collect(Collectors.toMap(snapshot -> Objects.requireNonNull(snapshot.get("email")).toString(), snapshot -> Objects.requireNonNull(snapshot.get("averageRating"))));

        firestore.collection("topratings").document("toplist").set(result);
    }
}
