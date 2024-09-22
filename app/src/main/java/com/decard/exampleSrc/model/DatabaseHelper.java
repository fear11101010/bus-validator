package com.decard.exampleSrc.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "bus_validator.db";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String BLACK_LIST_TABLE_NAME = "black_list_data";

    // Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";

    // Create Table SQL Query
    private static final String CREATE_TABLE_BLACK_LIST_DATA = "CREATE TABLE " + BLACK_LIST_TABLE_NAME +
            " (id INTEGER PRIMARY KEY AUTOINCREMENT, card_id TEXT NOT NULL,card_reason TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This method is called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table when database is created
        db.execSQL(CREATE_TABLE_BLACK_LIST_DATA);
    }

    // This method is called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + BLACK_LIST_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }
}
