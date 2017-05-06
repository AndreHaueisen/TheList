package com.andrehaueisen.listadejanot.C_database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import com.andrehaueisen.listadejanot.C_database.tables.PoliticianDbHelper

/**
 * Created by andre on 4/16/2017.
 */
class PoliticiansContentProvider : ContentProvider() {

    private val LOG_TAG = PoliticiansContentProvider::class.java.simpleName

    private val POLITICIANS = 100
    private val POLITICIAN_SPECIFIC = 101

    private val mUriMatcher = buildUriMatcher()
    lateinit private var mPoliticiansDbHelper: PoliticianDbHelper

    private fun buildUriMatcher(): UriMatcher {

        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher.addURI(PoliticiansContract.AUTHORITY, PoliticiansContract.PATH_POLITICIANS, POLITICIANS)
        matcher.addURI(PoliticiansContract.AUTHORITY, "${PoliticiansContract.PATH_POLITICIANS}/#", POLITICIAN_SPECIFIC)

        return matcher
    }

    override fun onCreate(): Boolean {
        mPoliticiansDbHelper = PoliticianDbHelper(context)
        mPoliticiansDbHelper.createDataBase()
        return true
    }

    override fun getType(uri: Uri?): String {
        val match = mUriMatcher.match(uri)

        when (match) {
            POLITICIANS -> return PoliticiansContract.Companion.PoliticiansEntry().CONTENT_TYPE
            POLITICIAN_SPECIFIC -> return PoliticiansContract.Companion.PoliticiansEntry().CONTENT_ITEM_TYPE
            else -> throw UnsupportedOperationException("Uri unknown: ${uri.toString()}")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val sqLiteDatabase: SQLiteDatabase
        val returnUri: Uri
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

        val match = mUriMatcher.match(uri)

        when (match) {

            POLITICIANS ->
                try {
                    sqLiteDatabase = mPoliticiansDbHelper.writableDatabase
                    val id = sqLiteDatabase.insert(politiciansEntry.TABLE_NAME, null, values)

                    if (id > 0) {
                        returnUri = politiciansEntry.buildUriWithId(id)
                        Log.i(LOG_TAG, "Insert on $uri")
                    } else {
                        throw SQLException("Failed insert into $uri")
                    }
                } catch (sqlE: SQLException) {
                    Log.e(LOG_TAG, "Failed insert into $uri")
                    return null
                }

            else -> throw UnsupportedOperationException("Unknown uri: " + uri)

        }

        context.contentResolver.notifyChange(uri, null)
        Log.i(LOG_TAG, "Item inserted: $returnUri")
        return returnUri
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val sqLiteDatabase: SQLiteDatabase
        val queryBuilder = SQLiteQueryBuilder()
        val cursor: Cursor
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        var mutableSortOrder = sortOrder

        if(mutableSortOrder == null){
            mutableSortOrder = politiciansEntry.DEFAULT_SORT_ORDER
        }

        val match = mUriMatcher.match(uri)

        when(match){
            POLITICIANS -> {
                sqLiteDatabase = mPoliticiansDbHelper.readableDatabase
                cursor = sqLiteDatabase.query(politiciansEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null)
            }

            POLITICIAN_SPECIFIC -> {
                sqLiteDatabase = mPoliticiansDbHelper.readableDatabase
                queryBuilder.appendWhere("${politiciansEntry._ID}=${uri.lastPathSegment}")
                cursor = queryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, mutableSortOrder)
            }

            else -> throw UnsupportedOperationException("Uri unknown: $uri")
        }
        cursor.setNotificationUri(context.contentResolver, uri)

        return cursor
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val sqLiteDatabase: SQLiteDatabase
        val id: String
        var where: String
        val match = mUriMatcher.match(uri)
        val rowsUpdated: Int
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

        when(match){
            POLITICIANS -> {
                sqLiteDatabase = mPoliticiansDbHelper.writableDatabase
                rowsUpdated = sqLiteDatabase.update(politiciansEntry.TABLE_NAME, values, selection, selectionArgs)
            }

            POLITICIAN_SPECIFIC ->
            {
                sqLiteDatabase = mPoliticiansDbHelper.writableDatabase
                id = uri.lastPathSegment
                where = "${politiciansEntry._ID}=$id"
                if (!selection.isNullOrEmpty()){
                    where += " AND $selection"
                }
                rowsUpdated = sqLiteDatabase.update(politiciansEntry.TABLE_NAME, values, where, selectionArgs)
            }

            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        if (rowsUpdated != 0){
            context.contentResolver.notifyChange(uri, null)
        }

        return rowsUpdated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val sqLiteDatabase: SQLiteDatabase
        val id: String
        var where: String
        val rowsDeleted: Int
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

        val match = mUriMatcher.match(uri)

        when(match){
            POLITICIANS -> {
                sqLiteDatabase = mPoliticiansDbHelper.writableDatabase
                rowsDeleted = sqLiteDatabase.delete(politiciansEntry.TABLE_NAME, selection, selectionArgs)
            }

            POLITICIAN_SPECIFIC ->{
                sqLiteDatabase = mPoliticiansDbHelper.writableDatabase
                id = uri.lastPathSegment
                where = "${politiciansEntry._ID}= $id"
                if(!selection.isNullOrEmpty()){
                    where += " AND $selection"
                }
                rowsDeleted = sqLiteDatabase.delete(politiciansEntry.TABLE_NAME, where, selectionArgs)
            }

            else -> throw UnsupportedOperationException("Unknown uri: " + uri)

        }

        if (rowsDeleted != 0){
            context.contentResolver.notifyChange(uri, null)
            Log.i(LOG_TAG, "Deletion successful: $rowsDeleted rows deleted")
        }

        return rowsDeleted
    }

    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        val sqLiteDatabase: SQLiteDatabase
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        val match = mUriMatcher.match(uri)
        var numberOfInsertedItems = 0

        when(match){
            POLITICIANS ->{
                sqLiteDatabase = mPoliticiansDbHelper.writableDatabase
                sqLiteDatabase.beginTransaction()
                try {
                    values.map{
                        val id = sqLiteDatabase.insert(politiciansEntry.TABLE_NAME, null, it)
                        if(id != -1L){
                            numberOfInsertedItems++
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful()
                }finally {
                    sqLiteDatabase.endTransaction()
                }
                context.contentResolver.notifyChange(uri, null)
                Log.i(LOG_TAG, "BulkInsert successful: $numberOfInsertedItems rows inserted")
                return numberOfInsertedItems
            }

            else -> return super.bulkInsert(uri, values)
        }
    }
}