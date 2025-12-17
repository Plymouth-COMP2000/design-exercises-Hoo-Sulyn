package com.example.swiftserve_admin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class MenuDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "menu.db";
    private static final int DATABASE_VERSION = 2; // INCREASE VERSION

    public static final String TABLE_MENU = "menu";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_PRICE = "price";
    public static final String COL_DESCRIPTION = "description"; // CHANGED FROM CATEGORY
    public static final String COL_STATUS = "status";
    public static final String COL_IMAGE = "image_path";

    public MenuDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MENU_TABLE =
                "CREATE TABLE " + TABLE_MENU + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT, " +
                        COL_PRICE + " REAL, " +
                        COL_DESCRIPTION + " TEXT, " +
                        COL_STATUS + " TEXT, " +
                        COL_IMAGE + " TEXT" +
                        ")";
        db.execSQL(CREATE_MENU_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If upgrading from version 1 to 2
        if (oldVersion < 2) {
            // Create new table with correct structure
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
            onCreate(db);
        }
    }

    public long insertMenuItem(String name, double price, String description, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_PRICE, price);
        values.put(COL_DESCRIPTION, description); // CHANGED
        values.put(COL_STATUS, status);

        return db.insert(TABLE_MENU, null, values);
    }

    // Get all menu items
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MENU,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                MenuItem item = new MenuItem();
                item.setId(cursor.getInt(0));
                item.setName(cursor.getString(1));
                item.setPrice(cursor.getDouble(2));
                item.setDescription(cursor.getString(3)); // CHANGED
                item.setStatus(cursor.getString(4));
                menuList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return menuList;
    }

    // Get single menu item by ID
    public MenuItem getMenuItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MENU,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            MenuItem item = new MenuItem(
                    cursor.getInt(0),          // id
                    cursor.getString(1),       // name
                    cursor.getDouble(2),       // price
                    cursor.getString(3),       // description (CHANGED)
                    cursor.getString(4)        // status
            );
            cursor.close();
            db.close();
            return item;
        }

        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    // Update menu item
    public int updateMenuItem(int id, String name, double price, String description, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_PRICE, price);
        values.put(COL_DESCRIPTION, description); // CHANGED
        values.put(COL_STATUS, status); // CHANGED: use constant

        return db.update(TABLE_MENU, values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Delete menu item
    public void deleteMenuItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MENU, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }


    public long insertMenuItem(String name, double price, String description,
                               String status, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_PRICE, price);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_STATUS, status);
        values.put(COL_IMAGE, imagePath); // NEW

        return db.insert(TABLE_MENU, null, values);
    }

    public int getMenuCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MENU, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    public int updateMenuItemStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);

        return db.update(TABLE_MENU, values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}

