package com.example.swiftserve_admin;

public class Reservation {
    private int id;
    private String guestName;
    private String date;
    private String time;
    private int guests;
    private String status;
    private String details;

    public Reservation(int id, String guestName, String date, String time, int guests, String status, String details) {
        this.id = id;
        this.guestName = guestName;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.status = status;
        this.details = details;
    }

    public int getId() { return id; }
    public String getGuestName() { return guestName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getGuests() { return guests; }
    public String getStatus() { return status; }
    public String getDetails() { return details; }

    public void setStatus(String status) { this.status = status; }
}