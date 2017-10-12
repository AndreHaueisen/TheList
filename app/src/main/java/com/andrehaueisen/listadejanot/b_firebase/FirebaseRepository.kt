package com.andrehaueisen.listadejanot.b_firebase

import android.content.Context
import android.util.Log
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import com.google.firebase.database.*
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/3/2017.
 */
class FirebaseRepository(private val mContext: Context, private val mDatabaseReference: DatabaseReference) {

    private val LOG_TAG: String = FirebaseRepository::class.java.simpleName

    private lateinit var mPublishSenadoresMainList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishSenadoresPreList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishDeputadosMainList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishDeputadosPreList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishGovernadoresMainList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishGovernadoresPreList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishUser: PublishSubject<User>
    private lateinit var mPublishVoteCountList: PublishSubject<HashMap<String, Long>>
    private lateinit var mPublishOpinionsList: PublishSubject<Pair<FirebaseAction, DataSnapshot>>

    private val mMainListSenadores = ArrayList<Politician>()
    private val mPreListSenadores = ArrayList<Politician>()
    private val mMainListDeputados = ArrayList<Politician>()
    private val mPreListDeputados = ArrayList<Politician>()
    private val mMainListGovernadores = ArrayList<Politician>()
    private val mPreListGovernadores = ArrayList<Politician>()

    enum class FirebaseAction {
        CHILD_ADDED, CHILD_REMOVED, CHILD_CHANGED
    }

    private val mGenericIndicator = object : GenericTypeIndicator<Politician>() {}

    fun handleListChangeOnDatabase(listAction: ListAction, politician:Politician, userEmail: String){

        val politicianEncodedEmail = politician.email!!.encodeEmail()
        val userEncodedEmail = userEmail.encodeEmail()

        val politicianType = when (politician.post) {
            Politician.Post.SENADOR, Politician.Post.SENADORA -> LOCATION_SENADORES_PRE_LIST
            Politician.Post.DEPUTADO, Politician.Post.DEPUTADA -> LOCATION_DEPUTADOS_PRE_LIST
            Politician.Post.GOVERNADOR, Politician.Post.GOVERNADORA -> LOCATION_GOVERNADORES_PRE_LIST
            else -> LOCATION_SENADORES_PRE_LIST
        }

        var hasRemovedVoteFromRecommendationsList = false
        var hasRemovedVoteFromCondemnationsList = false

        val database = mDatabaseReference.child(LOCATION_USERS).child(userEncodedEmail)
        database.runTransaction(object : Transaction.Handler {
            override fun onComplete(error: DatabaseError?, transactionCommitted: Boolean, dataSnapshot: DataSnapshot?) {

                if (transactionCommitted) {

                    val politicianDatabase = mDatabaseReference.child(politicianType).child(politicianEncodedEmail)
                    politicianDatabase.runTransaction(object: Transaction.Handler{
                        override fun onComplete(error: DatabaseError?, transactionCommitted: Boolean, dataSnapshot: DataSnapshot?) {

                        }

                        override fun doTransaction(mutableData: MutableData?): Transaction.Result {
                            val remotePolitician: Politician = mutableData?.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                            when (listAction) {
                                ListAction.ADD_TO_VOTE_LIST -> {
                                    remotePolitician.recommendationsCount++
                                    if(hasRemovedVoteFromCondemnationsList) remotePolitician.condemnationsCount--
                                }

                                ListAction.ADD_TO_SUSPECT_LIST -> {
                                    remotePolitician.condemnationsCount++
                                    if(hasRemovedVoteFromRecommendationsList) remotePolitician.recommendationsCount--
                                }
                                ListAction.REMOVE_FROM_LISTS ->{
                                    if(hasRemovedVoteFromCondemnationsList) remotePolitician.condemnationsCount--
                                    if(hasRemovedVoteFromRecommendationsList) remotePolitician.recommendationsCount--
                                }
                            }

                            mutableData.value = remotePolitician
                            return Transaction.success(mutableData)
                        }

                    })

                }
            }

            override fun doTransaction(mutableData: MutableData?): Transaction.Result {
                val remoteUser: User = mutableData?.getValue(User::class.java) ?: return Transaction.success(mutableData)

                val hasVoteOnRecommendationList = remoteUser.recommendations.containsKey(politicianEncodedEmail)
                val hasVoteOnCondemnationList = remoteUser.condemnations.containsKey(politicianEncodedEmail)

                with(remoteUser) {
                    when (listAction) {
                        ListAction.ADD_TO_VOTE_LIST -> {
                            if(hasVoteOnCondemnationList) {
                                remoteUser.condemnations.remove(politicianEncodedEmail)
                                politician.condemnationsCount--
                                hasRemovedVoteFromCondemnationsList = true
                            }
                            remoteUser.recommendations[politicianEncodedEmail] = ServerValue.TIMESTAMP
                            politician.recommendationsCount++
                        }

                        ListAction.ADD_TO_SUSPECT_LIST -> {
                            if(hasVoteOnRecommendationList) {
                                remoteUser.recommendations.remove(politicianEncodedEmail)
                                politician.recommendationsCount--
                                hasRemovedVoteFromRecommendationsList = true
                            }
                            remoteUser.condemnations[politicianEncodedEmail] = ServerValue.TIMESTAMP
                            politician.condemnationsCount++
                        }
                        ListAction.REMOVE_FROM_LISTS ->{
                            if(hasVoteOnRecommendationList) {
                                remoteUser.recommendations.remove(politicianEncodedEmail)
                                politician.recommendationsCount--
                                hasRemovedVoteFromRecommendationsList = true
                            }
                            if(hasVoteOnCondemnationList) {
                                remoteUser.condemnations.remove(politicianEncodedEmail)
                                politician.condemnationsCount--
                                hasRemovedVoteFromCondemnationsList = true
                            }
                        }

                    }

                    mutableData.value = remoteUser
                }
                return Transaction.success(mutableData)
            }
        })
    }

