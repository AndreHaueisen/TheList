package com.andrehaueisen.listadejanot.b_firebase

import android.content.Context
import android.util.Log
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_main_list.PoliticianListAdapter
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorMvpContract
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

    fun handleSenadorVoteOnDatabase(senador: Politician,
                                    userEmail: String,
                                    viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                    politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun changeIsSenadorOnMainListStatus(email: String?, isOnMainList: Boolean) {
            val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).child(email?.encodeEmail())
            val mutableMap = mutableMapOf<String, Any>()
            mutableMap.put(CHILD_LOCATION_IS_ON_MAIN_LIST, isOnMainList)
            database.updateChildren(mutableMap)
        }

        fun updateSenadorVoteOnMainList(senador1: Politician,
                                        userEmail: String,
                                        viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador1.email?.encodeEmail())

            database.runTransaction(object : Transaction.Handler {

                override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {
                    if (isSuccessful && dataSnapshot.exists()) {

                        val updatedSenador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                        senador1.condemnedBy = updatedSenador.condemnedBy
                        senador1.votesNumber = updatedSenador.votesNumber
                        if (viewHolder != null) {
                            if (updatedSenador.condemnedBy.contains(userEmail.encodeEmail())) {
                                viewHolder.initiateCondemnAnimations(senador1)
                            } else {
                                viewHolder.initiateAbsolveAnimations(senador1)
                            }
                        }

                        changeIsSenadorOnMainListStatus(senador1.email, isOnMainList = true)

                    } else if (!dataSnapshot.exists()) {

                        viewHolder?.notifyPoliticianRemovedFromMainList(senador1)
                        changeIsSenadorOnMainListStatus(senador1.email, isOnMainList = false)
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    val minimumVotesToMainList = mContext.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
                    if (senador1.votesNumber < minimumVotesToMainList) {
                        mutableData.value = null
                        return Transaction.success(mutableData)
                    }

                    mutableData.value = senador1.toSimpleMap(false)
                    return Transaction.success(mutableData)

                }
            })
        }

        fun updateSenadorVoteOnPreList(userEmail: String, politicianSelectorView: PoliticianSelectorMvpContract.View?) {

            if (senador.post == Politician.Post.SENADOR || senador.post == Politician.Post.SENADORA) {
                val database = mDatabaseReference
                        .child(LOCATION_SENADORES_PRE_LIST)
                        .child(senador.email?.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedSenador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedSenador.email = senador.email

                            updateVoteCountOnFirebase(updatedSenador)
                            updateUserVoteList(userEmail, updatedSenador)
                            val minimumVotesToMainList = mContext.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
                            if (updatedSenador.onMainList || (!updatedSenador.onMainList && updatedSenador.votesNumber >= minimumVotesToMainList)) {
                                updateSenadorVoteOnMainList(updatedSenador, userEmail, viewHolder)
                            }

                            senador.condemnedBy = updatedSenador.condemnedBy
                            senador.votesNumber = updatedSenador.votesNumber

                            if (politicianSelectorView is PoliticianSelectorMvpContract.View) {
                                if (updatedSenador.condemnedBy.contains(userEmail.encodeEmail())) {
                                    politicianSelectorView.initiateCondemnAnimations(senador)
                                } else {
                                    politicianSelectorView.initiateAbsolveAnimations(senador)
                                }
                            }
                        } else {
                            Log.e(LOG_TAG, error.toString())
                        }

                    }

                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val senadorRemote: Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                        val encodedEmail = userEmail.encodeEmail()
                        if (senadorRemote.condemnedBy.contains(encodedEmail)) {
                            senadorRemote.votesNumber--
                            senadorRemote.condemnedBy.remove(encodedEmail)
                        } else {
                            senadorRemote.votesNumber++
                            senadorRemote.condemnedBy[encodedEmail] = ServerValue.TIMESTAMP
                        }

                        mutableData.value = senadorRemote
                        return Transaction.success(mutableData)
                    }
                })
            }
        }

        updateSenadorVoteOnPreList(userEmail, politicianSelectorView)

    }

    fun handleDeputadoVoteOnDatabase(deputado: Politician,
                                     userEmail: String,
                                     viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                     politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun changeIsDeputadoOnMainListStatus(email: String?, isOnMainList: Boolean) {
            val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).child(email?.encodeEmail())
            val mutableMap = mutableMapOf<String, Any>()
            mutableMap.put(CHILD_LOCATION_IS_ON_MAIN_LIST, isOnMainList)
            database.updateChildren(mutableMap)
        }

        fun updateDeputadoVoteOnMainList(deputado1: Politician,
                                         userEmail: String,
                                         viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference
                    .child(LOCATION_DEPUTADOS_MAIN_LIST)
                    .child(deputado1.email?.encodeEmail())

            database.runTransaction(object : Transaction.Handler {

                override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                    if (isSuccessful && dataSnapshot.exists()) {

                        val updatedDeputado: Politician = dataSnapshot.getValue(Politician::class.java)!!
                        deputado1.condemnedBy = updatedDeputado.condemnedBy
                        deputado1.votesNumber = updatedDeputado.votesNumber
                        if (viewHolder != null) {
                            if (updatedDeputado.condemnedBy.contains(userEmail.encodeEmail())) {
                                viewHolder.initiateCondemnAnimations(deputado1)
                            } else {
                                viewHolder.initiateAbsolveAnimations(deputado1)
                            }
                        }

                        changeIsDeputadoOnMainListStatus(deputado1.email, isOnMainList = true)

                    } else if (!dataSnapshot.exists()) {

                        viewHolder?.notifyPoliticianRemovedFromMainList(deputado1)
                        changeIsDeputadoOnMainListStatus(deputado1.email, isOnMainList = false)
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val minimumVotesToMainList = mContext.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
                    if (deputado1.votesNumber < minimumVotesToMainList) {
                        mutableData.value = null
                        return Transaction.success(mutableData)
                    }

                    mutableData.value = deputado1.toSimpleMap(false)
                    return Transaction.success(mutableData)
                }
            })
        }

        fun updateDeputadoVoteOnPreList(userEmail: String,
                                        politicianSelectorView: PoliticianSelectorMvpContract.View?) {
            if (deputado.post == Politician.Post.DEPUTADO || deputado.post == Politician.Post.DEPUTADA) {
                val database = mDatabaseReference
                        .child(LOCATION_DEPUTADOS_PRE_LIST)
                        .child(deputado.email?.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedDeputado: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedDeputado.email = deputado.email

                            updateVoteCountOnFirebase(updatedDeputado)
                            updateUserVoteList(userEmail, updatedDeputado)
                            val minimumVotesToMainList = mContext.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
                            if (updatedDeputado.onMainList || (!updatedDeputado.onMainList && updatedDeputado.votesNumber >= minimumVotesToMainList)) {
                                updateDeputadoVoteOnMainList(updatedDeputado, userEmail, viewHolder)
                            }

                            deputado.condemnedBy = updatedDeputado.condemnedBy
                            deputado.votesNumber = updatedDeputado.votesNumber

                            if (politicianSelectorView is PoliticianSelectorMvpContract.View) {
                                if (updatedDeputado.condemnedBy.contains(userEmail.encodeEmail())) {
                                    politicianSelectorView.initiateCondemnAnimations(deputado)
                                } else {
                                    politicianSelectorView.initiateAbsolveAnimations(deputado)
                                }
                            }

                            Log.i(LOG_TAG, dataSnapshot.toString())
                        } else {
                            Log.e(LOG_TAG, error.toString())
                        }

                    }

                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val deputadoRemote: Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                        val encodedEmail = userEmail.encodeEmail()
                        if (deputadoRemote.condemnedBy.contains(encodedEmail)) {
                            deputadoRemote.votesNumber--
                            deputadoRemote.condemnedBy.remove(encodedEmail)
                        } else {
                            deputadoRemote.votesNumber++
                            deputadoRemote.condemnedBy[encodedEmail] = ServerValue.TIMESTAMP
                        }

                        mutableData.value = deputadoRemote
                        return Transaction.success(mutableData)

                    }
                })
            }
        }

        updateDeputadoVoteOnPreList(userEmail, politicianSelectorView)
    }

    fun handleGovernadorVoteOnDatabase(governador: Politician,
                                       userEmail: String,
                                       viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                       politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun changeIsGovernadorOnMainListStatus(email: String?, isOnMainList: Boolean) {
            val database = mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST).child(email?.encodeEmail())
            val mutableMap = mutableMapOf<String, Any>()
            mutableMap.put(CHILD_LOCATION_IS_ON_MAIN_LIST, isOnMainList)
            database.updateChildren(mutableMap)
        }

        fun updateGovernadorVoteOnMainList(governador1: Politician,
                                           userEmail: String,
                                           viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference
                    .child(LOCATION_GOVERNADORES_MAIN_LIST)
                    .child(governador1.email?.encodeEmail())

            database.runTransaction(object : Transaction.Handler {

                override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                    if (isSuccessful && dataSnapshot.exists()) {

                        val updatedGovernador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                        governador1.condemnedBy = updatedGovernador.condemnedBy
                        governador1.votesNumber = updatedGovernador.votesNumber
                        if (viewHolder != null) {
                            if (updatedGovernador.condemnedBy.contains(userEmail.encodeEmail())) {
                                viewHolder.initiateCondemnAnimations(governador1)
                            } else {
                                viewHolder.initiateAbsolveAnimations(governador1)
                            }
                        }

                        changeIsGovernadorOnMainListStatus(governador1.email, isOnMainList = true)

                    } else if (!dataSnapshot.exists()) {

                        viewHolder?.notifyPoliticianRemovedFromMainList(governador1)
                        changeIsGovernadorOnMainListStatus(governador1.email, isOnMainList = false)
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    val minimumVotesToMainList = mContext.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
                    if (governador1.votesNumber < minimumVotesToMainList) {
                        mutableData.value = null
                        return Transaction.success(mutableData)
                    }

                    mutableData.value = governador1.toSimpleMap(false)
                    return Transaction.success(mutableData)
                }
            })
        }

        fun updateGovernadorVoteOnPreList(userEmail: String, politicianSelectorView: PoliticianSelectorMvpContract.View?) {

            if (governador.post == Politician.Post.GOVERNADOR || governador.post == Politician.Post.GOVERNADORA) {
                val database = mDatabaseReference
                        .child(LOCATION_GOVERNADORES_PRE_LIST)
                        .child(governador.email?.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedGovernador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedGovernador.email = governador.email

                            updateVoteCountOnFirebase(updatedGovernador)
                            updateUserVoteList(userEmail, updatedGovernador)
                            val minimumVotesToMainList = mContext.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
                            if (updatedGovernador.onMainList || (!updatedGovernador.onMainList && updatedGovernador.votesNumber >= minimumVotesToMainList)) {
                                updateGovernadorVoteOnMainList(updatedGovernador, userEmail, viewHolder)
                            }

                            governador.condemnedBy = updatedGovernador.condemnedBy
                            governador.votesNumber = updatedGovernador.votesNumber

                            if (politicianSelectorView is PoliticianSelectorMvpContract.View) {
                                if (updatedGovernador.condemnedBy.contains(userEmail.encodeEmail())) {
                                    politicianSelectorView.initiateCondemnAnimations(governador)
                                } else {
                                    politicianSelectorView.initiateAbsolveAnimations(governador)
                                }
                            }

                            Log.i(LOG_TAG, dataSnapshot.toString())
                        } else {
                            Log.e(LOG_TAG, error.toString())
                        }

                    }

                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val governadorRemote: Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                        val encodedEmail = userEmail.encodeEmail()
                        if (governadorRemote.condemnedBy.contains(encodedEmail)) {
                            governadorRemote.votesNumber--
                            governadorRemote.condemnedBy.remove(encodedEmail)
                        } else {
                            governadorRemote.votesNumber++
                            governadorRemote.condemnedBy[encodedEmail] = ServerValue.TIMESTAMP
                        }

                        mutableData.value = governadorRemote
                        return Transaction.success(mutableData)

                    }
                })
            }
        }

        updateGovernadorVoteOnPreList(userEmail, politicianSelectorView)
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

    private fun updateVoteCountOnFirebase(updatedPolitician: Politician) {

        val database = mDatabaseReference.child(LOCATION_VOTE_COUNT)
        val emailVotesMap = HashMap<String?, Long>()
        emailVotesMap[updatedPolitician.email?.encodeEmail()] = updatedPolitician.votesNumber
        database.updateChildren(emailVotesMap.toMap())
    }

    private fun updateUserVoteList(userEmail: String, updatedPolitician: Politician) {

        val database = mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail())

        database.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {

                val user: User = mutableData.getValue(User::class.java) ?: User()
                val politicianEmail = updatedPolitician.email?.encodeEmail()

                if (updatedPolitician.condemnedBy.contains(userEmail.encodeEmail())) {
                    user.condemnations[politicianEmail!!] = (ServerValue.TIMESTAMP)
                } else {
                    user.condemnations.remove(politicianEmail)
                }

                mutableData.value = user
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) =
                    Unit
        })

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

    fun getSenadoresMainList(): PublishSubject<ArrayList<Politician>> {
        mPublishSenadoresMainList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_SENADORES_MAIN_LIST)
                .addListenerForSingleValueEvent(mListenerForSenadoresMainList)

        return mPublishSenadoresMainList
    }

    fun getSenadoresPreList(): PublishSubject<ArrayList<Politician>> {
        mPublishSenadoresPreList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_SENADORES_PRE_LIST)
                .addListenerForSingleValueEvent(mListenerForSenadoresPreList)

        return mPublishSenadoresPreList
    }

    fun getDeputadosMainList(): PublishSubject<ArrayList<Politician>> {
        mPublishDeputadosMainList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_DEPUTADOS_MAIN_LIST)
                .addListenerForSingleValueEvent(mListenerForDeputadosMainList)

        return mPublishDeputadosMainList
    }

    fun getDeputadosPreList(): PublishSubject<ArrayList<Politician>> {
        mPublishDeputadosPreList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_DEPUTADOS_PRE_LIST)
                .addListenerForSingleValueEvent(mListenerForDeputadosPreList)

        return mPublishDeputadosPreList
    }

    fun getGovernadoresMainList(): PublishSubject<ArrayList<Politician>> {
        mPublishGovernadoresMainList = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_GOVERNADORES_MAIN_LIST)
                .addListenerForSingleValueEvent(mListenerForGovernadoresMainList)

        return mPublishGovernadoresMainList
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

    fun listenForMinimumVotes() {
        mDatabaseReference.child(LOCATION_MINIMUM_VOTES_FOR_MAIN_LIST).addValueEventListener(mListenerForMinimumVotesToMainList)
    }

    fun removeMinimumVoteListener() =
            mDatabaseReference.child(LOCATION_MINIMUM_VOTES_FOR_MAIN_LIST)
                    .removeEventListener(mListenerForMinimumVotesToMainList)

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

    private fun addSenadorOnMainList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador.email?.encodeEmail())
        database.setValue(senador.toSimpleMap(false), ({ _, _ ->
            Log.i(LOG_TAG, "New senador on main list")
        }))

    }

    private fun addSenadorOnPreList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).child(senador.email?.encodeEmail())
        database.setValue(senador.toSimpleMap(true), ({ _, _ ->

        }))

    }

    private fun addDeputadoOnMainList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).child(deputado.email?.encodeEmail())
        database.setValue(deputado.toSimpleMap(false), ({ _, _ ->
            Log.i(LOG_TAG, "New deputado on main list")
        }))

    }

    private fun addDeputadoOnPreList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).child(deputado.email?.encodeEmail())
        database.setValue(deputado.toSimpleMap(true), ({ _, _ ->

        }))

    }

    private fun removeSenadorFromMainList(senador: Politician) {
        mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador.email?.encodeEmail()).removeValue()
    }

    private fun removeDeputadoFromMainList(deputado: Politician) {
        mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).child(deputado.email?.encodeEmail()).removeValue()
    }

    private fun saveSenadoresOnMainList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR || it.post == Politician.Post.SENADORA }
                .forEach { senador -> mapSenadores.put("/${senador.email?.encodeEmail()}/", senador.toSimpleMap(false)) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                if (error != null)
                    Log.e(LOG_TAG, error.message)

                Log.i(LOG_TAG, reference?.toString())
            }
        })
    }

    fun saveSenadoresOnPreList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR || it.post == Politician.Post.SENADORA }
                .forEach { senador -> mapSenadores.put("/${senador.email?.encodeEmail()}/", senador.toSimpleMap(true)) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })
    }

    private fun saveDeputadosOnMainList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO || it.post == Politician.Post.DEPUTADA }
                .forEach { deputado -> mapSenadores.put("/${deputado.email?.encodeEmail()}/", deputado.toSimpleMap(false)) }

        database.updateChildren(mapSenadores, { _, _ -> })
    }

    fun saveDeputadosOnPreList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO || it.post == Politician.Post.DEPUTADA }
                .forEach { deputado -> mapSenadores.put("/${deputado.email?.encodeEmail()}/", deputado.toSimpleMap(true)) }

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
                .forEach { governador -> mapSenadores.put("/${governador.email?.encodeEmail()}/", governador.toSimpleMap(true)) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })
    }
}