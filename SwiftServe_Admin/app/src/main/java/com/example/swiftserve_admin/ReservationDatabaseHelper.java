package com.example.swiftserve_admin;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ReservationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "swiftserve.db";
    private static final int DB_VERSION = 1;

    public ReservationDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table if not exists
        db.execSQL("CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "guest_name TEXT," +
                "date TEXT," +
                "time TEXT," +
                "guests INTEGER," +
                "status TEXT," +
                "details TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS reservations");
        onCreate(db);
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM reservations ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            list.add(new Reservation(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getString(6)
            ));
        }
        cursor.close();
        return list;
    }

    public void updateStatus(int id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE reservations SET status = ? WHERE id = ?", new Object[]{newStatus, id});
    }

    // Total reservations (any status)
    public int countAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM reservations", null);
        c.moveToFirst();
        int total = c.getInt(0);
        c.close();
        return total;
    }

    // Count by exact status string
    public int countByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM reservations WHERE status = ?",
                new String[]{status});
        c.moveToFirst();
        int cnt = c.getInt(0);
        c.close();
        return cnt;
    }

    /* newest LIMIT pending reservations */
    public List<Reservation> getPendingReservation(int limit) {
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM reservations WHERE status = ? ORDER BY id DESC LIMIT ?",
                new String[]{"Pending", String.valueOf(limit)});
        while (c.moveToNext()) {
            list.add(new Reservation(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getInt(4),
                    c.getString(5),
                    c.getString(6)));
        }
        c.close();
        return list;
    }
}