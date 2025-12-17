package com.example.swiftserve_admin;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String description; // CHANGED from category
    private String status;

    public MenuItem() {}

    public MenuItem(int id, String name, double price, String description, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description; // CHANGED
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; } // CHANGED
    public void setDescription(String description) { this.description = description; } // CHANGED

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}