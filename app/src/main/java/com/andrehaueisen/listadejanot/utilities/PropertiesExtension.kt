package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.c_database.PoliticiansContract

enum class RatingBarType{
    HONESTY, LEADER, PROMISE_KEEPER, RULES_FOR_PEOPLE, ANSWER_VOTERS
}

enum class ListAction {
    ADD_TO_VOTE_LIST, ADD_TO_SUSPECT_LIST, REMOVE_FROM_LISTS
}

enum class SortType {
    RECOMMENDATIONS_COUNT, CONDEMNATIONS_COUNT, TOP_OVERALL_GRADE, WORST_OVERALL_GRADE, MEDIA_HIGHLIGHT
}

enum class CallingActivity {
    MAIN_LISTS_CHOICES_PRESENTER_ACTIVITY,
    MAIN_LISTS_PRESENTER_ACTIVITY,
    POLITICIAN_SELECTOR_PRESENTER_ACTIVITY,
    USER_VOTE_LIST_PRESENTER_ACTIVITY
}

const val PLAY_STORE_LINK = """http://play.google.com/store/apps/details?id=com.andrehaueisen.listadejanot"""

const val INTENT_CALLING_ACTIVITY = "intent_calling_activity"
const val INTENT_POLITICIAN_NAME = "intent_politician_name"
const val BUNDLE_PRESIDENTES_LIST = "bundle_presidentes_list"
const val BUNDLE_SENADORES_LIST = "bundle_senadores_list"
const val BUNDLE_GOVERNADORES_LIST = "bundle_governadores_list"
const val BUNDLE_DEPUTADOS_LIST = "bundle_deputados_list"
const val BUNDLE_SORT_TYPE = "bundle_sort_type"
const val BUNDLE_PRESIDENTES_LAYOUT_MANAGER =  "bundle_presidentes_layout_manager"
const val BUNDLE_SENADORES_LAYOUT_MANAGER =  "bundle_senadores_layout_manager"
const val BUNDLE_GOVERNADORES_LAYOUT_MANAGER = "bundle_governadores_layout_manager"
const val BUNDLE_DEPUTADOS_LAYOUT_MANAGER = "bundle_deputados_layout_manager"
const val BUNDLE_SEARCHABLE_POLITICIANS = "politicians_list"
const val BUNDLE_MANAGER = "layout_manager_state"
const val BUNDLE_CURRENT_SHOWING_LIST = "current_showing_list"
const val BUNDLE_POLITICIAN_NAME = "politician_name"
const val BUNDLE_POLITICIAN = "politician"
const val BUNDLE_VOTED_POLITICIANS = "politicians_list"
const val BUNDLE_POLITICIAN_EMAIL = "politician_email"
const val BUNDLE_POLITICIAN_IMAGE = "politician_image"
const val BUNDLE_USER_EMAIL = "user_email"
const val BUNDLE_USER_OPINION = "user_opinion"

const val LOCATION_UID_MAPPINGS = "UID_mappings"
const val LOCATION_MESSAGE_TOKENS = "Message_tokens"
const val LOCATION_DEPUTADOS_LIST = "Deputados_list"
const val LOCATION_SENADORES_LIST = "Senadores_list"
const val LOCATION_GOVERNADORES_LIST = "Governadores_list"
const val LOCATION_PRESIDENTES_LIST = "Presidentes_list"
const val LOCATION_USERS = "Users"
const val LOCATION_MEDIA_HIGHLIGHT = "Media_highlight"
const val LOCATION_OPINIONS_ON_POLITICIANS = "Opinions_on_politicians"
const val CHILD_LOCATION_USER_HONESTY = "honestyGrades"
const val CHILD_LOCATION_USER_LEADER = "leaderGrades"
const val CHILD_LOCATION_USER_PROMISE_KEEPER = "promiseKeeperGrades"
const val CHILD_LOCATION_USER_RULES_FOR_THE_PEOPLE = "rulesForThePeopleGrades"
const val CHILD_LOCATION_USER_ANSWER_VOTERS = "answerVotersGrades"

const val SUSPECTS_POLITICIANS_ADAPTER_TYPE = 0
const val WILL_VOTE_POLITICIANS_ADAPTER_TYPE = 1

const val DEFAULT_ANIMATIONS_DURATION: Long = 500
const val QUICK_ANIMATIONS_DURATION: Long = 250
const val VERY_QUICK_ANIMATIONS_DURATION: Long = 100
const val NONEXISTING_GRADE_VALUE: Float = -1F

const val SHARED_PREFERENCES = "com_andre_haueisen_shared_pref"
const val SHARED_MESSAGE_TOKEN = "message_token"
const val SHARED_VERSION_CODE = "version_code"

val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

const val LOADER_ID = 0

val POLITICIANS_COLUMNS = arrayOf(
        politiciansEntry._ID,
        politiciansEntry.COLUMN_POST,
        politiciansEntry.COLUMN_EMAIL,
        politiciansEntry.COLUMN_IMAGE_URL,
        politiciansEntry.COLUMN_IMAGE_TIMESTAMP)

const val NOTIFICATION_CHANNEL_ID = "com.andrehaueisen.listadejanot.notificationChannel"
const val NEW_POLITICIAN_CHANNEL = "new_politician_channel"

