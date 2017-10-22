package com.andrehaueisen.listadejanot.b_firebase

import android.content.Context
import android.util.Log
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

    private lateinit var mPublishSenadoresList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishDeputadosList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishGovernadoresList: PublishSubject<ArrayList<Politician>>
    private lateinit var mPublishUser: PublishSubject<User>
    private lateinit var mPublishOpinionsList: PublishSubject<Pair<FirebaseAction, DataSnapshot>>

    private val mListSenadores = ArrayList<Politician>()
    private val mListDeputados = ArrayList<Politician>()
    private val mListGovernadores = ArrayList<Politician>()

    enum class FirebaseAction {
        CHILD_ADDED, CHILD_REMOVED, CHILD_CHANGED
    }

    private val mGenericIndicator = object : GenericTypeIndicator<Politician>() {}

    fun handleListChangeOnDatabase(listAction: ListAction, politician: Politician, userEmail: String) {

        val politicianEncodedEmail = politician.email!!.encodeEmail()
        val userEncodedEmail = userEmail.encodeEmail()

        val politicianType = when (politician.post) {
            Politician.Post.SENADOR, Politician.Post.SENADORA -> LOCATION_SENADORES_LIST
            Politician.Post.DEPUTADO, Politician.Post.DEPUTADA -> LOCATION_DEPUTADOS_LIST
            Politician.Post.GOVERNADOR, Politician.Post.GOVERNADORA -> LOCATION_GOVERNADORES_LIST
            else -> LOCATION_SENADORES_LIST
        }

        var hasRemovedVoteFromRecommendationsList = false
        var hasRemovedVoteFromCondemnationsList = false

        val database = mDatabaseReference.child(LOCATION_USERS).child(userEncodedEmail)
        database.runTransaction(object : Transaction.Handler {
            override fun onComplete(error: DatabaseError?, transactionCommitted: Boolean, dataSnapshot: DataSnapshot?) {

                if (transactionCommitted) {

                    val politicianDatabase = mDatabaseReference.child(politicianType).child(politicianEncodedEmail)
                    politicianDatabase.runTransaction(object : Transaction.Handler {
                        override fun onComplete(error: DatabaseError?, transactionCommitted: Boolean, dataSnapshot: DataSnapshot?) {

                        }

                        override fun doTransaction(mutableData: MutableData?): Transaction.Result {
                            val remotePolitician: Politician = mutableData?.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                            when (listAction) {
                                ListAction.ADD_TO_VOTE_LIST -> {
                                    remotePolitician.recommendationsCount++
                                    if (hasRemovedVoteFromCondemnationsList) remotePolitician.condemnationsCount--
                                }

                                ListAction.ADD_TO_SUSPECT_LIST -> {
                                    remotePolitician.condemnationsCount++
                                    if (hasRemovedVoteFromRecommendationsList) remotePolitician.recommendationsCount--
                                }
                                ListAction.REMOVE_FROM_LISTS -> {
                                    if (hasRemovedVoteFromCondemnationsList) remotePolitician.condemnationsCount--
                                    if (hasRemovedVoteFromRecommendationsList) remotePolitician.recommendationsCount--
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
                            if (hasVoteOnCondemnationList) {
                                remoteUser.condemnations.remove(politicianEncodedEmail)
                                politician.condemnationsCount--
                                hasRemovedVoteFromCondemnationsList = true
                            }
                            remoteUser.recommendations[politicianEncodedEmail] = ServerValue.TIMESTAMP
                            politician.recommendationsCount++
                        }

                        ListAction.ADD_TO_SUSPECT_LIST -> {
                            if (hasVoteOnRecommendationList) {
                                remoteUser.recommendations.remove(politicianEncodedEmail)
                                politician.recommendationsCount--
                                hasRemovedVoteFromRecommendationsList = true
                            }
                            remoteUser.condemnations[politicianEncodedEmail] = ServerValue.TIMESTAMP
                            politician.condemnationsCount++
                        }
                        ListAction.REMOVE_FROM_LISTS -> {
                            if (hasVoteOnRecommendationList) {
                                remoteUser.recommendations.remove(politicianEncodedEmail)
                                politician.recommendationsCount--
                                hasRemovedVoteFromRecommendationsList = true
                            }
                            if (hasVoteOnCondemnationList) {
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
            Politician.Post.SENADOR, Politician.Post.SENADORA -> LOCATION_SENADORES_LIST
            Politician.Post.DEPUTADO, Politician.Post.DEPUTADA -> LOCATION_DEPUTADOS_LIST
            Politician.Post.GOVERNADOR, Politician.Post.GOVERNADORA -> LOCATION_GOVERNADORES_LIST
            else -> LOCATION_SENADORES_LIST
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

    fun listenToUser(userListener: ValueEventListener, userEmail: String?) {
        userEmail?.let {
            mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail()).addValueEventListener(userListener)
        }
    }

    fun queryTopInRecommendationsCount(){

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

    private val mListenerForSenadoresList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mListSenadores.isNotEmpty()) {
                mListSenadores.clear()
            }

            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { snapshot ->
                    if (snapshot != null) {
                        val senador = (snapshot.getValue(mGenericIndicator) as Politician)
                        senador.email = snapshot.key
                        mListSenadores.add(senador)
                    }
                }
            }
            mPublishSenadoresList.onNext(mListSenadores)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishSenadoresList.onError(error.toException())
    }

    private val mListenerForDeputadosList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mListDeputados.isNotEmpty()) {
                mListDeputados.clear()
            }

            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { snapshot ->
                    if (snapshot != null) {
                        val deputado = (snapshot.getValue(mGenericIndicator) as Politician)
                        deputado.email = snapshot.key
                        mListDeputados.add(deputado)
                    }
                }
            }
            mPublishDeputadosList.onNext(mListDeputados)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishDeputadosList.onError(error.toException())
    }

    private val mListenerForGovernadoresList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            if (mListGovernadores.isNotEmpty()) mListGovernadores.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { snapshot ->
                    if (snapshot != null) {
                        val governador = (snapshot.getValue(mGenericIndicator) as Politician)
                        governador.email = snapshot.key
                        mListGovernadores.add(governador)
                    }
                }
            }
            mPublishGovernadoresList.onNext(mListGovernadores)
        }

        override fun onCancelled(error: DatabaseError) =
                mPublishGovernadoresList.onError(error.toException())
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

    fun getUser(userEmail: String): PublishSubject<User> {
        mPublishUser = PublishSubject.create()

        mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail()).addListenerForSingleValueEvent(mListenerForUser)
        return mPublishUser
    }

    fun getSenadoresList(): PublishSubject<ArrayList<Politician>> {
        mPublishSenadoresList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_SENADORES_LIST)
                .addListenerForSingleValueEvent(mListenerForSenadoresList)

        return mPublishSenadoresList
    }

    fun getDeputadosList(): PublishSubject<ArrayList<Politician>> {
        mPublishDeputadosList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_DEPUTADOS_LIST)
                .addListenerForSingleValueEvent(mListenerForDeputadosList)

        return mPublishDeputadosList
    }

    fun getGovernadoresList(): PublishSubject<ArrayList<Politician>> {
        mPublishGovernadoresList = PublishSubject.create()

        mDatabaseReference.child(LOCATION_GOVERNADORES_LIST).addListenerForSingleValueEvent(mListenerForGovernadoresList)

        return mPublishGovernadoresList
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
        mDatabaseReference.child(LOCATION_SENADORES_LIST).removeEventListener(mListenerForSenadoresList)
        mDatabaseReference.child(LOCATION_DEPUTADOS_LIST).removeEventListener(mListenerForDeputadosList)
        mDatabaseReference.child(LOCATION_GOVERNADORES_LIST).removeEventListener(mListenerForDeputadosList)
    }

    fun destroyUserListener(userListener: ValueEventListener, userEmail: String?) {
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

    private fun addSenadorOnList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_LIST).child(senador.email?.encodeEmail())
        database.setValue(senador.toSimpleMap(), ({ _, _ ->

        }))

    }

    private fun addDeputadoOnList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_LIST).child(deputado.email?.encodeEmail())
        database.setValue(deputado.toSimpleMap(), ({ _, _ ->

        }))

    }

    fun saveSenadoresList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_SENADORES_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR || it.post == Politician.Post.SENADORA }
                .forEach { senador -> mapSenadores.put("/${senador.email?.encodeEmail()}/", senador.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })
    }

    fun saveDeputadosList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO || it.post == Politician.Post.DEPUTADA }
                .forEach { deputado -> mapSenadores.put("/${deputado.email?.encodeEmail()}/", deputado.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })

    }

    fun saveGovernadoresList(governadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_GOVERNADORES_LIST)

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