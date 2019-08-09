package com.sendy.googlemaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="places.db";
    public static final String TABLE_NAME="places";
    public static final String COL_1_ID= "id";
    public static final String COL_2_Name = "names";
    public static final String COL_3_Email = "emails";
    public static final String COL_4_Password = "passwords";
    public static final String COL_5_Places = "visitedplaces";

    // create table sql query
    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COL_1_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_2_Name + " TEXT,"
            + COL_3_Email + " TEXT," + COL_4_Password + " TEXT," + COL_5_Places + " TEXT" + ")";

    // drop table sql query
    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_USER_TABLE);
        //db.execSQL("create table "+TABLE_NAME+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT, names TEXT, emails TEXT, passwords TEXT,visitedplaces TEXT)");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE);

        // Create tables again
        onCreate(db);

    }
                //This method is to create user record
    public void addUser(String names, String emails, String passwords, String visitedplaces){
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_2_Name, names);
            values.put(COL_3_Email, emails);
            values.put(COL_4_Password,passwords);
            values.put(COL_5_Places,visitedplaces);

            // Inserting Row
            db.insert(TABLE_NAME, null, values);
            db.close();
    }

    /**
     * This method to check user exist or not
     *
     * @param emails
     * @return true/false
     */
    public boolean checkUser(String emails){
        // array of columns to fetch
        String[] columns = {
                COL_1_ID
        };

        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = COL_3_Email + " = ?";

        // selection argument
        String[] selectionArgs = {emails};

        // query user table with condition
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com';
         */
        Cursor cursor = db.query(TABLE_NAME, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                      //filter by row groups
                null);                      //The sort order
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        if (cursorCount > 0){
            return true;
        }
        return false;

    }


    /**
     * This method to check user exist or not
     *
     * @param emails
     * @param passwords
     * @return true/false
     */
    public boolean checkUser(String emails, String passwords){
        // array of columns to fetch
        String[] columns = {
                COL_1_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = COL_3_Email + " = ?" + " AND " + COL_4_Password + " = ?";

        // selection arguments
        String[] selectionArgs = {emails, passwords};
        // query user table with conditions
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com' AND user_password = 'qwerty';
         */
        Cursor cursor = db.query(TABLE_NAME, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                       //filter by row groups
                null);                      //The sort order

        int cursorCount = cursor.getCount();

        cursor.close();
        db.close();
        if (cursorCount > 0) {
            return true;
        }

        return false;
    }

}
