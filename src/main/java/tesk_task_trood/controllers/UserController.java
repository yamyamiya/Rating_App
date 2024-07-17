package tesk_task_trood.controllers;

import com.google.firebase.auth.FirebaseAuth;
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

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;


    @SneakyThrows
    @PostMapping("/")
    public UserCreatedResponse createUser(@RequestBody User user){
        return userService.createUser(user);
    }

    @SneakyThrows
    @GetMapping("/")
    public UserListResponse getAllUsers(){
        return userService.getAllUsers();
    }

    @SneakyThrows
    @GetMapping("/email") //email?email=olga@example.com
    public User getUserByEmail(Principal principal){
        UserRecord user = FirebaseAuth.getInstance().getUser(principal.getName());
        String email = user.getEmail();
        return userService.getUserByEmail(email);
    }


    @SneakyThrows
    @PutMapping("/update")
    public UserUpdatedResponse updateUser(@RequestBody User user){
        return userService.updateUser(user);
    }

    @SneakyThrows
    @DeleteMapping("/delete")
    public UserDeletedResponse deleteUser(@RequestParam String email){
        return userService.deleteUser(email);
    }

    @SneakyThrows
    @GetMapping("/ratings/average")
    public double averageRatingForUserByUserEmail(@RequestParam String email) {
        return userService.getUserRating(email);
    }

    @SneakyThrows
    @PostMapping("/ratings")
    public ResponseEntity<Void> addScoreToTheUserByUserEmail(@Valid @RequestBody AddScoreDTO addScoreDTO, BindingResult bindingResult){
       if(bindingResult.hasErrors()){
           return ResponseEntity.badRequest().build();
        }else{
            userService.addScoreToTheUserByUserEmail(addScoreDTO.getRecipientEmail(), addScoreDTO.getSenderEmail(), addScoreDTO.getScore());
            return ResponseEntity.ok().build();
        }
    }

    @SneakyThrows
    @PostMapping("/toprating")
    public void getTopUsers() {
        userService.calculateTopUsers();
    }

}
