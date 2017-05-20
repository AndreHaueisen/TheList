package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.C_database.PoliticiansContract

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

        @JvmField val POLITICIANS_COLUMNS_NO_IMAGE = arrayOf(
                politiciansEntry.COLUMN_CARGO,
                politiciansEntry.COLUMN_NAME,
                politiciansEntry.COLUMN_EMAIL)

        @JvmField val POLITICIANS_COLUMNS_IMAGE = arrayOf(
                politiciansEntry.COLUMN_NAME,
                politiciansEntry.COLUMN_IMAGE)

        @JvmField val INTENT_DEPUTADOS_MAIN_LIST = "deputados_main_list"
        @JvmField val INTENT_SENADORES_MAIN_LIST = "senadores_main_list"
        @JvmField val BUNDLE_SEARCHABLE_POLITICIANS = "politicians_list"
        @JvmField val BUNDLE_DEPUTADOS = "deputados_list"
        @JvmField val BUNDLE_SENADORES = "senadores_list"
        @JvmField val BUNDLE_MANAGER = "layout_manager_state"
        @JvmField val BUNDLE_PAGER_ADAPTER = "pager_adapter"
        @JvmField val BUNDLE_POLITICIAN_NAME = "politician_name"
        @JvmField val BUNDLE_PAIR_NAME_IMAGE = "pair_name_image"

        @JvmField val LOCATION_UID_MAPPINGS = "UID_Mappings"
        @JvmField val LOCATION_USERS = "Users"
        @JvmField val LOCATION_DEPUTADOS_MAIN_LIST = "Deputados_main_list"
        @JvmField val LOCATION_SENADORES_MAIN_LIST = "Senadores_main_list"
        @JvmField val LOCATION_DEPUTADOS_PRE_LIST = "Deputados_pre_list"
        @JvmField val LOCATION_SENADORES_PRE_LIST = "Senadores_pre_list"
        @JvmField val CHILD_LOCATION_VOTES_NUMBER = "votesNumber"
        @JvmField val CHILD_LOCATION_CONDEMNED_BY = "condemnedBy"
        @JvmField val CHILD_LOCATION_CONDEMNATIONS = "condemnations"
    }
}