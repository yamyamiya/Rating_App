package tesk_task_trood.scheduler;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Component
public class CleanUpUsersTask {

    @Autowired
    Firestore firestore;

    @Scheduled(cron = "0 0 9 * * *")
    public void deleteNotTopUsers() throws ExecutionException, InterruptedException {

        Map<String, Object> usersWithTopRatingsMap = firestore.collection("topratings").document("top list").get().get().getData();
        if(usersWithTopRatingsMap==null){
            return;
        }
        Set<String> topEmails = usersWithTopRatingsMap.keySet();


        List<QueryDocumentSnapshot> usersDocuments = firestore.collection("users").get().get().getDocuments();
        List<QueryDocumentSnapshot> toDeleteUsers = usersDocuments.stream().filter(doc -> !topEmails.contains(doc.getString("email"))).toList();
        List<String> emailsToDelete = toDeleteUsers.stream().map(doc -> doc.getString("email")).toList();

        for (String email : emailsToDelete) {
            firestore.collection("users").document(email).delete();
        }
    }
}
