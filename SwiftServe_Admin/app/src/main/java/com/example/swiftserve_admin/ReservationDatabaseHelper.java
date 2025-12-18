package com.example.swiftserve_admin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ReservationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "swiftserve.db";
    private static final int DB_VERSION = 5;

    public ReservationDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "guest_name TEXT," +
                "email TEXT," +
                "phone TEXT," +
                "date TEXT," +
                "time TEXT," +
                "guests INTEGER," +
                "status TEXT," +
                "details TEXT," +
                "table_number TEXT," +
                "reason TEXT," +
                "last_updated DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drops old table and creates the new one with more columns
        db.execSQL("DROP TABLE IF EXISTS reservations");
        onCreate(db);
    }

    // Update your addReservation to accept email and phone
    public long addReservation(String name, String email, String phone, String date, String time, int guests, String status, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();

        values.put("guest_name", name);
        values.put("email", email); // Save Email
        values.put("phone", phone); // Save Phone
        values.put("date", date);
        values.put("time", time);
        values.put("guests", guests);
        values.put("status", status);
        values.put("details", details);
        values.put("table_number", "-");

        return db.insert("reservations", null, values);
    }

    // Update getAllReservations to match your new Reservation Model constructor
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM reservations ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            list.add(new Reservation(
                    cursor.getInt(0),  // id
                    cursor.getString(1), // name
                    cursor.getString(2), // email
                    cursor.getString(3), // phone
                    cursor.getString(4), // date
                    cursor.getString(5), // time
                    cursor.getInt(6),    // guests
                    cursor.getString(7), // status
                    cursor.getString(8),  // details
                    cursor.getString(9),  // table_number
                    cursor.getString(10)  // reason
            ));
        }
        cursor.close();
        return list;
    }

    public void updateStatus(int id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();

        // We are updating the "status" column for the specific ID
        values.put("status", newStatus);

        // This runs: UPDATE reservations SET status = 'Accepted' WHERE id = 5
        db.update("reservations", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Total reservations
    public int countAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM reservations", null);
        int total = 0;
        if (c.moveToFirst()) {
            total = c.getInt(0);
        }
        c.close();
        return total;
    }

    // Count reservations by their specific status (e.g., "Pending", "Accepted", "Declined")
    public int countByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Use a parameterized query to prevent SQL injection and handle strings correctly
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM reservations WHERE status = ?",
                new String[]{status});

        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    // Fetches the most recent pending reservations for the Dashboard table
    public List<Reservation> getPendingReservation(int limit) {
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query: Get all columns where status is Pending, newest first, up to the limit
        Cursor c = db.rawQuery(
                "SELECT * FROM reservations WHERE status = ? ORDER BY id DESC LIMIT ?",
                new String[]{"Pending", String.valueOf(limit)});

        if (c.moveToFirst()) {
            do {
                list.add(new Reservation(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("guest_name")),
                        c.getString(c.getColumnIndexOrThrow("email")),
                        c.getString(c.getColumnIndexOrThrow("phone")),
                        c.getString(c.getColumnIndexOrThrow("date")),
                        c.getString(c.getColumnIndexOrThrow("time")),
                        c.getInt(c.getColumnIndexOrThrow("guests")),
                        c.getString(c.getColumnIndexOrThrow("status")),
                        c.getString(c.getColumnIndexOrThrow("details")),
                        c.getString(c.getColumnIndexOrThrow("table_number")),
                        c.getString(c.getColumnIndexOrThrow("reason")) // Safe way to get col 10
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void updateStatusWithTable(int id, String status, String tableNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        cv.put("table_number", tableNo);

        db.update("reservations", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void updateStatusWithReason(int id, String status, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("reason", reason); // Ensure your table has a 'reason' column

        db.update("reservations", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<String> getOccupiedTables() {
        List<String> occupied = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Only get tables from reservations that are currently "Accepted"
        Cursor cursor = db.rawQuery("SELECT table_number FROM reservations WHERE status = 'Accepted'", null);

        if (cursor.moveToFirst()) {
            do {
                String tables = cursor.getString(0);
                if (tables != null) {
                    // If multiple tables were selected (e.g., "Table 1, Table 2"), split them
                    String[] splitTables = tables.split(", ");
                    for (String t : splitTables) {
                        occupied.add(t.trim());
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return occupied;
    }

    public List<Reservation> getHistoryByEmail(String email) {
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM reservations WHERE email = ? ORDER BY id DESC", new String[]{email});
        while (cursor.moveToNext()) {
            list.add(new Reservation(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5),
                    cursor.getInt(6), cursor.getString(7), cursor.getString(8),
                    cursor.getString(9), cursor.getString(10)
            ));
        }
        cursor.close();
        return list;
    }

    public void deleteHistoryByEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("reservations", "email = ?", new String[]{email});
        db.close();
    }

    public void deleteReservation(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("reservations", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Reservation getReservationById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM reservations WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            Reservation res = new Reservation(
                    cursor.getInt(0),    // id
                    cursor.getString(1), // guest_name
                    cursor.getString(2), // email
                    cursor.getString(3), // phone
                    cursor.getString(4), // date
                    cursor.getString(5), // time
                    cursor.getInt(6),    // guests
                    cursor.getString(7), // status
                    cursor.getString(8), // details
                    cursor.getString(9), // table_number
                    cursor.getString(10) // reason
            );
            cursor.close();
            return res;
        }
        return null;
    }

    public void fullUpdateReservation(long id, String name, String email, String phone, String date, String time, int guests, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();

        cv.put("guest_name", name);
        cv.put("email", email);
        cv.put("phone", phone);
        cv.put("date", date);
        cv.put("time", time);
        cv.put("guests", guests);
        cv.put("details", details);
        cv.put("status", "Pending");

        // Update the timestamp so the Polling service detects the change
        cv.put("last_updated", System.currentTimeMillis());

        db.update("reservations", cv, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public String getLatestEditTimestamp() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT MAX(last_updated) FROM reservations", null);
        String timestamp = "";
        if (cursor.moveToFirst()) {
            timestamp = cursor.getString(0);
        }
        cursor.close();
        return timestamp != null ? timestamp : "";
    }

    // Get status for a specific guest
    // Get status for a specific guest
    public String getReservationStatus(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        // CHANGED 'guest_email' TO 'email' TO MATCH YOUR TABLE
        Cursor cursor = db.rawQuery("SELECT status FROM reservations WHERE email = ? ORDER BY id DESC LIMIT 1", new String[]{email});

        String status = "None"; // Default if nothing found
        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(0);
            cursor.close();
        }
        return status;
    }

    // Count total menu items
    public int getMenuCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM menu", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}