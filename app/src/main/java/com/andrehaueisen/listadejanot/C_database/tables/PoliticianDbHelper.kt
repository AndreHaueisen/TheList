package com.andrehaueisen.listadejanot.C_database.tables

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract

/**
 * Created by andre on 4/16/2017.
 */
class PoliticianDbHelper(context: Context) : SQLiteOpenHelper(context, "politicians.db", null, 1){

   /* private val DATABASE_NAME = "politicians.db"
    private val DATABASE_VERSION = 1*/

    override fun onCreate(db: SQLiteDatabase?) {

        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

        val SQL_CREATE_POLITICIANS_TABLE =
                "CREATE TABLE ${politiciansEntry.TABLE_NAME} (" +
                        "${politiciansEntry._ID} INTEGER PRIMARY KEY, " +
                        "${politiciansEntry.COLUMN_CARGO} TEXT NOT NULL, " +
                        "${politiciansEntry.COLUMN_IMAGE_URL} TEXT, " +
                        "${politiciansEntry.COLUMN_NAME} TEXT NOT NULL, " +
                        "${politiciansEntry.COLUMN_EMAIL} TEXT, " +
                        "${politiciansEntry.COLUMN_IMAGE} BLOB );"

        db?.execSQL(SQL_CREATE_POLITICIANS_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + PoliticiansContract.Companion.PoliticiansEntry().TABLE_NAME)
        onCreate(db)
    }
}