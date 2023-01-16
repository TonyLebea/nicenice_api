package com.blueconnectionz.nicenice.controller;


import com.blueconnectionz.nicenice.model.*;
import com.blueconnectionz.nicenice.payload.req.DriverReq;
import com.blueconnectionz.nicenice.payload.res.AllCarsRes;
import com.blueconnectionz.nicenice.payload.res.AllDriversRes;
import com.blueconnectionz.nicenice.payload.res.TransactionInfo;
import com.blueconnectionz.nicenice.repository.*;
import com.blueconnectionz.nicenice.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/admin")
public class AdminController {

    @Autowired
    CarRepository carRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OwnerRepository ownerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    DealRepository dealRepository;
    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    TransactionsRepository transactionsRepository;


    // Block / Report a driver
    @PutMapping("/{driverId}/suspend-driver")
    public ResponseEntity<?> suspendDriver(@PathVariable(value = "driverId") Long driverId) {
        Driver driver = driverRepository.getById(driverId);
        driver.setReported(true);
        driverRepository.save(driver);
        return new ResponseEntity<>(
                driver,
                HttpStatus.OK
        );
    }

    /*
     * Allows an admin to approve a driver account
     * A notification is sent to the driver when the account is approved
     */
    @PutMapping("/{driverId}/approve-driver")
    public ResponseEntity<?> approveDriver(@PathVariable(value = "driverId") Long driverId) {
        Driver driver = driverRepository.getById(driverId);
        driver.setApproved(true);
        driverRepository.save(driver);
        return new ResponseEntity<>(
                driver,
                HttpStatus.OK
        );
    }


    // Delete a driver from the system
    @PostMapping("/{driverId}/delete-driver")
    public ResponseEntity<?> deleteDriver(@PathVariable(value = "driverId") Long driverId) {
        Driver driver = driverRepository.getById(driverId);
        driverRepository.delete(driver);
        userRepository.delete(driver.getUser());
        return ResponseEntity.ok().body(String.format("Driver %s deleted", driver.getFullName()));
    }

    // Load credit for a owner/driver
    @PutMapping("/{userId}/load-credit")
    public ResponseEntity<?> loadCredit(@PathVariable(value = "userId") Long userId, @RequestParam("query") int amount) {
        User user = userRepository.getById(userId);
        if (user.getRole().equals(Role.DRIVER)) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("DRIVER DOES NOT EXIST"));
            driver.setCreditBalance(driver.getCreditBalance() + amount);

            // Record the transaction
            Transactions newTransaction = transactionsRepository.save(new Transactions(amount, userId, true));
            driverRepository.save(driver);

            return new ResponseEntity<>(new TransactionInfo<>(driver, newTransaction), HttpStatus.OK);
        } else if (user.getRole().equals(Role.OWNER)) {
            Owner owner = ownerRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("OWNER DOES NOT EXIST"));
            owner.setCreditBalance(owner.getCreditBalance() + amount);
            // Record the transaction
            Transactions newTransaction = transactionsRepository.save(new Transactions(amount, userId, true));
            ownerRepository.save(owner);
            return new ResponseEntity<>(new TransactionInfo<>(owner, newTransaction), HttpStatus.OK);
        }
        return new ResponseEntity<>("CANNOT LOAD BALANCE", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{driverId}/load-owner-credit")
    public ResponseEntity<?> loadOwnerCredit(@PathVariable(value = "driverId") Long driverId, @RequestParam("query") int amount) {

        Driver driver = driverRepository.getById(driverId);
        driver.setCreditBalance(driver.getCreditBalance() + amount);

        // Record the transaction
        Transactions newTransaction = transactionsRepository.save(new Transactions(amount, driver.getUser().getId(), true));
        driverRepository.save(driver);

        return new ResponseEntity<>(new TransactionInfo<>(driver, newTransaction), HttpStatus.OK);
    }


    // A list of all the drivers in the system
    @GetMapping("/drivers")
    public ResponseEntity<?> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        if (drivers.isEmpty()) {
            return ResponseEntity.ok().body("No drivers yet");
        }

        List<AllDriversRes> response = new ArrayList<>();
        LocalDateTime dateToday = LocalDateTime.now();
        for (Driver driver : drivers) {
            List<Document> documents = documentRepository.findByUniqueDocumentId(driver.getUniqueDocumentId());
            int age = dateToday.compareTo(driver.getCreatedAt());
            response.add(new AllDriversRes(
                    driver.getId(),
                    driver.getFullName(), driver.getViews(), age, driver.getLocation(), 2, documents.get(0).getUrl(), driver.isOnline()
            ));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all-docs")
    public ResponseEntity<?> allDocs() {
        return new ResponseEntity<>(documentRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/cars")
    public ResponseEntity<?> getAllCars() {
        List<Car> cars = carRepository.findAll();
        if (cars.isEmpty()) {
            return ResponseEntity.ok().body("No Cars yet");
        }
        List<AllCarsRes> response = new ArrayList<>();
        for (Car c : cars) {
            List<Document> document = documentRepository.findByUniqueDocumentId(c.getUniqueCarImgID());
            Owner owner = ownerRepository.getById(c.getOwnerID());
            response.add(new AllCarsRes(
                    document.get(0).getUrl(),
                    c.getMake(),
                    c.getModel(),
                    "",
                    "UBER",
                    owner.getPhoneNumber(),
                    "Active",
                    String.valueOf(120)
            ));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/owners")
    public ResponseEntity<?> getAllOwners() {
        List<Owner> owners = ownerRepository.findAll();
        if (owners.isEmpty()) {
            return ResponseEntity.ok().body("No Owners yet");
        }

        return new ResponseEntity<>(owners, HttpStatus.OK);
    }

    @GetMapping("/uploaded-documents")
    public ResponseEntity<?> uploadedDocuments(@RequestParam("query") String docID) {
        List<Document> documents = documentRepository.findByUniqueDocumentId(docID);
        //List<Document> documents = documentRepository.findAll();
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }


    @PutMapping("/{driverId}/edit-driver")
    public ResponseEntity<?> editDriver(@PathVariable(value = "driverId") Long driverId,
                                        @RequestBody DriverReq driverReq) {
        Driver driver = driverRepository.getById(driverId);
        driver.setFullName(driverReq.getFullName());
        driver.setPhoneNumber(driverReq.getPhoneNumber());
        driver.setLocation(driverReq.getLocation());
        // If the admin is not changing the credit balance then keep it the same
        if (driverReq.getCreditBalance() == -1) {
            driver.setCreditBalance(driver.getCreditBalance());
        } else {
            driver.setCreditBalance(driverReq.getCreditBalance());
        }
        driverRepository.save(driver);
        return new ResponseEntity<>(driver, HttpStatus.OK);
    }


}
