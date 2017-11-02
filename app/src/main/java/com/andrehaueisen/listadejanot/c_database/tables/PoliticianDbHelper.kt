package com.andrehaueisen.listadejanot.c_database.tables

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.andrehaueisen.listadejanot.BuildConfig
import com.andrehaueisen.listadejanot.utilities.SHARED_VERSION_CODE
import com.andrehaueisen.listadejanot.utilities.pullIntFromSharedPreferences
import com.andrehaueisen.listadejanot.utilities.putValueOnSharedPreferences
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by andre on 4/16/2017.
 */
class PoliticianDbHelper(val context: Context) : SQLiteOpenHelper(context, "politicians.db", null, 1) {

    private val DB_NAME = "politicians.db"
    private val DB_PATH: String = "${context.applicationInfo.dataDir}/databases/"
    private var mDataBase: SQLiteDatabase? = null

    @Throws(IOException::class)
    fun createDataBase() {
        //If the database does not exist, copy it from the assets.
        val lastVersionCode = context.pullIntFromSharedPreferences(SHARED_VERSION_CODE)
        val currentVersionCode = BuildConfig.VERSION_CODE

        val mDataBaseExist = checkDataBase()
        if (!mDataBaseExist || lastVersionCode != currentVersionCode) {
            this.readableDatabase
            this.close()
            try {
                //Copy the database from assets
                copyDataBase()
                context.putValueOnSharedPreferences(SHARED_VERSION_CODE, currentVersionCode)
                Log.i("PoliticianDbHelper", "createDatabase database created")
            } catch (mIOException: IOException) {
                throw Error("ErrorCopyingDataBase")
            }

        }
    }

    private fun checkDataBase(): Boolean {
        val dbFile = File(DB_PATH + DB_NAME)
        Log.i("PoliticianDbHelper", "$dbFile   ${dbFile.exists()}")
        return dbFile.exists()
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val mInput = context.assets.open(DB_NAME)
        val outFileName = DB_PATH + DB_NAME
        val mOutput = FileOutputStream(outFileName, false)
        val mBuffer = ByteArray(1024)
        var mLength: Int
        mLength = mInput.read(mBuffer)

        while (mLength > 0) {
            mOutput.write(mBuffer, 0, mLength)
            mLength = mInput.read(mBuffer)
        }

        mOutput.flush()
        mOutput.close()
        mInput.close()
    }

    //Open the database, so we can query it
    @Throws(SQLException::class)
    fun openDataBase(): Boolean {
        val mPath = DB_PATH + DB_NAME
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return mDataBase != null
    }

    @Synchronized override fun close() {
        if (mDataBase != null) {
            mDataBase?.close()
        }
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase?) = Unit

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}