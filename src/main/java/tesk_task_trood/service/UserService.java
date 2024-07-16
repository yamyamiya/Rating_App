package tesk_task_trood.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tesk_task_trood.dto.UserCreatedResponse;
import tesk_task_trood.dto.UserDeletedResponse;
import tesk_task_trood.dto.UserListResponse;
import tesk_task_trood.dto.UserUpdatedResponse;
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
        ApiFuture<QuerySnapshot> apiFuture = firestore.collection("users")
                .whereEqualTo("email", email).get();
        List<QueryDocumentSnapshot> list = apiFuture.get().getDocuments();

        return list.stream()
                .map(x -> x.toObject(User.class))
                .findFirst()
                .orElse(null);
    }


    public double getUserRatings(String email) throws ExecutionException, InterruptedException {
//        Collection<Object> ratings = Objects.requireNonNull(firestore.collection("ratings")
//                .document(email).get().get().getData()).values();

        DocumentReference docRefRatings = Objects.requireNonNull(firestore.collection("ratings").document(email));
        if(docRefRatings.get().get().getData()==null){
            return 0;
        }

        Collection<Object> ratings = docRefRatings.get().get().getData().values();

        return ratings.stream().mapToDouble(value -> Double.parseDouble(value.toString())).average().orElse(0);
    }

    public void addScoreToTheUserByUsersEmail(String recipientEmail, String senderEmail, int score) throws ExecutionException, InterruptedException {
        DocumentReference docRefRecipientUser = firestore.collection("users")
                .document(recipientEmail);
        if(docRefRecipientUser.get().get().getData()==null){
            throw new IllegalArgumentException("Not existing recipient user.");
        }
        DocumentReference docRefSenderUser = firestore.collection("users")
                .document(senderEmail);
        if(docRefSenderUser.get().get().getData()==null){
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
    }

    public UserUpdatedResponse updateUser(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(user.getEmail());
        ApiFuture<WriteResult> apiFuture = docRef.set(user);

        return new UserUpdatedResponse(user.getEmail(), apiFuture.get().getUpdateTime().toDate());
    }

    public UserDeletedResponse deleteUser(String email) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(email);
        ApiFuture<WriteResult> apiFuture = docRef.delete();

        return new UserDeletedResponse(apiFuture.get().getUpdateTime().toDate(), true);
    }


}
