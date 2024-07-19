# Test Task Trood

`Springboot application` for users' rating and providing list of top users.

### Task Requirements

- Users (ideally those with logins in Firebase Auth, or as a last resort, just objects in a database of your choice) can leave reviews and ratings for each other.
- There is a separate table (ideally an object in Firebase RTDB) where the top users by rating are stored.
- There is a scheduler that deletes all users except the top ten once a day.

### Implementation

### Technology stack

- `Firebase Authentication` is used for user authentication.
- `Firebase Realtime database` is used as database.
- `Spring Boot Scheduler` is used to schedule tasks to delete users.
- `JUnit, Mockito, SpringBoot Test, Firestore emulator` are used for testing.

### Project configuration

#### Firebase

- `Firebase` is integrated via `firebase-admin`(version 9.3.0) library which requires `admin-sdk.json`, the project will try to find it via environment variable `FIREBASE_TROOD_KEY`. The key itself can be generated and downloaded via Firebase web site: `Project Settings -> Service accounts -> Generate new private key`.
- `Authentication` and `Realtime database` have to be enabled for the project.
- Before login to the App, users with email and password have to be in `Firebase Authentication` `https://console.firebase.google.com/project/{PROJECT-NAME}/authentication/users`.
- `Web API Key` from project settings in Firebase is needed to login the user.
  
#### Login

Use Postman to login the user via `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="{Web API Key}"` with user email and password:

<img width="868" alt="image" src="https://github.com/user-attachments/assets/75c2ae6f-3f66-46d5-8770-dc28b81fbf0b">

Response will have `idToken`:
```
{
    ...
    "idToken": "idToken",
    ...
}
```

Then the `idToken` can be used as value for `Bearer Token` to authenticate requests for Spring Application. 

<img width="848" alt="image" src="https://github.com/user-attachments/assets/c0fd33a3-4fcf-4d29-a88a-65f0a9b25e80">

### Implementation details 

Sources listed bellow were used:

#### Authentication

[https://medium.com/](https://medium.com/comsystoreply/authentication-with-firebase-auth-and-spring-security-fcb2c1dc96d), unfortunately not everything is working as described

#### Database
[https://firebase.google.com/docs/](https://firebase.google.com/docs/admin/setup?hl=ru#java)

P.S. [this library](https://github.com/GoogleCloudPlatform/spring-cloud-gcp) is also can be used for the App, it provides possibility to use `Spring Data` instead direct access to Firestore (that enable replacing of Database without change of the logic). 

### Testing

For testing purpose are used:

- Firestore emulator, to emulate database on local machine.
- Mocked `FirestoreAuth` to provide the application with user details.

`UserControllerTest.class` is responsible for testing 

`@ImportAutoConfiguration(classes = {Config.class, TestApplicationConfiguration.class, TestSecurityConfig.class})` annotation is used in it for pointing the files 
that should be used for testing configuration.

For overridding `connectFirebase` Bean in `Config` with the Bean for emulator `TestApplicationConfiguration` with corresponding configuration was provided.
`test` profile is used for testing to enable option to override beans with `spring.main.allow-bean-definition-overriding=true` line in `application-test.properties` ([based on this source](https://stackoverflow.com/questions/53723303/springboot-beandefinitionoverrideexception-invalid-bean-definition))

`@WithMockUser` annotation is used to mock Authentication process ([based on this article](https://docs.spring.io/spring-security/site/docs/5.2.x/reference/html/test.html))


#### Emulator configuration

In order to run the tests Firestore emulator should be installed on the machine.

Sources listed bellow were used:

[documentation](https://cloud.google.com/firestore/docs/emulator)

[article](https://stackoverflow.com/questions/63781560/connecting-to-firestore-emulator-from-java-admin-sdk) 

To run emulator:

```
gcloud emulators firestore start
```

Currently used port is given as response:
<img width="846" alt="image" src="https://github.com/user-attachments/assets/a66594de-1d49-4b38-b755-42f6fb864fa4">

This port has to be used in `TestApplicationconfiguration`:

```
FirestoreOptions.newBuilder().setEmulatorHost("localhost:8014").build()
```

## Endpoints
 
The following endpoints are provided in UserController:

POST: `/user/` - creation of new user in database by adding its email and name with zero rating to the collection of "users"

GET: `/user/` - return all existing users in "users" collection

`/user/email` - return the current User data

`/user/ratings/average` - return the current User rating

`/user/ratings` - addition of score from 1-5 by current user to the recipient using recipient email 

`/user/toprating` - generating "topratings" collection with "toplist" document where 10 users with highest rating are provided



