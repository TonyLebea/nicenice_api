package com.blueconnectionz.nicenice.controller;

import com.blueconnectionz.nicenice.model.Driver;
import com.blueconnectionz.nicenice.model.Owner;
import com.blueconnectionz.nicenice.model.Role;
import com.blueconnectionz.nicenice.payload.req.LoginReq;
import com.blueconnectionz.nicenice.payload.res.LoginRes;
import com.blueconnectionz.nicenice.repository.ConfirmationTokenRepository;
import com.blueconnectionz.nicenice.repository.DriverRepository;
import com.blueconnectionz.nicenice.repository.OwnerRepository;
import com.blueconnectionz.nicenice.repository.UserRepository;
import com.blueconnectionz.nicenice.security.jwt.JWT;
import com.blueconnectionz.nicenice.security.service.ImageStorageService;
import com.blueconnectionz.nicenice.security.service.UserDetailsImp;

import com.blueconnectionz.nicenice.model.ConfirmationToken;
import com.blueconnectionz.nicenice.token.ConfirmationTokenService;
import com.blueconnectionz.nicenice.utils.Helper;
import com.blueconnectionz.nicenice.utils.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.blueconnectionz.nicenice.model.User;
import io.getstream.chat.java.exceptions.StreamException;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

    @Autowired
    Helper helper = new Helper();
    @Autowired
    ImageStorageService imageStorageService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OwnerRepository ownerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager manager;
    @Autowired
    JWT jwt;
    private static final String NOT_FOUND = "User associated with email %s not found";

    ConfirmationTokenService confirmationTokenService;
    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;

    /*
     * CREATE A NEW OWNER ACCOUNT
     * data1 -> Stores User Object
     * data2 -> Stores Owner Object
     */

    @PostMapping("/register-owner")
    public ResponseEntity<?> registerOwner(@RequestPart("data1") Map<String, Object> data1,
                                           @RequestPart("data2") Map<String, Object> data2,
                                           @RequestPart("document") MultipartFile document) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();

        StringWriter out = new StringWriter();
        JSONValue.writeJSONString(data1, out);
        String userObj = out.toString();
        User user = mapper.readValue(userObj, User.class);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.OWNER);

        StringWriter out2 = new StringWriter();
        JSONValue.writeJSONString(data2, out2);
        String ownerObj = out2.toString();

        Owner owner = mapper.readValue(ownerObj, Owner.class);
        owner.setUser(user);
        String uniqueDocumentId = "OW-" + Helper.generatePassword(20);
        owner.setUniqueDocumentId(uniqueDocumentId);

        // Email was registered before
        if (userRepository.existsByEmail(user.getEmail()) || ownerRepository.existsByPhoneNumber(owner.getPhoneNumber())) {
            return ResponseEntity.badRequest().body("ACCOUNT ALREADY EXISTS");
        } else {

            ExecutorService executor = Executors.newFixedThreadPool(10);
            String token = UUID.randomUUID().toString();

            executor.submit(() -> {
                userRepository.save(user);
                ownerRepository.save(owner);
                ConfirmationToken confirmationToken = new ConfirmationToken(
                        token,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15),
                        user
                );
                confirmationTokenRepository.save(confirmationToken);
            });

            String link = "http://nicenice-api.ap-northeast-1.elasticbeanstalk.com/api/v1/auth/confirm?token=" + token;
            uploadFile(document, uniqueDocumentId);

            helper.sendRegistrationMail(user.getEmail(), link);
            return new ResponseEntity<>(owner, HttpStatus.OK);
        }
    }


    /*
     * Checks the validity of the owners registration token
     */
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmOwnerAccount(@RequestParam("token") String token) {

        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).orElseThrow(() ->
                new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());

        Owner owner = ownerRepository.findByUserId(confirmationToken.getUser().getId()).orElseThrow(() ->
                new IllegalStateException("Owner not found"));
        owner.setApproved(true);
        ownerRepository.save(owner);
        confirmationTokenRepository.save(confirmationToken);

        return new ResponseEntity<>("Approved. You can log in on the app!", HttpStatus.OK);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody LoginReq loginReq) {
        if (userRepository.existsByEmail(loginReq.getEmail())) {
            return new ResponseEntity<>("ADMIN EXISTS", HttpStatus.OK);
        } else {
            User user = new User(
                    loginReq.getEmail(),
                    passwordEncoder.encode(loginReq.getPassword()),
                    Role.ADMIN
            );
            userRepository.save(user);
            return new ResponseEntity<>(user.getEmail(), HttpStatus.OK);
        }
    }


    @GetMapping("/users")
    public ResponseEntity<?> allUsers() {
        userRepository.deleteAll();
        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
    }


    @PostMapping("/register-driver")
    public ResponseEntity<?> registerDriver(@RequestPart("data1") Map<String, Object> data1,
                                            @RequestPart("data2") Map<String, Object> data2) throws StreamException, InterruptedException {

        User user = new User();
        user.setEmail((String) data1.get("email"));
        user.setPassword(passwordEncoder.encode((String) data1.get("password")));
        user.setRole(Role.DRIVER);

        /*
         * data2 contains json data that needs to be mapped to the driver object
         */
        Driver driver = new Driver();
        driver.setFullName((String) data2.get("fullName"));
        driver.setPhoneNumber((String) data2.get("phoneNumber"));
        driver.setLocation((String) data2.get("location"));
        driver.setApproved(false);
        driver.setReported(false);
        driver.setCreditBalance(0);
        driver.setPlatform("Uber/Bolt");
        driver.setReference1((String) data2.get("reference1"));
        driver.setReference2((String) data2.get("reference2"));

        driver.setUniqueDocumentId((String) data2.get("uniqueDocumentId"));
        driver.setUser(user);

        // Email was registered before
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Helper.emailExists(user.getEmail()));
        }
        // Phone number was registered before
        if (driverRepository.existsByPhoneNumber(driver.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(Helper.phoneNumberExists(driver.getPhoneNumber()));
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> {
            try {
                userRepository.save(user);
                driverRepository.save(driver);
                // get stream requires existing users
                String email2 = user.getEmail().toLowerCase();
                int atIndex = email2.indexOf('@');
                var usersUpsertRequest = io.getstream.chat.java.models.User.upsert();
                usersUpsertRequest.user(io.getstream.chat.java.models.User.UserRequestObject.builder()
                        .id(email2.replaceAll("[.]", ""))
                        .name(email2.substring(0, atIndex))
                        .build());
                var response = usersUpsertRequest.request();
            } catch (StreamException e) {
                e.printStackTrace();
            }
        });

        // Shut down the executor to prevent any new tasks from being submitted
        executor.shutdown();
        // Wait for all tasks to complete before continuing
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return ResponseEntity.ok().body("DRIVER REGISTERED");

    }


    /*
     * Allows a driver to upload their documents
     * @Return - The unique document ID that will  be mapped to a driver model
     */
    @PostMapping("/upload-driver-docs")
    public ResponseEntity<?> uploadDriverDocuments(
            @RequestPart("documents") List<MultipartFile> documents) throws InterruptedException {
        String uniqueDocumentId = "DR-" + Helper.generatePassword(20);
        uploadFiles(documents, uniqueDocumentId);
        return new ResponseEntity<>(uniqueDocumentId, HttpStatus.OK);
    }


    public void uploadFiles(List<MultipartFile> files, String uniqueDocId) throws InterruptedException {
        // Create a fixed thread pool with a specified number of threads
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (MultipartFile file : files) {
            // Submit a task to the executor to save the file
            executor.submit(() -> {
                try {
                    imageStorageService.save(file, uniqueDocId);
                } catch (IOException e) {
                    // You may want to consider logging the exception or re-throwing it here
                }
            });
        }
        // Shut down the executor to prevent any new tasks from being submitted
        executor.shutdown();
        // Wait for all tasks to complete before continuing
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

    }

    public void uploadFile(MultipartFile file, String uniqueDocId) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> {
            try {
                imageStorageService.save(file, uniqueDocId);
            } catch (IOException e) {
                // You may want to consider logging the exception or re-throwing it here
            }
        });
        // Shut down the executor to prevent any new tasks from being submitted
        executor.shutdown();
        // Wait for all tasks to complete before continuing
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        Resource file = imageStorageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    /*
     * Authenticate users based on the supplied details
     * @Params -> Email , Password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq) {
        // Check the validity of the supplied email and password
        Authentication authenticate = manager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        UserDetailsImp user = (UserDetailsImp) authenticate.getPrincipal();
        ResponseCookie responseCookie = jwt.generateJWT(user);
        // Stores a list of roles the current user has
        List<String> authorities = user.getAuthorities()
                .stream().map(
                        GrantedAuthority::getAuthority).collect(Collectors.toList());

        LoginRes loginRes = new LoginRes(
                user.getId(),
                user.getEmail(),
                authorities
        );

        if (authorities.get(0).equals("DRIVER")) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() ->
                    new NotFoundException(String.format("ID %s not found", user.getId())));

            // Deny the user access if they have been reported
            if (driver.isReported())
                return ResponseEntity.badRequest().body(String.format("Account for %s reported", driver.getFullName()));
            // Deny access if they have not been approved
/*

            if (!driver.isApproved())
                return ResponseEntity.badRequest().body(String.format("Account for %s is being reviewed", driver.getUser().getEmail()));

*/

        } else if (authorities.get(0).equals("OWNER")) {
            Owner owner = ownerRepository.findByUserId(user.getId()).orElseThrow(() ->
                    new NotFoundException(String.format("ID %s not found", user.getId())));

            if (owner.isReported())
                return ResponseEntity.badRequest().body(String.format("Account for %s reported", owner.getUser().getEmail()));
/*

            if (!owner.isApproved())
                return ResponseEntity.badRequest().body(String.format("Account for %s is being reviewed", owner.getUser().getEmail()));
*/

        }
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginRes);
    }


    @PostMapping("/email-exists")
    public ResponseEntity<?> checkIfEmailExists(@RequestBody String email) {

        String _email = email.replaceAll("\"", "");
        if (userRepository.existsByEmail(_email)) {
            User user = userRepository.findByEmail(_email).orElseThrow(
                    () -> new UsernameNotFoundException(
                            String.format(NOT_FOUND, _email)
                    )
            );

            String forgotPasswordLink = "https://nicenice-globe.com";

            helper.sendPasswordMail(user.getEmail(), forgotPasswordLink);
            return new ResponseEntity<>(user.getId(), HttpStatus.OK);
        }
        return new ResponseEntity<>("No Account Found", HttpStatus.NO_CONTENT);
    }


    @PostMapping("/{userID}/new-password")
    public ResponseEntity<?> changePassword(@PathVariable("userID") Long userID, @RequestBody String password) {
        User user = userRepository.getById(userID);
        user.setPassword(passwordEncoder.encode(password.replaceAll("\"", "")));
        userRepository.save(user);
        return new ResponseEntity<>("UPDATED", HttpStatus.OK);
    }



    /*

    spring.datasource.url= jdbc:mysql://nicenicev1db.cxh6kky3suqx.ap-northeast-1.rds.amazonaws.com:3306/nicenicev1db
    spring.datasource.username= niceniceadmin
    spring.datasource.password= 12345678

    spring.datasource.url= jdbc:mysql://localhost:3306/testdb
    spring.datasource.username= root
    spring.datasource.password= 12345678
     */

}
