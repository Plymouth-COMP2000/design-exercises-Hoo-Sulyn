package com.example.swiftserve_admin;

public class Reservation {
    private int id;
    private String guestName;
    private String email;
    private String phone;
    private String date;
    private String time;
    private int guests;
    private String status;
    private String details;
    private String tableNum;
    private String reason;

    // Updated Constructor to include tableNum
    public Reservation(int id, String guestName, String email, String phone,
                       String date, String time, int guests, String status,
                       String details, String tableNum, String reason) {
        this.id = id;
        this.guestName = guestName;
        this.email = email;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.status = status;
        this.details = details;
        this.tableNum = tableNum;
        this.reason = reason;
    }

    // Getters
    public int getId() { return id; }
    public String getGuestName() { return guestName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getGuests() { return guests; }
    public String getStatus() { return status; }
    public String getDetails() { return details; }
    public String getReason() { return reason; }

    public String getTableNum() {
        return (tableNum == null || tableNum.isEmpty()) ? "-" : tableNum;
    }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setTableNum(String tableNum) { this.tableNum = tableNum; }
    public void setReason(String reason) { this.reason = reason; }
}