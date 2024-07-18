package tesk_task_trood.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tesk_task_trood.dto.*;
import tesk_task_trood.entity.User;
import tesk_task_trood.service.UserService;

import java.security.Principal;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    FirebaseAuth firebaseAuth;


    @SneakyThrows
    @PostMapping("/")
    public UserCreatedResponse createUser(Principal principal){
        UserRecord userRec = extractFBUser(principal);
        User newUser = new User(userRec.getEmail(), userRec.getDisplayName());
        return userService.createUser(newUser);
    }

    @SneakyThrows
    @GetMapping("/")
    public UserListResponse getAllUsers(){
        return userService.getAllUsers();
    }

    @SneakyThrows
    @GetMapping("/email")
    public User getUserByEmail(Principal principal){
        String email = extractFBUser(principal).getEmail();
        return userService.getUserByEmail(email);
    }

    @SneakyThrows
    @GetMapping("/ratings/average")
    public double averageRatingForUserByUserEmail(Principal principal) {
        return userService.getUserRating(extractFBUser(principal).getEmail());
    }

    @SneakyThrows
    @PostMapping("/ratings")
    public ResponseEntity<Void> addScoreToTheUserByUserEmail(@Valid @RequestBody AddScoreDTO addScoreDTO, BindingResult bindingResult, Principal principal){
       if(bindingResult.hasErrors()){
           return ResponseEntity.badRequest().build();
        }else{
            userService.addScoreToTheUserByUserEmail(addScoreDTO.getRecipientEmail(), extractFBUser(principal).getEmail(), addScoreDTO.getScore());
            return ResponseEntity.ok().build();
        }
    }

    @SneakyThrows
    @GetMapping("/toprating")
    public void getTopUsers() {
        userService.calculateTopUsers();
    }

    public UserRecord extractFBUser(Principal principal) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord firebaseUser = firebaseAuth.getUser(principal.getName());
        if(userService.getUserByEmail(firebaseUser.getEmail())==null){
            User newUser = new User(firebaseUser.getEmail(), firebaseUser.getDisplayName());
            userService.createUser(newUser);
        }
        return firebaseUser;
    }

}