    fun handleGradeChange(voteType: RatingBarType, outdatedUserGrade: Float, newGrade: Float, politician: Politician, userEmail: String, user: User) {

        val politicianType = when (politician.post) {
            Politician.Post.SENADOR, Politician.Post.SENADORA -> LOCATION_SENADORES_PRE_LIST
            Politician.Post.DEPUTADO, Politician.Post.DEPUTADA -> LOCATION_DEPUTADOS_PRE_LIST
            Politician.Post.GOVERNADOR, Politician.Post.GOVERNADORA -> LOCATION_GOVERNADORES_PRE_LIST
            else -> LOCATION_SENADORES_PRE_LIST
        }

        val politicianEncodedEmail = politician.email?.encodeEmail()

        val database = mDatabaseReference.child(politicianType).child(politicianEncodedEmail)
        database.runTransaction(object : Transaction.Handler {
            override fun onComplete(error: DatabaseError?, transactionCommitted: Boolean, dataSnapshot: DataSnapshot?) {

                if (transactionCommitted) {
                    when (voteType) {
                        RatingBarType.HONESTY -> {
                            mDatabaseReference
                                    .child(LOCATION_USERS)
                                    .child(userEmail.encodeEmail())
                                    .child(CHILD_LOCATION_USER_HONESTY)
                                    .child(politicianEncodedEmail)
                                    .setValue(newGrade)
                        }
                        RatingBarType.LEADER -> {
                            mDatabaseReference
                                    .child(LOCATION_USERS)
                                    .child(userEmail.encodeEmail())
                                    .child(CHILD_LOCATION_USER_LEADER)
                                    .child(politicianEncodedEmail)
                                    .setValue(newGrade)
                        }
                        RatingBarType.PROMISE_KEEPER -> {
                            mDatabaseReference
                                    .child(LOCATION_USERS)
                                    .child(userEmail.encodeEmail())
                                    .child(CHILD_LOCATION_USER_PROMISE_KEEPER)
                                    .child(politicianEncodedEmail)
                                    .setValue(newGrade)
                        }
                        RatingBarType.RULES_FOR_PEOPLE -> {
                            mDatabaseReference
                                    .child(LOCATION_USERS)
                                    .child(userEmail.encodeEmail())
                                    .child(CHILD_LOCATION_USER_RULES_FOR_THE_PEOPLE)
                                    .child(politicianEncodedEmail)
                                    .setValue(newGrade)
                        }
                        RatingBarType.ANSWER_VOTERS -> {
                            mDatabaseReference
                                    .child(LOCATION_USERS)
                                    .child(userEmail.encodeEmail())
                                    .child(CHILD_LOCATION_USER_ANSWER_VOTERS)
                                    .child(politicianEncodedEmail)
                                    .setValue(newGrade)
                        }
                    }
                }
            }

            override fun doTransaction(mutableData: MutableData?): Transaction.Result {
                val remotePolitician: Politician = mutableData?.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                with(remotePolitician) {
                    when (voteType) {
                        RatingBarType.HONESTY -> {
                            val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                            val isNotFirstGrade = honestyGrade != UNEXISTING_GRADE_VALUE

                            honestyGrade = if (isNotFirstGrade) {
                                if (containsUserPastGrade) {
                                    ((honestyGrade * honestyCount) - outdatedUserGrade + newGrade) / honestyCount
                                } else {
                                    honestyCount++
                                    ((honestyGrade * (honestyCount - 1)) + newGrade) / honestyCount
                                }
                            } else {
                                honestyCount++
                                newGrade
                            }
                        }

                        RatingBarType.LEADER -> {
                            val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                            val isNotFirstGrade = leaderGrade != UNEXISTING_GRADE_VALUE

                            leaderGrade = if (isNotFirstGrade) {
                                if (containsUserPastGrade) {
                                    ((leaderGrade * leaderCount) - outdatedUserGrade + newGrade) / leaderCount
                                } else {
                                    leaderCount++
                                    ((leaderGrade * (leaderCount - 1)) + newGrade) / leaderCount
                                }
                            } else {
                                leaderCount++
                                newGrade
                            }
                        }

                        RatingBarType.PROMISE_KEEPER -> {
                            val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                            val isNotFirstGrade = promiseKeeperGrade != UNEXISTING_GRADE_VALUE

                            promiseKeeperGrade = if (isNotFirstGrade) {
                                if (containsUserPastGrade) {
                                    ((promiseKeeperGrade * promiseKeeperCount) - outdatedUserGrade + newGrade) / promiseKeeperCount
                                } else {
                                    promiseKeeperCount++
                                    ((promiseKeeperGrade * (promiseKeeperCount - 1)) + newGrade) / promiseKeeperCount
                                }
                            } else {
                                promiseKeeperCount++
                                newGrade
                            }
                        }

                        RatingBarType.RULES_FOR_PEOPLE -> {
                            val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                            val isNotFirstGrade = rulesForThePeopleGrade != UNEXISTING_GRADE_VALUE

                            rulesForThePeopleGrade = if (isNotFirstGrade) {
                                if (containsUserPastGrade) {
                                    ((rulesForThePeopleGrade * rulesForThePeopleCount) - outdatedUserGrade + newGrade) / rulesForThePeopleCount
                                } else {
                                    rulesForThePeopleCount++
                                    ((rulesForThePeopleGrade * (rulesForThePeopleCount - 1)) + newGrade) / rulesForThePeopleCount
                                }
                            } else {
                                rulesForThePeopleCount++
                                newGrade
                            }
                        }

                        RatingBarType.ANSWER_VOTERS -> {
                            val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                            val isNotFirstGrade = answerVotersGrade != UNEXISTING_GRADE_VALUE

                            answerVotersGrade = if (isNotFirstGrade) {
                                if (containsUserPastGrade) {
                                    ((answerVotersGrade * answerVotersCount) - outdatedUserGrade + newGrade) / answerVotersCount
                                } else {
                                    answerVotersCount++
                                    ((answerVotersGrade * (answerVotersCount - 1)) + newGrade) / answerVotersCount
                                }
                            } else {
                                answerVotersCount++
                                newGrade
                            }
                        }
                    }

                    mutableData.value = remotePolitician
                }
                return Transaction.success(mutableData)
            }
        })
    }

