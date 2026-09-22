package com.kenya.domain;
import java.time.LocalDate;

public class User {
    private int userId;
    private String name;
    private String pin;         //String to store leading 0's and PIN will be hashed to contain chars
    private LocalDate dob;     //users date of birth, using LocalDate to avoid a type mismatch with DATE column

    public User(int userId, String name, String pin, LocalDate dob) {
        this.userId = userId;
        this.name = name;
        this.pin = pin;
        this.dob = dob;
    }

    //getter methods to return user fields
    public int getUserId()  { return userId; }
    public String getName() { return name; }
    public String getPin()  { return pin; }
    public LocalDate getDob()  { return dob; }
}