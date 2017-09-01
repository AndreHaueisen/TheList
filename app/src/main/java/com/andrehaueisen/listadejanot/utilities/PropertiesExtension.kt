package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.c_database.PoliticiansContract

val INTENT_DEPUTADOS_MAIN_LIST = "deputados_main_list"
val INTENT_SENADORES_MAIN_LIST = "senadores_main_list"
val INTENT_GOVERNADORES_MAIN_LIST = "governadores_main_list"
val INTENT_POLITICIAN_NAME = "intent_politician_name"
val BUNDLE_SEARCHABLE_POLITICIANS = "politicians_list"
val BUNDLE_DEPUTADOS = "deputados_list"
val BUNDLE_SENADORES = "senadores_list"
val BUNDLE_GOVERNADORES = "governadores_list"
val BUNDLE_MANAGER = "layout_manager_state"
val BUNDLE_PAGER_ADAPTER = "pager_adapter"
val BUNDLE_POLITICIAN_NAME = "politician_name"
val BUNDLE_POLITICIAN = "politician"
val BUNDLE_USER_VOTES_LIST = "politicians_list"
val BUNDLE_USER = "user"
val BUNDLE_POLITICIAN_EMAIL = "politician_email"
val BUNDLE_POLITICIAN_IMAGE = "politician_image"
val BUNDLE_USER_EMAIL = "user_email"
val BUNDLE_USER_OPINION = "user_opinion"

val LOCATION_UID_MAPPINGS = "UID_mappings"
val LOCATION_MESSAGE_TOKENS = "Message_tokens"
val LOCATION_DEPUTADOS_MAIN_LIST = "Deputados_main_list"
val LOCATION_SENADORES_MAIN_LIST = "Senadores_main_list"
val LOCATION_GOVERNADORES_MAIN_LIST = "Governadores_main_list"
val LOCATION_DEPUTADOS_PRE_LIST = "Deputados_pre_list"
val LOCATION_SENADORES_PRE_LIST = "Senadores_pre_list"
val LOCATION_GOVERNADORES_PRE_LIST = "Governadores_pre_list"
val LOCATION_USERS = "Users"
val LOCATION_VOTE_COUNT = "Vote_count"
val LOCATION_OPINIONS_ON_POLITICIANS = "Opinions_on_politicians"
val LOCATION_MINIMUM_VOTES_FOR_MAIN_LIST = "Min_votes_for_main_list"
val CHILD_LOCATION_IS_ON_MAIN_LIST = "isOnMainList"
val CHILD_LOCATION_VOTES_NUMBER = "votesNumber"
val CHILD_LOCATION_CONDEMNED_BY = "condemnedBy"
val CHILD_LOCATION_CONDEMNATIONS = "condemnations"

val DEFAULT_ANIMATIONS_DURATION: Long = 500
val QUICK_ANIMATIONS_DURATION: Long = 250
val VERY_QUICK_ANIMATIONS_DURATION: Long = 100

val SHARED_PREFERENCES = "com_andre_haueisen_shared_pref"
val SHARED_MESSAGE_TOKEN = "message_token"
val SHARED_MINIMUM_VALUE_TO_MAIN_LIST = "main_list_minimum_value"

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

val NOTIFICATION_CHANNEL_ID = "com.andrehaueisen.listadejanot.notificationChannel"
val NEW_POLITICIAN_CHANNEL = "new_politician_channel"

