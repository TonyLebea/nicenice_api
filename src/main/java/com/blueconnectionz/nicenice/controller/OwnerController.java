package com.blueconnectionz.nicenice.controller;

import com.blueconnectionz.nicenice.model.*;
import com.blueconnectionz.nicenice.payload.res.*;
import com.blueconnectionz.nicenice.repository.*;
import com.blueconnectionz.nicenice.security.service.ImageStorageService;
import com.blueconnectionz.nicenice.utils.Helper;
import com.blueconnectionz.nicenice.utils.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.Doc;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.blueconnectionz.nicenice.utils.Helper.driverProfileImage;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/owner")
public class OwnerController {

    @Autowired
    DealRepository dealRepository;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    OwnerRepository ownerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    CarRepository carRepository;
    @Autowired
    ImageStorageService imageStorageService;
    @Autowired
    TransactionsRepository transactionsRepository;

    @Autowired
    UserRepository userRepository;

    /*
     * Retrieve all the drivers that are on the system -> Displayed on the driver home page
     * If currently connected to a driver hide them from the home page
     */
    @GetMapping("/{userID}/drivers")
    public ResponseEntity<?> getAllDrivers(@PathVariable(value = "userID") Long userID) throws InterruptedException {
        Owner owner = ownerRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));

        List<Driver> drivers = driverRepository.findAll();
        if (drivers.isEmpty()) {
            return ResponseEntity.ok().body("No drivers yet");
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);


        List<AllDriversRes> response = new ArrayList<>();
        LocalDateTime dateToday = LocalDateTime.now();
        Random random = new Random();

        for (Driver driver : drivers) {
            executor.submit(() -> {
                // Check if the owner has not connected/ not in communication with the driver
                if (!owner.getDriversConnectedWith().contains(driver.getId())) {

                    // How long the driver has been on the system
                    int age = dateToday.compareTo(driver.getCreatedAt());
                    // Locate the drivers image from the list of uploaded documents

                    try {
                        response.add(new AllDriversRes(
                                driver.getId(),
                                driver.getFullName(), transactionsRepository.findByUserID(driver.getUser().getId()).size(),
                                age, driver.getLocation(), random.nextInt(3) + 1,
                                driverProfileImage(documentRepository.findByUniqueDocumentId(driver.getUniqueDocumentId())),
                                driver.isOnline()
                        ));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }

            });
        }
        // Shut down the executor to prevent any new tasks from being submitted
        executor.shutdown();
        // Wait for all tasks to complete before continuing
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/{userID}/car-application")
    public ResponseEntity<?> newCar(@PathVariable(value = "userID") Long userID,
                                    @RequestPart("car") Map<String, Object> newCar,
                                    @RequestPart("document") MultipartFile carImage) throws IOException {

        Owner owner = ownerRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
        if (owner.getCreditBalance() >= 150) {
            String uniqueCarImgID = "C-" + Helper.generatePassword(20);

            StringWriter writer = new StringWriter();
            JSONValue.writeJSONString(newCar, writer);
            String carObj = writer.toString();

            ObjectMapper mapper = new ObjectMapper();
            Car car = mapper.readValue(carObj, Car.class);
            car.setUniqueCarImgID(uniqueCarImgID);
            car.setOwnerID(owner.getId());

            uploadFile(carImage, uniqueCarImgID);
            carRepository.save(car);

            return new ResponseEntity<>(car, HttpStatus.OK);
        }
        return new ResponseEntity<>("NOT ENOUGH CREDIT", HttpStatus.NOT_ACCEPTABLE);
    }

    public void uploadFile(MultipartFile file, String uniqueDocumentId) {
        try {
            imageStorageService.save(file, uniqueDocumentId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @PostMapping("/{userID}/{driverID}/connect-driver")
    public ResponseEntity<?> connectWithDriver(@PathVariable(value = "userID") Long userID,
                                               @PathVariable(value = "driverID") Long driverID) {
        // Check if the owner has enough credit to connect to the driver
        Owner owner = ownerRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("OWNER DOES NOT EXIST"));

        if (owner.getCreditBalance() >= 150 && !owner.getDriversConnectedWith().contains(String.valueOf(driverID))) {

            // Record the deal
            Transactions newTransaction = transactionsRepository.save(new Transactions(-150, userID, false));
            // Reduce the owners credits by 150
            owner.setCreditBalance(owner.getCreditBalance() - 150);
            var token = io.getstream.chat.java.models.User
                    .createToken(owner.getUser().getEmail().replaceAll("[.]", "").toLowerCase()
                            , null, null);

            // Expose the chat channel ID
            Driver driver = driverRepository.getById(driverID);

            // Update the list of drivers the owner is communicating with
            List<String> listOfDriverIDS = new ArrayList<>(owner.getDriversConnectedWith());
            listOfDriverIDS.add(String.valueOf(driverID));
            owner.setDriversConnectedWith(listOfDriverIDS);

            ownerRepository.save(owner);
            driverRepository.save(driver);


            ChatConnection chatConnection = new ChatConnection(
                    owner.getUser().getEmail().toLowerCase(),
                    driver.getUser().getEmail().toLowerCase(),
                    token,
                    newTransaction
            );

            return new ResponseEntity<>(chatConnection, HttpStatus.OK);
        } else if (owner.getDriversConnectedWith().contains(String.valueOf(driverID))) {
            return new ResponseEntity<>("ALREADY CONNECTED", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("NOT ENOUGH CREDIT Cr " + owner.getCreditBalance(), HttpStatus.OK);
        }
    }

    @GetMapping("/{userID}/dashboard-info")
    public ResponseEntity<?> dashboard(@PathVariable(value = "userID") Long userID) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Owner owner = ownerRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("OWNER DOES NOT EXIST"));
        List<Car> cars = carRepository.findByOwnerID(owner.getId());
        int numAccepted = 0;
        int numRejected = 0;

        // A list of all the cars the owner has listed on the platform
        List<OwnerCars> ownerCars = new ArrayList<>();
        for (Car c : cars) {
            executor.submit(() -> {
                List<Document> documents = documentRepository.findByUniqueDocumentId(c.getUniqueCarImgID());
                String imageURL = getImageURLFromList(documents);
                List<Deal> deals = dealRepository.findByCarID(c.getId());
                ownerCars.add(new OwnerCars(imageURL, c.getMake() + " " + c.getModel(), deals.size(), c.isAvailable()));

            });

        }

        OwnerDashRes response = new OwnerDashRes(
                cars.size(),
                numAccepted,
                numRejected,
                ownerCars
        );

        executor.shutdown();
        // Wait for all tasks to complete before continuing
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return new ResponseEntity<>(
                response, HttpStatus.OK
        );
    }

    private String getImageURLFromList(List<Document> documents) {
        for (Document d : documents) {
            if (d.getUrl().contains(".png") || d.getUrl().contains(".jpg") || d.getUrl().contains(".jpeg"))
                return d.getUrl();
        }
        return "";
    }


    @GetMapping("/{userID}/transaction-history")
    public ResponseEntity<?> allTransactions(@PathVariable(value = "userID") Long userID) {
        List<Transactions> transactions = transactionsRepository.findByUserID(userID);
        User user = userRepository.getById(userID);
        if (user.getRole().equals(Role.DRIVER)) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            TransactionsInfoRes response = new TransactionsInfoRes(
                    transactions,
                    driver.getCreditBalance()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);

        } else if (user.getRole().equals(Role.OWNER)) {
            Owner owner = ownerRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            TransactionsInfoRes response = new TransactionsInfoRes(
                    transactions,
                    owner.getCreditBalance()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);

        }
        return new ResponseEntity<>("NOT ELIGIBLE FOR CREDITS", HttpStatus.OK);
    }


    @GetMapping("/transactions")
    public ResponseEntity<?> allTransactions() {
        return new ResponseEntity<>(transactionsRepository.findAll(), HttpStatus.OK);
    }

    /*
     * Allows an owner to specify a car as unavailable
     * A list of drivers in the deal will be returned
     * The owner has to specify which driver got the car
     */
    @PutMapping("/{carID}/disable-car")
    public ResponseEntity<?> disableCar(@PathVariable(value = "carID") Long carID) {
        Car car = carRepository.getById(carID);
        car.setAvailable(false);
        return new ResponseEntity<>(carRepository.save(car), HttpStatus.OK);
    }



    /*
     * We need to determine the owner's city in the client
     * List<Driver> findByDriverCity(String city)
     * If no drivers in owners current city deduct once off payment to view all drivers
     */


}