    fun listenToUser(userListener: ValueEventListener, userEmail: String?){
        userEmail?.let {
            mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail()).addValueEventListener(userListener)
        }
    }

    private val mListenerForUser = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            val user = if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.getValue(User::class.java) ?: User()
            } else {
                User()
            }

            mPublishUser.onNext(user)
        }

        override fun onCancelled(error: DatabaseError) = mPublishUser.onError(error.toException())
    }

    private val mListenerForVoteCountList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val voteCountList = HashMap<String, Long>()

            if (dataSnapshot != null && dataSnapshot.exists()) {

                dataSnapshot.children.forEach { voteCount ->
                    voteCountList[voteCount.key] = voteCount.value as? Long ?: 0
                }
            }

            mPublishVoteCountList.onNext(voteCountList)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishVoteCountList.onError(error.toException())
    }

    private val mListenerForSenadoresMainList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mMainListSenadores.isNotEmpty()) mMainListSenadores.clear()

            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mMainListSenadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishSenadoresMainList.onNext(mMainListSenadores)

        }

        override fun onCancelled(error: DatabaseError) =
                mPublishSenadoresMainList.onError(error.toException())
    }

    private val mListenerForSenadoresPreList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mPreListSenadores.isNotEmpty()) {
                mPreListSenadores.clear()
            }

            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mPreListSenadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishSenadoresPreList.onNext(mPreListSenadores)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishSenadoresPreList.onError(error.toException())
    }

    private val mListenerForDeputadosMainList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mMainListDeputados.isNotEmpty()) mMainListDeputados.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mMainListDeputados.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishDeputadosMainList.onNext(mMainListDeputados)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishDeputadosMainList.onError(error.toException())
    }

    private val mListenerForDeputadosPreList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mPreListDeputados.isNotEmpty()) {
                mPreListDeputados.clear()
            }

            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mPreListDeputados.add(it.getValue(mGenericIndicator)!!) }

            }
            mPublishDeputadosPreList.onNext(mPreListDeputados)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishDeputadosPreList.onError(error.toException())
    }

    private val mListenerForGovernadoresMainList = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            if (mMainListGovernadores.isNotEmpty()) mMainListGovernadores.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mMainListGovernadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishGovernadoresMainList.onNext(mMainListGovernadores)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishGovernadoresMainList.onError(error.toException())
    }

    private val mListenerForGovernadoresPreList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            if (mPreListGovernadores.isNotEmpty()) mPreListGovernadores.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mPreListGovernadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishGovernadoresPreList.onNext(mPreListGovernadores)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishGovernadoresPreList.onError(error.toException())
    }

    private val mListenerForOpinionsList = object : ChildEventListener {

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?) {

            if (dataSnapshot != null && dataSnapshot.exists()) {
                val pair = Pair(FirebaseAction.CHILD_CHANGED, dataSnapshot)
                mPublishOpinionsList.onNext(pair)
            }
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?) {

            if (dataSnapshot != null && dataSnapshot.exists()) {
                val pair = Pair(FirebaseAction.CHILD_ADDED, dataSnapshot)
                mPublishOpinionsList.onNext(pair)
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            if (dataSnapshot != null && dataSnapshot.exists()) {
                val pair = Pair(FirebaseAction.CHILD_REMOVED, dataSnapshot)
                mPublishOpinionsList.onNext(pair)
            }
        }

        override fun onCancelled(p0: DatabaseError?) = Unit

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?) = Unit
    }

    private val mListenerForMinimumVotesToMainList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val minimumVoteToMailList: Int = dataSnapshot?.getValue(Int::class.java) ?: 0

            val sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES, 0)
            val lastMinimumVoteToMailList = sharedPreferences.getInt(SHARED_MINIMUM_VALUE_TO_MAIN_LIST, 0)

            if (minimumVoteToMailList != lastMinimumVoteToMailList)
                mContext.showToast(mContext.getString(R.string.new_minimum_votes_value, minimumVoteToMailList))

            val sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putInt(SHARED_MINIMUM_VALUE_TO_MAIN_LIST, minimumVoteToMailList)
            sharedPreferencesEditor.apply()
        }

        override fun onCancelled(p0: DatabaseError?) = Unit
    }

    fun getUser(userEmail: String): PublishSubject<User> {
        mPublishUser = PublishSubject.create()

        mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail()).addListenerForSingleValueEvent(mListenerForUser)
        return mPublishUser
    }

    fun getVoteCountList(): PublishSubject<HashMap<String, Long>> {
        mPublishVoteCountList = PublishSubject.create()
        mDatabaseReference.child(LOCATION_VOTE_COUNT).addListenerForSingleValueEvent(mListenerForVoteCountList)

        return mPublishVoteCountList
    }

    fun getSenadoresPreList(): PublishSubject<ArrayList<Politician>> {
        mPublishSenadoresPreList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_SENADORES_PRE_LIST)
                .addListenerForSingleValueEvent(mListenerForSenadoresPreList)

        return mPublishSenadoresPreList
    }

    fun getDeputadosPreList(): PublishSubject<ArrayList<Politician>> {
        mPublishDeputadosPreList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_DEPUTADOS_PRE_LIST)
                .addListenerForSingleValueEvent(mListenerForDeputadosPreList)

        return mPublishDeputadosPreList
    }

    fun getGovernadoresPreList(): PublishSubject<ArrayList<Politician>> {
        mPublishGovernadoresPreList = PublishSubject.create()

        mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST).addListenerForSingleValueEvent(mListenerForGovernadoresPreList)

        return mPublishGovernadoresPreList
    }

    fun getPoliticianOpinions(politicianEmail: String): PublishSubject<Pair<FirebaseAction, DataSnapshot>> {
        mPublishOpinionsList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_OPINIONS_ON_POLITICIANS)
                .child(politicianEmail.encodeEmail())
                .addChildEventListener(mListenerForOpinionsList)

        return mPublishOpinionsList
    }

    fun completePublishOptionsList() = mPublishOpinionsList.onComplete()

    fun onDestroy() {
        mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).removeEventListener(mListenerForSenadoresMainList)
        mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).removeEventListener(mListenerForSenadoresPreList)
        mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).removeEventListener(mListenerForDeputadosMainList)
        mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).removeEventListener(mListenerForDeputadosPreList)
        mDatabaseReference.child(LOCATION_GOVERNADORES_MAIN_LIST).removeEventListener(mListenerForGovernadoresMainList)
        mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST).removeEventListener(mListenerForDeputadosPreList)


    }

    fun destroyUserListener(userListener: ValueEventListener, userEmail: String?){
        userEmail?.let {
            mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail()).removeEventListener(userListener)
        }
    }

    fun addOpinionOnPolitician(politicianEmail: String, userEmail: String, opinion: String) {
        mDatabaseReference
                .child(LOCATION_OPINIONS_ON_POLITICIANS)
                .child(politicianEmail.encodeEmail())
                .child(userEmail.encodeEmail())
                .setValue(opinion)
    }

    fun removeOpinion(politicianEmail: String, userEmail: String?) {
        userEmail?.let {
            mDatabaseReference
                    .child(LOCATION_OPINIONS_ON_POLITICIANS)
                    .child(politicianEmail.encodeEmail())
                    .child(userEmail.encodeEmail())
                    .removeValue()
        }
    }

    fun killFirebaseListener(politicianEmail: String) = mDatabaseReference
            .child(LOCATION_OPINIONS_ON_POLITICIANS)
            .child(politicianEmail.encodeEmail())
            .removeEventListener(mListenerForOpinionsList)



    private fun addSenadorOnPreList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).child(senador.email?.encodeEmail())
        database.setValue(senador.toSimpleMap(), ({ _, _ ->

        }))

    }

    private fun addDeputadoOnPreList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).child(deputado.email?.encodeEmail())
        database.setValue(deputado.toSimpleMap(), ({ _, _ ->

        }))

    }

    fun saveSenadoresOnPreList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR || it.post == Politician.Post.SENADORA }
                .forEach { senador -> mapSenadores.put("/${senador.email?.encodeEmail()}/", senador.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })
    }

    fun saveDeputadosOnPreList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO || it.post == Politician.Post.DEPUTADA }
                .forEach { deputado -> mapSenadores.put("/${deputado.email?.encodeEmail()}/", deputado.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })

    }

    fun saveGovernadoresOnPreList(governadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        governadores.filter { it.post == Politician.Post.GOVERNADOR || it.post == Politician.Post.GOVERNADORA }
                .forEach { governador -> mapSenadores.put("/${governador.email?.encodeEmail()}/", governador.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })
    }
}