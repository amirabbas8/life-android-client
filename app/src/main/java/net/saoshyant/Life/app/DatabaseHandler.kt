package net.saoshyant.Life.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import java.util.HashMap


class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, "life", null, 1) {


    /**
     * Getting user data from database
     */
    // Move to first row
    // return user
    val userDetails: HashMap<String, String>
        get() {
            val user = HashMap<String, String>()
            val selectQuery = "SELECT  * FROM life"

            val db = this.readableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor.count > 0) {
                user["idNo"] = cursor.getString(1)
                user["code"] = cursor.getString(2)
                user["realname"] = cursor.getString(3)
                user["firstPage"] = cursor.getString(4)
                user["profileImage"] = cursor.getString(5)

            }
            cursor.close()
            db.close()
            return user
        }

    // return row count
    val rowCount: Int
        get() {
            val countQuery = "SELECT  * FROM life"
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            val rowCount = cursor.count
            db.close()
            cursor.close()
            return rowCount
        }


    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_LOGIN_TABLE = ("CREATE TABLE life("
                + "id INTEGER PRIMARY KEY,"
                + "idNo TEXT,"
                + "code TEXT,"
                + "realname TEXT,"
                + "firstPage TEXT,"
                + "profileImage TEXT"

                + ")")
        db.execSQL(CREATE_LOGIN_TABLE)
    }


    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if ((newVersion > oldVersion) and (oldVersion < 5)) {
            // db.execSQL("ALTER TABLE " + TABLE_LOGIN + " ADD COLUMN " + KEY_email + " TEXT");
        }

    }


    /**
     * Storing user details in database
     */
    fun addUser(idNo: String, code: String, name: String, firstPage: String, profileImage: String) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put("idNo", idNo) // idNo
        values.put("code", code) // idNo
        values.put("realname", name) //name
        values.put("firstPage", firstPage) // firstPage
        values.put("profileImage", profileImage) // profileImage

        // Inserting Row
        db.insert("life", null, values)
        db.close() // Closing database connection
    }


    /**
     * Re crate database
     * Delete all tables and create them again
     */
    fun resetTables() {
        val db = this.writableDatabase
        // Delete All Rows
        db.delete("life", null, null)
        db.close()
    }
}
