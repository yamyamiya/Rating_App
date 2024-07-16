package tesk_task_trood.controllers;

import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tesk_task_trood.dto.*;
import tesk_task_trood.entity.User;
import tesk_task_trood.service.UserService;

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
    @GetMapping("/email") //email?email=olga@example
    public User getUsersByEmail(@RequestParam String email){
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
    public double averageRatingFor(@RequestParam String email) {
        return userService.getUserRatings(email);
    }
    @SneakyThrows
    @PostMapping("/ratings")
    public ResponseEntity<Void> addScoreToTheUserByUsersEmail(@Valid @RequestBody AddScoreDTO addScoreDTO, BindingResult bindingResult){
       if(bindingResult.hasErrors()){
           return ResponseEntity.badRequest().build();
        }else{
            userService.addScoreToTheUserByUsersEmail(addScoreDTO.getRecipientEmail(), addScoreDTO.getSenderEmail(), addScoreDTO.getScore());
            return ResponseEntity.ok().build();
        }
    }

}
