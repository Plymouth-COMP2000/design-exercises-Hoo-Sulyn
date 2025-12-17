package com.example.swiftserve_admin;

public class User {
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private String contact;
    private String usertype;

    // Full Constructor
    public User(String username, String password, String firstname, String lastname, String email, String contact, String usertype) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.contact = contact;
        this.usertype = usertype;
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getEmail() { return email; }
    public String getContact() { return contact; }
    public String getUsertype() { return usertype; }
}