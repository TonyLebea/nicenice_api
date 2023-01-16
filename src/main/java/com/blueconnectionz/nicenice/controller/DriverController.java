package com.blueconnectionz.nicenice.controller;

import com.blueconnectionz.nicenice.model.*;
import com.blueconnectionz.nicenice.payload.res.CarConnection;
import com.blueconnectionz.nicenice.payload.res.CarsRes;
import com.blueconnectionz.nicenice.payload.res.ChatConnection;
import com.blueconnectionz.nicenice.payload.res.UserRes;
import com.blueconnectionz.nicenice.repository.*;
import com.blueconnectionz.nicenice.security.service.ImageStorageService;
import com.blueconnectionz.nicenice.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/driver")
public class DriverController {

    @Autowired
    ImageStorageService imageStorageService;
    @Autowired
    DealRepository dealRepository;
    @Autowired
    OwnerRepository ownerRepository;
    @Autowired
    CarRepository carRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    TransactionsRepository transactionsRepository;

    /*
     * @Return a list of cars in the system
     * @View - Driver Home page
     */
    @GetMapping("/all-cars")
    public ResponseEntity<?> getAllCars() {
        List<CarsRes> response = new ArrayList<>();

        List<Car> carList = carRepository.findAll();
        LocalDateTime dateToday = LocalDateTime.now();
        if (carList.isEmpty())
            return ResponseEntity.ok().body("No cars available");
        else if (!carList.isEmpty())
            for (Car c : carList) {
                try {
                    long age = dateToday.compareTo(c.getCreatedAt());
                    c.setAge(Math.toIntExact(age));
                    List<Deal> deals = dealRepository.findByCarID(c.getId());
                    c.setNumConnections(deals.size());
                    List<Document> document = documentRepository.findByUniqueDocumentId(c.getUniqueCarImgID());
                    response.add(new CarsRes(c, document.get(0).getUrl()));
                } catch (NullPointerException e) {
                    carList.stream().skip(carList.indexOf(c));
                }
            }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/delete-cars")
    public ResponseEntity<?> deleteCars() {
        driverRepository.deleteAll();
        return new ResponseEntity<>("DONE", HttpStatus.OK);
    }

    /*
     * @Returns current users profile information
     */
    @GetMapping("/{userId}/profile-info")
    public ResponseEntity<?> profileInfo(@PathVariable(value = "userId") Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ResponseEntity<>("ACCOUNT DOES NOT EXIST", HttpStatus.NO_CONTENT);
        }
        User user = userRepository.getById(userId);
        if (user.getRole().equals(Role.DRIVER)) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            List<Document> documents = documentRepository.findByUniqueDocumentId(driver.getUniqueDocumentId());
            UserRes response = new UserRes(
                    driver.getFullName(),
                    user.getEmail(),
                    driver.getPhoneNumber(),
                    documents
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (user.getRole().equals(Role.OWNER)) {
            Owner owner = ownerRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            List<Document> documents = documentRepository.findByUniqueDocumentId(owner.getUniqueDocumentId());
            UserRes response = new UserRes(
                    null,
                    user.getEmail(),
                    owner.getPhoneNumber(),
                    documents
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{userId}/update-profile")
    public ResponseEntity<?> updateProfile(@PathVariable(value = "userId") Long userId,
                                           @RequestBody UserRes request) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.ok().body("ACCOUNT DOES NOT EXIST");
        }

        User user = userRepository.getById(userId);
        if (user.getRole().equals(Role.DRIVER)) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            driver.setFullName(request.getFullName());
            driver.setPhoneNumber(request.getPhoneNumber());
            user.setEmail(request.getEmail());
            userRepository.save(user);
            driverRepository.save(driver);
            return new ResponseEntity<>("PROFILE UPDATED", HttpStatus.OK);
        } else if (user.getRole().equals(Role.OWNER)) {
            Owner owner = ownerRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            owner.setPhoneNumber(request.getPhoneNumber());
            user.setEmail(request.getFullName());
            userRepository.save(user);
            ownerRepository.save(owner);
            return new ResponseEntity<>("PROFILE UPDATED", HttpStatus.OK);
        }
        return null;
    }


    @PutMapping("/{carID}/{userID}/update-view")
    public ResponseEntity<?> updateViews(@PathVariable(value = "carID") Long carID,
                                         @PathVariable(value = "userID") Long userID) {
        Car car = carRepository.getById(carID);
        System.out.println("VIEWERS " + carID + " | " + userID);
        // Check if the current user has viewed the car yet
        if (!car.getUsersWhoViewed().contains(String.valueOf(userID))) {
            car.setViews(car.getViews() + 1);

            List<String> driversWhoViewed = new ArrayList<>(car.getUsersWhoViewed());
            driversWhoViewed.add(String.valueOf(userID));
            car.setUsersWhoViewed(driversWhoViewed);

            carRepository.save(car);
            return new ResponseEntity<>(car, HttpStatus.OK);
        }
        return new ResponseEntity<>("ALREADY VIEWED", HttpStatus.OK);
    }


    @PostMapping("/{userId}/check-password")
    public boolean checkIfPasswordIsValid(@PathVariable(value = "userId") Long userId,
                                          @RequestBody String password) {
        User user = userRepository.getById(userId);

        return passwordEncoder.matches(password.replaceAll("\"", ""), user.getPassword());
    }


    @PostMapping("/{userId}/new-password")
    public ResponseEntity<?> changePassword(@PathVariable(value = "userId") Long userId, @RequestBody String password) {
        User user = userRepository.getById(userId);
        user.setPassword(passwordEncoder.encode(password.replaceAll("\"", "")));
        userRepository.save(user);
        return new ResponseEntity<>("UPDATED", HttpStatus.OK);
    }


    @GetMapping("/{userID}/{carID}/car-info")
    public ResponseEntity<?> getCarInfo(@PathVariable("userID") Long userID,
                                        @PathVariable("carID") Long carID) {

        Car car = carRepository.getById(carID);

        Driver driver = driverRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));

