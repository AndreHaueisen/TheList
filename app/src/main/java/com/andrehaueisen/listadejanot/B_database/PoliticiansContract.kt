package com.andrehaueisen.listadejanot.B_database

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri

/**
 * Created by andre on 4/16/2017.
 */
class PoliticiansContract {

    companion object{
        val AUTHORITY = "com.andrehaueisen.listadejanot.politicians"
        val BASE_CONTENT_URI : Uri = Uri.parse("content://$AUTHORITY")

        val PATH_POLITICIANS = "politicians"

        class PoliticiansEntry {

            val TABLE_NAME = "politicians_table"

            val _ID = "_id"
            val COLUMN_CARGO = "cargo"
            val COLUMN_IMAGE_URL = "image_url"
            val COLUMN_NAME = "name"
            val COLUMN_EMAIL = "email"
            val COLUMN_IMAGE = "image"

            val DEFAULT_SORT_ORDER = "$_ID DESC"

            val CONTENT_TYPE = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/$AUTHORITY/$PATH_POLITICIANS"
            val CONTENT_ITEM_TYPE = "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/$AUTHORITY/$PATH_POLITICIANS"

            val CONTENT_URI : Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POLITICIANS).build()

            fun buildUriWithId(id: Long) : Uri{
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

        }
    }
}