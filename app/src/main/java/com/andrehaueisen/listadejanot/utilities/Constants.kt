package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.B_database.PoliticiansContract

/**
 * Created by andre on 4/15/2017.
 */
class Constants{

    companion object{

        private val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

        @JvmField val LOADER_ID = 0
        @JvmField val POLITICIANS_COLUMNS = arrayOf(
                politiciansEntry.COLUMN_CARGO,
                politiciansEntry.COLUMN_NAME,
                politiciansEntry.COLUMN_EMAIL,
                politiciansEntry.COLUMN_IMAGE)

        @JvmField val COLUMNS_INDEX_CARGO = 0
        @JvmField val COLUMNS_INDEX_NAME = 1
        @JvmField val COLUMNS_INDEX_EMAIL = 2
        @JvmField val COLUMNS_INDEX_IMAGE = 3

        @JvmField val BUNDLE_DEPUTADOS = "deputados_list"
        @JvmField val BUNDLE_SENADORES = "senadores_list"
        @JvmField val BUNDLE_MANAGER = "layout_manager_state"
        @JvmField val BUNDLE_PAGER_ADAPTER = "pager_adapter"
    }
}