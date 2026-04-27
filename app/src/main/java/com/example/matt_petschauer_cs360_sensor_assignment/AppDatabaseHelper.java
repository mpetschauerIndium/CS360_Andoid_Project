package com.example.matt_petschauer_cs360_sensor_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles all SQLite database operations for user accounts and inventory items.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cs360_project_three.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "user_accounts";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    public static final String TABLE_INVENTORY = "inventory_items";
    public static final String COL_ID = "_id";
    public static final String COL_ITEM_NAME = "item_name";
    public static final String COL_QUANTITY = "quantity";

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USERNAME + " TEXT PRIMARY KEY, "
                + COL_PASSWORD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_INVENTORY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ITEM_NAME + " TEXT UNIQUE NOT NULL, "
                + COL_QUANTITY + " INTEGER NOT NULL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long insertedRowId = db.insert(TABLE_USERS, null, values);
        return insertedRowId != -1;
    }

    public boolean isValidLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USERNAME},
                COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );

        boolean hasMatch = cursor.moveToFirst();
        cursor.close();
        return hasMatch;
    }

    public boolean addItem(String itemName, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_QUANTITY, quantity);
        return db.insert(TABLE_INVENTORY, null, values) != -1;
    }

    public boolean updateItem(String itemName, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_QUANTITY, quantity);
        return db.update(TABLE_INVENTORY, values, COL_ITEM_NAME + " = ?", new String[]{itemName}) > 0;
    }

    public boolean deleteItem(String itemName) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_INVENTORY, COL_ITEM_NAME + " = ?", new String[]{itemName}) > 0;
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_INVENTORY,
                new String[]{COL_ID, COL_ITEM_NAME, COL_QUANTITY},
                null,
                null,
                null,
                null,
                COL_ITEM_NAME + " ASC"
        );
    }
}
