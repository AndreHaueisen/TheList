package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.c_database.PoliticiansContract

enum class RatingBarType{
    HONESTY, LEADER, PROMISE_KEEPER, RULES_FOR_PEOPLE, ANSWER_VOTERS
}

enum class ListAction {
    ADD_TO_VOTE_LIST, ADD_TO_SUSPECT_LIST, REMOVE_FROM_LISTS
}

val PLAY_STORE_LINK = "market://details?id=com.andrehaueisen.listadejanot"

val INTENT_POLITICIAN_NAME = "intent_politician_name"
val BUNDLE_SEARCHABLE_POLITICIANS = "politicians_list"
val BUNDLE_MANAGER = "layout_manager_state"
val BUNDLE_CURRENT_SHOWING_LIST = "current_showing_list"
val BUNDLE_POLITICIAN_NAME = "politician_name"
val BUNDLE_POLITICIAN = "politician"
val BUNDLE_VOTED_POLITICIANS = "politicians_list"
val BUNDLE_USER = "user"
val BUNDLE_POLITICIAN_EMAIL = "politician_email"
val BUNDLE_POLITICIAN_IMAGE = "politician_image"
val BUNDLE_USER_EMAIL = "user_email"
val BUNDLE_USER_OPINION = "user_opinion"

val LOCATION_UID_MAPPINGS = "UID_mappings"
val LOCATION_MESSAGE_TOKENS = "Message_tokens"
val LOCATION_DEPUTADOS_LIST = "Deputados_list"
val LOCATION_SENADORES_LIST = "Senadores_list"
val LOCATION_GOVERNADORES_LIST = "Governadores_list"
val LOCATION_USERS = "Users"
val LOCATION_OPINIONS_ON_POLITICIANS = "Opinions_on_politicians"
val CHILD_LOCATION_USER_HONESTY = "honestyGrades"
val CHILD_LOCATION_USER_LEADER = "leaderGrades"
val CHILD_LOCATION_USER_PROMISE_KEEPER = "promiseKeeperGrades"
val CHILD_LOCATION_USER_RULES_FOR_THE_PEOPLE = "rulesForThePeopleGrades"
val CHILD_LOCATION_USER_ANSWER_VOTERS = "answerVotersGrades"

val SUSPECTS_POLITICIANS_ADAPTER_TYPE = 0
val WILL_VOTE_POLITICIANS_ADAPTER_TYPE = 1

val SLOW_ANIMATION_DURATION: Long = 25000
val ULTRA_SLOW_ANIMATION_DURATION: Long = 40000
val DEFAULT_ANIMATIONS_DURATION: Long = 500
val QUICK_ANIMATIONS_DURATION: Long = 250
val VERY_QUICK_ANIMATIONS_DURATION: Long = 100
val UNEXISTING_GRADE_VALUE: Float = -1F

val SHARED_PREFERENCES = "com_andre_haueisen_shared_pref"
val SHARED_MESSAGE_TOKEN = "message_token"

val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

val LOADER_ID = 0

val POLITICIANS_COLUMNS = arrayOf(
        politiciansEntry.COLUMN_POST,
        politiciansEntry.COLUMN_NAME,
        politiciansEntry.COLUMN_EMAIL)

val POLITICIANS_COLUMNS_NAME_NO_EMAIL = arrayOf(
        politiciansEntry.COLUMN_POST,
        politiciansEntry.COLUMN_NAME)

val NOTIFICATION_CHANNEL_ID = "com.andrehaueisen.listadejanot.notificationChannel"
val NEW_POLITICIAN_CHANNEL = "new_politician_channel"

