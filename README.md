Test Task Trood
Springboot application for users rating and providing list of top users

Technology stack
This software is based on Spring Boot framework.

Firebase Realtime Database is used as database

JUnit, Mockito, SpringBoot Test are used for testing.

Settings
as DB was used FirebaseDataBase
used the following library for connection of FB to springBoot App:
<dependency>
  <groupId>com.google.firebase</groupId>
  <artifactId>firebase-admin</artifactId>
  <version>9.3.0</version>
</dependency>

for connection of FB created new Project and in Project Settings -> Service accounts generated new private key for FB
used this key in Config.class in connectFirebase Bean

As Entity User were used real Users of FB with their credentials, using OAuth2 
In this article were found instructions for SecutyConfig https://medium.com/comsystoreply/authentication-with-firebase-auth-and-spring-security-fcb2c1dc96d

Using request POST on Postman https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="key"
and as a key using Web API Key from FB from General Project settings we ligin User
we get idToken that we provide as Bearer Token for Authorization of user request

As endpoints in UserController are:
POST: /user/ - creation of new user in database by adding its email and name with zero rating to the collection of "users"
GET: /user/ - return all existing users in "users" collection
/user/email - return the current User data
/user/ratings/average - return the current User rating
/user/ratings - addition of score form 1-5 by current user to the recipient using recipient email 
/user/toprating - generating "topratings" collection with "toplist" document where 10 users with highest rating are provided

Also there is provided CleanUpUsersTask.class that removes all users that are not included in "topratings" collection from "users" collection each day at 9a.m.

Testing 
UserControllerTest.class is responsible for testing 
@ImportAutoConfiguration(classes = {Config.class, TestApplicationConfiguration.class, TestSecurityConfig.class}) annotation is used for pointing the files 
that should be used for testing configuration
For testing of database is used FB emulator (https://cloud.google.com/firestore/docs/emulator)
The setting are provided in TestApplicationConfiguration.class based on https://stackoverflow.com/questions/63781560/connecting-to-firestore-emulator-from-java-admin-sdk
for overridding "connectFirebase" Bean in Config used "spring.main.allow-bean-definition-overriding=true" line in application-test.properties
(https://stackoverflow.com/questions/53723303/springboot-beandefinitionoverrideexception-invalid-bean-definition)
in tests were used @WithMockUser annotation (https://docs.spring.io/spring-security/site/docs/5.2.x/reference/html/test.html) to mock Authentication process