        int balance = driver.getCreditBalance();
        int dealCost = 150;
        int driverRequest = Integer.parseInt(car.getWeeklyTarget());

        List<Deal> deals = dealRepository.findByCarID(carID);
        int peopleInDeal = deals.size();
        int peopleWhoAboveMin = 0;
        for (Deal d : deals) {
            if (d.getAmount() >= driverRequest)
                peopleWhoAboveMin++;
        }

        Locale SOUTH_AFRICA = new Locale("en", "ZA");
        NumberFormat randFormat = NumberFormat.getCurrencyInstance(SOUTH_AFRICA);

        return new ResponseEntity<>(new CarConnection(balance, dealCost,
                randFormat.format(driverRequest), peopleInDeal, peopleWhoAboveMin
        ), HttpStatus.OK);
    }

    @PostMapping("/{userID}/{carID}/connect-owner")
    public ResponseEntity<?> connectWithOwner(@PathVariable("userID") Long userID,
                                              @PathVariable("carID") Long carID) {

        User user = userRepository.getById(userID);
        Car car = carRepository.getById(carID);

        if (user.getRole().equals(Role.DRIVER)) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));

            // The driver has enough credit and the car is available
            if (driver.getCreditBalance() >= 50 && car.isAvailable()) {
                // Deduct 50 credits from the driver
                driver.setCreditBalance(driver.getCreditBalance() - 50);
                driverRepository.save(driver);
                // Recording the transaction
                Transactions newTransaction = transactionsRepository.save(new Transactions(-50, userID, false));

                // Create a new channel with the owner on the client side
                var token = io.getstream.chat.java.models.User
                        .createToken(driver.getUser().getEmail().replaceAll("[.]", "").toLowerCase()
                                , null, null);

                var owner = ownerRepository.getById(car.getOwnerID());

                ChatConnection chatConnection = new ChatConnection(
                        owner.getUser().getEmail().toLowerCase(),
                        driver.getUser().getEmail().toLowerCase(),
                        token,
                        newTransaction
                );

                return new ResponseEntity<>(chatConnection, HttpStatus.OK);

            }
            // The driver does not have enough credit
            else if (!(driver.getCreditBalance() >= 50)) {
                return new ResponseEntity<>("NOT ENOUGH CREDIT", HttpStatus.OK);
            }
            // The car is not available
            else if (!(car.isAvailable())) {
                return new ResponseEntity<>("CAR NOT AVAILABLE", HttpStatus.OK);
            }
        }

        return new ResponseEntity<>("CAN'T ESTABLISH CONNECTION", HttpStatus.OK);
    }




    @GetMapping("/{userID}/is-online")
    public ResponseEntity<?> isDriverOnline(@PathVariable("userID") Long userID){
        Driver driver = driverRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
        return new ResponseEntity<>(driver.isOnline(),HttpStatus.OK);
    }

    @PutMapping("/{userID}/is-online")
    public ResponseEntity<?> updateOnline(@PathVariable("userID") Long userID,
                                          @RequestBody boolean value){
        Driver driver = driverRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
        driver.setOnline(value);
        driverRepository.save(driver);
        return new ResponseEntity<>(value,HttpStatus.OK);
    }




    /*
    spring.datasource.url= jdbc:postgresql://ec2-52-70-45-163.compute-1.amazonaws.com:5432/de7u26m075tnmu?sslmode=require
    spring.datasource.username= vadloszjrazhui
    spring.datasource.password= ef933de17d78243531476d9bfb207759c09a4e849755df7c4ca0b6d76777556f

     spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation= true
    spring.jpa.properties.hibernate.dialect= org.hibernate.dialect.PostgreSQLDialect
    # Hibernate ddl auto (create, create-drop, validate, update)
    spring.jpa.hibernate.ddl-auto= update
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jackson.serialization.fail-on-empty-beans=false
     */

    /*
    spring.datasource.url= jdbc:mysql://localhost:3306/testdb?allowPublicKeyRetrieval=true&useSSL=false
    spring.datasource.username= root
    spring.datasource.password= 123456

    spring.jpa.properties.hibernate.dialect= org.hibernate.dialect.MySQL5InnoDBDialect
    spring.jpa.hibernate.ddl-auto= update

     */


}



