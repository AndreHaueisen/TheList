package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.C_database.PoliticiansContract

val INTENT_DEPUTADOS_MAIN_LIST = "deputados_main_list"
val INTENT_SENADORES_MAIN_LIST = "senadores_main_list"
val BUNDLE_SEARCHABLE_POLITICIANS = "politicians_list"
val BUNDLE_DEPUTADOS = "deputados_list"
val BUNDLE_SENADORES = "senadores_list"
val BUNDLE_MANAGER = "layout_manager_state"
val BUNDLE_PAGER_ADAPTER = "pager_adapter"
val BUNDLE_POLITICIAN_NAME = "politician_name"
val BUNDLE_POLITICIAN = "politician"

val LOCATION_UID_MAPPINGS = "UID_mappings"
val LOCATION_USERS_DATA = "Users_data"
val LOCATION_DEPUTADOS_MAIN_LIST = "Deputados_main_list"
val LOCATION_SENADORES_MAIN_LIST = "Senadores_main_list"
val LOCATION_DEPUTADOS_PRE_LIST = "Deputados_pre_list"
val LOCATION_SENADORES_PRE_LIST = "Senadores_pre_list"
val LOCATION_USERS_CONDEMNATIONS = "Users_condemnations"
val CHILD_LOCATION_VOTES_NUMBER = "votesNumber"
val CHILD_LOCATION_CONDEMNED_BY = "condemnedBy"
val CHILD_LOCATION_CONDEMNATIONS = "condemnations"


val FAKE_USER_EMAIL = "fakeEmailUser@ba.com"

val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

val LOADER_ID = 0
val POLITICIANS_COLUMNS = arrayOf(
        politiciansEntry.COLUMN_POST,
        politiciansEntry.COLUMN_NAME,
        politiciansEntry.COLUMN_EMAIL,
        politiciansEntry.COLUMN_IMAGE)

val POLITICIANS_COLUMNS_NO_IMAGE = arrayOf(
        politiciansEntry.COLUMN_POST,
        politiciansEntry.COLUMN_NAME,
        politiciansEntry.COLUMN_EMAIL)

