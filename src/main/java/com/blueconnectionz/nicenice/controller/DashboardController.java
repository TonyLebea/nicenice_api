package com.blueconnectionz.nicenice.controller;


import com.blueconnectionz.nicenice.model.*;
import com.blueconnectionz.nicenice.payload.res.CarInfo;
import com.blueconnectionz.nicenice.payload.res.DashboardRes;
import com.blueconnectionz.nicenice.payload.res.DriverTable;
import com.blueconnectionz.nicenice.payload.res.LatestTransactionsRes;
import com.blueconnectionz.nicenice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/dashboard")

public class DashboardController {


    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DriverRepository driverRepository;
    @Autowired
    OwnerRepository ownerRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DealRepository dealRepository;
    @Autowired
    TransactionsRepository transactionsRepository;
    @Autowired
    CarRepository carRepository;

    // Dashboard page data
    @GetMapping("/overview")
    public ResponseEntity<?> overview() {

        List<Driver> approvedDrivers = driverRepository.findByApproved(true);
        List<Owner> approvedOwners = ownerRepository.findByApproved(true);
        List<User> users = userRepository.findAll();
        List<Deal> deals = dealRepository.findAll();
        int newOwners = 0;
        int newDrivers = 0;

        LocalDateTime dateToday = LocalDateTime.now();

        for (User user : users) {
            long daysDiff = ChronoUnit.DAYS.between(dateToday, user.getCreatedAt());
            if (user.getRole().equals(Role.DRIVER) && daysDiff < 7) {
                newDrivers++;
            } else if (user.getRole().equals(Role.OWNER) && daysDiff < 7) {
                newOwners++;
            }
        }

        double totalRevenue = 0.0;
        for (Transactions t : transactionsRepository.findAll()) {
            if (t.getAmount() < 0) {
                // In case the number is a negative
                totalRevenue += Math.abs(t.getAmount());
            } else {
                totalRevenue += t.getAmount();
            }
        }
        Locale SOUTH_AFRICA = new Locale("en", "ZA");
        NumberFormat randFormat = NumberFormat.getCurrencyInstance(SOUTH_AFRICA);

        DashboardRes response = new DashboardRes(
                newOwners,
                newDrivers,
                users.size(),
                approvedOwners.size(),
                approvedDrivers.size(),
                transactionsRepository.findAll().size(),
                0,
                0,
                randFormat.format(totalRevenue)
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/all-transactions")
    public ResponseEntity<?> allTransactions() {
        List<Transactions> transactions = transactionsRepository.findAll();
        List<LatestTransactionsRes> transactionsRes = new ArrayList<>(10);

        for (Transactions t : transactions) {
            User user = userRepository.getById(t.getUserID());
            /*
             * Check the transaction type
             * If positive then it was a top up
             * If negative then the credits were spent
             */
            String type;
            if (t.getAmount() < 0) {
                type = user.getEmail() + " spent credits";

            } else {
                type = "Payment from " + user.getEmail();
            }

            transactionsRes.add(new LatestTransactionsRes(
                    t.getId(),
                    type,
                    t.getCreatedAt(),
                    t.getAmount()));
        }

        return new ResponseEntity<>(transactionsRes, HttpStatus.OK);
    }


    // car Table
    @GetMapping("/car-table")
    public ResponseEntity<?> getCarTableData() {
        List<Car> cars = carRepository.findAll();
        List<CarInfo> carInfo = new ArrayList<>();
        for (Car c : cars) {
            List<Document> documents = documentRepository.findByUniqueDocumentId(c.getUniqueCarImgID());
            List<Deal> deals = dealRepository.findByCarID(c.getId());
            carInfo.add(new CarInfo(
                    c.getId(),
                    documents.get(0).getUrl(),
                    c.getMake(),
                    c.getModel(),
                    c.getYear(),
                    c.getCity(),
                    c.getWeeklyTarget(),
                    c.isDepositRequired(),
                    c.isHasInsurance(),
                    c.isHasTracker(),
                    c.isActiveOnHailingPlatforms(),
                    deals.size(),
                    c.getAge(),
                    c.getViews()
            ));
        }
        return new ResponseEntity<>(carInfo, HttpStatus.OK);
    }


    // Driver Table
    @GetMapping("/driver-table")
    public ResponseEntity<?> getDriverTableData() {
        List<Driver> drivers = driverRepository.findAll();
        List<DriverTable> driverTables = new ArrayList<>();
        for (Driver d : drivers) {
            driverTables.add(new DriverTable(
                    d.getId(),
                    d.getFullName(),
                    d.getUser().getEmail(),
                    d.getPhoneNumber(),
                    d.getLocation(),
                    d.isApproved(),
                    d.getCreditBalance(),
                    d.getReference1(),
                    d.getReference2(),
                    d.getUniqueDocumentId()
            ));
        }
        return new ResponseEntity<>(driverTables, HttpStatus.OK);
    }


    // Owner Table
    @GetMapping("/owner-table")
    public ResponseEntity<?> getOwnerTableData(){
        return null;
    }

}
