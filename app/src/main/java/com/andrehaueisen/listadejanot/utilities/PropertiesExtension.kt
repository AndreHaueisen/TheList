package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.c_database.PoliticiansContract

val INTENT_DEPUTADOS_MAIN_LIST = "deputados_main_list"
val INTENT_SENADORES_MAIN_LIST = "senadores_main_list"
val INTENT_POLITICIAN_NAME = "intent_politician_name"
val BUNDLE_SEARCHABLE_POLITICIANS = "politicians_list"
val BUNDLE_DEPUTADOS = "deputados_list"
val BUNDLE_SENADORES = "senadores_list"
val BUNDLE_MANAGER = "layout_manager_state"
val BUNDLE_PAGER_ADAPTER = "pager_adapter"
val BUNDLE_POLITICIAN_NAME = "politician_name"
val BUNDLE_POLITICIAN = "politician"
val BUNDLE_USER_VOTES_LIST = "politicians_list"
val BUNDLE_USER = "user"

val LOCATION_UID_MAPPINGS = "UID_mappings"
val LOCATION_MESSAGE_TOKENS = "Message_tokens"
val LOCATION_DEPUTADOS_MAIN_LIST = "Deputados_main_list"
val LOCATION_SENADORES_MAIN_LIST = "Senadores_main_list"
val LOCATION_DEPUTADOS_PRE_LIST = "Deputados_pre_list"
val LOCATION_SENADORES_PRE_LIST = "Senadores_pre_list"
val LOCATION_USERS = "Users"
val LOCATION_VOTE_COUNT = "Vote_count"
val CHILD_LOCATION_IS_ON_MAIN_LIST = "isOnMainList"
val CHILD_LOCATION_VOTES_NUMBER = "votesNumber"
val CHILD_LOCATION_CONDEMNED_BY = "condemnedBy"
val CHILD_LOCATION_CONDEMNATIONS = "condemnations"

val VOTES_TO_MAIN_LIST_THRESHOLD: Long = 1
val DEFAULT_ANIMATIONS_DURATION: Long = 500
val QUICK_ANIMATIONS_DURATION: Long = 250
val VERY_QUICK_ANIMATIONS_DURATION: Long = 100

val SHARED_PREFERENCES = "com_andre_haueisen_shared_pref"
val SHARED_MESSAGE_TOKEN = "message_token"

val DEFAULT_POLITICIANS_MAIN_LIST = listOf(
        "dep.luciovieiralima@camara.leg.br",
        "dep.rodrigomaia@camara.leg.br",
        "dep.rodrigorochaloures@camara.gov.br",
        "edison.lobao@senador.leg.br",
        "lindbergh.farias@senador.leg.br",
        "renan.calheiros@senador.leg.br",
        "romero.juca@senador.leg.br",
        "aecio.neves@senador.leg.br")

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

val POLITICIANS_COLUMNS_NAME_NO_EMAIL = arrayOf(
        politiciansEntry.COLUMN_POST,
        politiciansEntry.COLUMN_NAME,
        politiciansEntry.COLUMN_IMAGE)

val PERMISSION_WRITE_EXTERNAL_STORAGE: Int = 10

