package com.andrehaueisen.listadejanot.b_firebase

import android.util.Log
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
class FirebaseRepository(val mDatabaseReference: DatabaseReference) {

    private val LOG_TAG: String = FirebaseRepository::class.java.simpleName

    private val mPublishSenadoresMainList: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPublishSenadoresPreList: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPublishDeputadosMainList: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPublishDeputadosPreList: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPublishGovernadoresMainList: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPublishGovernadoresPreList: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPublishUser: PublishSubject<User> = PublishSubject.create()
    private val mPublishVoteCountList: PublishSubject<HashMap<String, Long>> = PublishSubject.create()
    private lateinit var mPublishOpinionsList: PublishSubject<Pair<FirebaseAction, DataSnapshot>>

    private val mMainListSenadores = ArrayList<Politician>()
    private val mPreListSenadores = ArrayList<Politician>()
    private val mMainListDeputados = ArrayList<Politician>()
    private val mPreListDeputados = ArrayList<Politician>()
    private val mMainListGovernadores = ArrayList<Politician>()
    private val mPreListGovernadores = ArrayList<Politician>()

    enum class FirebaseAction{
        CHILD_ADDED, CHILD_REMOVED, CHILD_CHANGED
    }

    private val mGenericIndicator = object : GenericTypeIndicator<Politician>() {}

    fun handleSenadorVoteOnDatabase(senador: Politician,
                                    userEmail: String,
                                    viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                    politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun changeIsSenadorOnMainListStatus(email: String, isOnMainList: Boolean) {
            val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).child(email.encodeEmail())
            val mutableMap = mutableMapOf<String, Any>()
            mutableMap.put(CHILD_LOCATION_IS_ON_MAIN_LIST, isOnMainList)
            database.updateChildren(mutableMap)
        }

        fun updateSenadorVoteOnMainList(senador: Politician,
                                        userEmail: String,
                                        viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador.email.encodeEmail())

            database.runTransaction(object : Transaction.Handler {

                override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {
                    if (isSuccessful && dataSnapshot.exists()) {

                        val updatedSenador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                        senador.condemnedBy = updatedSenador.condemnedBy
                        senador.votesNumber = updatedSenador.votesNumber
                        if (viewHolder != null) {
                            if (updatedSenador.condemnedBy.contains(userEmail.encodeEmail())) {
                                viewHolder.initiateCondemnAnimations(senador)
                            } else {
                                viewHolder.initiateAbsolveAnimations(senador)
                            }
                        }

                        changeIsSenadorOnMainListStatus(senador.email, isOnMainList = true)
                        viewHolder?.notifyPoliticianAddedToMainList(senador.email)

                    } else if (!dataSnapshot.exists()) {

                        viewHolder?.notifyPoliticianRemovedFromMainList(senador)
                        changeIsSenadorOnMainListStatus(senador.email, isOnMainList = false)
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    if (senador.votesNumber < VOTES_TO_MAIN_LIST_THRESHOLD && !DEFAULT_POLITICIANS_MAIN_LIST.contains(senador.email)) {
                        mutableData.value = null
                        return Transaction.success(mutableData)
                    }

                    mutableData.value = senador.toSimpleMap(false)
                    return Transaction.success(mutableData)

                }
            })
        }

        fun updateSenadorVoteOnPreList(senador: Politician, userEmail: String, politicianSelectorView: PoliticianSelectorMvpContract.View?) {

            if (senador.post == Politician.Post.SENADOR || senador.post == Politician.Post.SENADORA) {
                val database = mDatabaseReference
                        .child(LOCATION_SENADORES_PRE_LIST)
                        .child(senador.email.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedSenador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedSenador.email = senador.email

                            updateVoteCountOnFirebase(updatedSenador)
                            updateUserVoteList(userEmail, updatedSenador)
                            if (updatedSenador.isOnMainList || (!updatedSenador.isOnMainList && updatedSenador.votesNumber >= VOTES_TO_MAIN_LIST_THRESHOLD)) {
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

        updateSenadorVoteOnPreList(senador, userEmail, politicianSelectorView)

    }

    fun handleDeputadoVoteOnDatabase(deputado: Politician,
                                     userEmail: String,
                                     viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                     politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun changeIsDeputadoOnMainListStatus(email: String, isOnMainList: Boolean) {
            val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).child(email.encodeEmail())
            val mutableMap = mutableMapOf<String, Any>()
            mutableMap.put(CHILD_LOCATION_IS_ON_MAIN_LIST, isOnMainList)
            database.updateChildren(mutableMap)
        }

        fun updateDeputadoVoteOnMainList(deputado: Politician,
                                         userEmail: String,
                                         viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference
                    .child(LOCATION_DEPUTADOS_MAIN_LIST)
                    .child(deputado.email.encodeEmail())

            database.runTransaction(object : Transaction.Handler {

                override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                    if (isSuccessful && dataSnapshot.exists()) {

                        val updatedDeputado: Politician = dataSnapshot.getValue(Politician::class.java)!!
                        deputado.condemnedBy = updatedDeputado.condemnedBy
                        deputado.votesNumber = updatedDeputado.votesNumber
                        if (viewHolder != null) {
                            if (updatedDeputado.condemnedBy.contains(userEmail.encodeEmail())) {
                                viewHolder.initiateCondemnAnimations(deputado)
                            } else {
                                viewHolder.initiateAbsolveAnimations(deputado)
                            }
                        }

                        changeIsDeputadoOnMainListStatus(deputado.email, isOnMainList = true)
                        viewHolder?.notifyPoliticianAddedToMainList(deputado.email)

                    } else if (!dataSnapshot.exists()) {

                        viewHolder?.notifyPoliticianRemovedFromMainList(deputado)
                        changeIsDeputadoOnMainListStatus(deputado.email, isOnMainList = false)
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    if (deputado.votesNumber < VOTES_TO_MAIN_LIST_THRESHOLD && !DEFAULT_POLITICIANS_MAIN_LIST.contains(deputado.email)) {
                        mutableData.value = null
                        return Transaction.success(mutableData)
                    }

                    mutableData.value = deputado.toSimpleMap(false)
                    return Transaction.success(mutableData)
                }
            })
        }

        fun updateDeputadoVoteOnPreList(deputado: Politician,
                                        userEmail: String,
                                        politicianSelectorView: PoliticianSelectorMvpContract.View?) {
            if (deputado.post == Politician.Post.DEPUTADO || deputado.post == Politician.Post.DEPUTADA) {
                val database = mDatabaseReference
                        .child(LOCATION_DEPUTADOS_PRE_LIST)
                        .child(deputado.email.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedDeputado: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedDeputado.email = deputado.email

                            updateVoteCountOnFirebase(updatedDeputado)
                            updateUserVoteList(userEmail, updatedDeputado)
                            if (updatedDeputado.isOnMainList || (!updatedDeputado.isOnMainList && updatedDeputado.votesNumber >= VOTES_TO_MAIN_LIST_THRESHOLD)) {
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

        updateDeputadoVoteOnPreList(deputado, userEmail, politicianSelectorView)
    }

    fun handleGovernadorVoteOnDatabase(governador: Politician,
                                       userEmail: String,
                                       viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                       politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun changeIsGovernadorOnMainListStatus(email: String, isOnMainList: Boolean) {
            val database = mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST).child(email.encodeEmail())
            val mutableMap = mutableMapOf<String, Any>()
            mutableMap.put(CHILD_LOCATION_IS_ON_MAIN_LIST, isOnMainList)
            database.updateChildren(mutableMap)
        }

        fun updateGovernadorVoteOnMainList(governador: Politician,
                                           userEmail: String,
                                           viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference
                    .child(LOCATION_GOVERNADORES_MAIN_LIST)
                    .child(governador.email.encodeEmail())

            database.runTransaction(object : Transaction.Handler {

                override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                    if (isSuccessful && dataSnapshot.exists()) {

                        val updatedGovernador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                        governador.condemnedBy = updatedGovernador.condemnedBy
                        governador.votesNumber = updatedGovernador.votesNumber
                        if (viewHolder != null) {
                            if (updatedGovernador.condemnedBy.contains(userEmail.encodeEmail())) {
                                viewHolder.initiateCondemnAnimations(governador)
                            } else {
                                viewHolder.initiateAbsolveAnimations(governador)
                            }
                        }

                        changeIsGovernadorOnMainListStatus(governador.email, isOnMainList = true)
                        viewHolder?.notifyPoliticianAddedToMainList(governador.email)

                    } else if (!dataSnapshot.exists()) {

                        viewHolder?.notifyPoliticianRemovedFromMainList(governador)
                        changeIsGovernadorOnMainListStatus(governador.email, isOnMainList = false)
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    if (governador.votesNumber < VOTES_TO_MAIN_LIST_THRESHOLD && !DEFAULT_POLITICIANS_MAIN_LIST.contains(governador.email)) {
                        mutableData.value = null
                        return Transaction.success(mutableData)
                    }

                    mutableData.value = governador.toSimpleMap(false)
                    return Transaction.success(mutableData)
                }
            })
        }

        fun updateGovernadorVoteOnPreList(governador: Politician,
                                        userEmail: String,
                                        politicianSelectorView: PoliticianSelectorMvpContract.View?) {
            if (governador.post == Politician.Post.GOVERNADOR || governador.post == Politician.Post.GOVERNADORA) {
                val database = mDatabaseReference
                        .child(LOCATION_GOVERNADORES_PRE_LIST)
                        .child(governador.email.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedGovernador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedGovernador.email = governador.email

                            updateVoteCountOnFirebase(updatedGovernador)
                            updateUserVoteList(userEmail, updatedGovernador)
                            if (updatedGovernador.isOnMainList || (!updatedGovernador.isOnMainList && updatedGovernador.votesNumber >= VOTES_TO_MAIN_LIST_THRESHOLD)) {
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

        updateGovernadorVoteOnPreList(governador, userEmail, politicianSelectorView)
    }

    private fun updateVoteCountOnFirebase(updatedPolitician: Politician) {

        val database = mDatabaseReference.child(LOCATION_VOTE_COUNT)
        val emailVotesHashMap = HashMap<String, Long>()
        emailVotesHashMap[updatedPolitician.email.encodeEmail()] = updatedPolitician.votesNumber
        database.updateChildren(emailVotesHashMap.toMap())
    }

    private fun updateUserVoteList(userEmail: String, updatedPolitician: Politician) {

        val database = mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail())

        database.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {

                val user: User = mutableData.getValue(User::class.java) ?: User()
                val politicianEmail = updatedPolitician.email.encodeEmail()

                if (updatedPolitician.condemnedBy.contains(userEmail.encodeEmail())) {
                    user.condemnations[politicianEmail] = (ServerValue.TIMESTAMP)
                } else {
                    user.condemnations.remove(politicianEmail)
                }

                mutableData.value = user
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

            }
        })

    }

    private val mListenerForUser = object: ValueEventListener{

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val user: User?
            if(dataSnapshot != null && dataSnapshot.exists()){
                user = dataSnapshot.getValue(User::class.java) ?: User()
            }else{
                user = User()
            }

            mPublishUser.onNext(user)
        }

        override fun onCancelled(error: DatabaseError) {
            mPublishUser.onError(error.toException())
        }
    }

    private val mListenerForVoteCountList = object: ValueEventListener{

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val voteCountList = HashMap<String, Long>()

            if(dataSnapshot != null && dataSnapshot.exists()){

                dataSnapshot.children.forEach { voteCount ->
                    voteCountList[voteCount.key] = voteCount.value as? Long ?: 0
                }
            }

            mPublishVoteCountList.onNext(voteCountList)
        }

        override fun onCancelled(error: DatabaseError) {
            mPublishVoteCountList.onError(error.toException())
        }
    }

    private val mListenerForSenadoresMainList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mMainListSenadores.isNotEmpty()) mMainListSenadores.clear()

            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mMainListSenadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishSenadoresMainList.onNext(mMainListSenadores)

        }

        override fun onCancelled(error: DatabaseError) {
            mPublishSenadoresMainList.onError(error.toException())
        }
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

        override fun onCancelled(error: DatabaseError) {
            mPublishSenadoresPreList.onError(error.toException())
        }
    }

    private val mListenerForDeputadosMainList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mMainListDeputados.isNotEmpty()) mMainListDeputados.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mMainListDeputados.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishDeputadosMainList.onNext(mMainListDeputados)
        }

        override fun onCancelled(error: DatabaseError) {
            mPublishDeputadosMainList.onError(error.toException())
        }
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

        override fun onCancelled(error: DatabaseError) {
            mPublishDeputadosPreList.onError(error.toException())
        }
    }

    private val mListenerForGovernadoresMainList = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            if (mMainListGovernadores.isNotEmpty()) mMainListGovernadores.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mMainListGovernadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishGovernadoresMainList.onNext(mMainListGovernadores)
        }

        override fun onCancelled(error: DatabaseError) {
            mPublishGovernadoresMainList.onError(error.toException())
        }
    }

    private val mListenerForGovernadoresPreList = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            if (mPreListGovernadores.isNotEmpty()) mPreListGovernadores.clear()
            if (dataSnapshot != null && dataSnapshot.exists()) {
                dataSnapshot.children.forEach { if (it != null) mPreListGovernadores.add(it.getValue(mGenericIndicator)!!) }
            }
            mPublishGovernadoresPreList.onNext(mPreListGovernadores)
        }

        override fun onCancelled(error: DatabaseError) {
            mPublishGovernadoresPreList.onError(error.toException())
        }
    }

    private val mListenerForOpinionsList = object : ChildEventListener {

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?) {

            if(dataSnapshot != null && dataSnapshot.exists()) {
                val pair = Pair(FirebaseAction.CHILD_CHANGED, dataSnapshot)
                mPublishOpinionsList.onNext(pair)
            }
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?) {

            if(dataSnapshot != null && dataSnapshot.exists()){
                val pair = Pair(FirebaseAction.CHILD_ADDED, dataSnapshot)
                mPublishOpinionsList.onNext(pair)
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            if(dataSnapshot != null && dataSnapshot.exists()) {
                val pair = Pair(FirebaseAction.CHILD_REMOVED, dataSnapshot)
                mPublishOpinionsList.onNext(pair)
            }
        }

        override fun onCancelled(p0: DatabaseError?) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?) {

        }
    }

    fun getUser(userEmail: String): PublishSubject<User>{
        mDatabaseReference.child(LOCATION_USERS).child(userEmail.encodeEmail()).addListenerForSingleValueEvent(mListenerForUser)
        return mPublishUser
    }

    fun getVoteCountList(): PublishSubject<HashMap<String, Long>>{
        mDatabaseReference.child(LOCATION_VOTE_COUNT).addListenerForSingleValueEvent(mListenerForVoteCountList)
        return mPublishVoteCountList
    }

    fun getSenadoresMainList(): PublishSubject<ArrayList<Politician>> {
        mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).addListenerForSingleValueEvent(mListenerForSenadoresMainList)

        return mPublishSenadoresMainList
    }

    fun getSenadoresPreList(): PublishSubject<ArrayList<Politician>> {
        mDatabaseReference
                .child(LOCATION_SENADORES_PRE_LIST)
                .addListenerForSingleValueEvent(mListenerForSenadoresPreList)

        return mPublishSenadoresPreList
    }

    fun getDeputadosMainList(): PublishSubject<ArrayList<Politician>> {
        mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).addListenerForSingleValueEvent(mListenerForDeputadosMainList)

        return mPublishDeputadosMainList
    }

    fun getDeputadosPreList(): PublishSubject<ArrayList<Politician>> {
        mDatabaseReference
                .child(LOCATION_DEPUTADOS_PRE_LIST)
                .addListenerForSingleValueEvent(mListenerForDeputadosPreList)

        return mPublishDeputadosPreList
    }

    fun getGovernadoresMainList(): PublishSubject<ArrayList<Politician>>{
        mDatabaseReference.child(LOCATION_GOVERNADORES_MAIN_LIST).addListenerForSingleValueEvent(mListenerForGovernadoresMainList)

        return mPublishGovernadoresMainList
    }

    fun getGovernadoresPreList(): PublishSubject<ArrayList<Politician>>{
        mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST).addListenerForSingleValueEvent(mListenerForGovernadoresPreList)

        return mPublishGovernadoresPreList
    }

    fun getPoliticianOpinions(politicianEmail: String): PublishSubject<Pair<FirebaseAction, DataSnapshot>>{
        mPublishOpinionsList  = PublishSubject.create()

        mDatabaseReference
                .child(LOCATION_OPINIONS_ON_POLITICIANS)
                .child(politicianEmail.encodeEmail())
                .addChildEventListener(mListenerForOpinionsList)

        return mPublishOpinionsList
    }

    fun completePublishOptionsList(){
        mPublishOpinionsList.onComplete()
    }

    fun onDestroy() {
        mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).removeEventListener(mListenerForSenadoresMainList)
        mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).removeEventListener(mListenerForSenadoresPreList)
        mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).removeEventListener(mListenerForDeputadosMainList)
        mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).removeEventListener(mListenerForDeputadosPreList)
        mDatabaseReference.child(LOCATION_GOVERNADORES_MAIN_LIST).removeEventListener(mListenerForGovernadoresMainList)
        mDatabaseReference.child(LOCATION_GOVERNADORES_PRE_LIST).removeEventListener(mListenerForDeputadosPreList)
    }

    fun addOpinionOnPolitician(politicianEmail: String, userEmail: String, opinion: String){
        mDatabaseReference
                .child(LOCATION_OPINIONS_ON_POLITICIANS)
                .child(politicianEmail.encodeEmail())
                .child(userEmail.encodeEmail())
                .setValue(opinion)
    }

    fun removeOpinion(politicianEmail: String, userEmail: String?){
        userEmail?.let {
            mDatabaseReference
                    .child(LOCATION_OPINIONS_ON_POLITICIANS)
                    .child(politicianEmail.encodeEmail())
                    .child(userEmail.encodeEmail())
                    .removeValue()
        }
    }

    fun killFirebaseListener(politicianEmail: String){
        mDatabaseReference
                .child(LOCATION_OPINIONS_ON_POLITICIANS)
                .child(politicianEmail.encodeEmail())
                .removeEventListener(mListenerForOpinionsList)
    }

    private fun addSenadorOnMainList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador.email.encodeEmail())
        database.setValue(senador.toSimpleMap(false), ({ _, _ ->
            Log.i(LOG_TAG, "New senador on main list")
        }))

    }

    private fun addSenadorOnPreList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).child(senador.email.encodeEmail())
        database.setValue(senador.toSimpleMap(true), ({ _, _ ->

        }))

    }

    private fun addDeputadoOnMainList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).child(deputado.email.encodeEmail())
        database.setValue(deputado.toSimpleMap(false), ({ _, _ ->
            Log.i(LOG_TAG, "New deputado on main list")
        }))

    }

    private fun addDeputadoOnPreList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).child(deputado.email.encodeEmail())
        database.setValue(deputado.toSimpleMap(true), ({ _, _ ->

        }))

    }

    private fun removeSenadorFromMainList(senador: Politician) {
        mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador.email.encodeEmail()).removeValue()
    }

    private fun removeDeputadoFromMainList(deputado: Politician) {
        mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).child(deputado.email.encodeEmail()).removeValue()
    }

    private fun saveSenadoresOnMainList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR || it.post == Politician.Post.SENADORA }
                .forEach { senador -> mapSenadores.put("/${senador.email.encodeEmail()}/", senador.toSimpleMap(false)) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                if (error != null)
                    Log.e(LOG_TAG, error.message)

                Log.i(LOG_TAG, reference?.toString())
            }
        })
    }

    private fun saveSenadoresOnPreList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR || it.post == Politician.Post.SENADORA }
                .forEach { senador -> mapSenadores.put("/${senador.email.encodeEmail()}/", senador.toSimpleMap(true)) }

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
                .forEach { deputado -> mapSenadores.put("/${deputado.email.encodeEmail()}/", deputado.toSimpleMap(false)) }

        database.updateChildren(mapSenadores, { _, _ -> })
    }

    private fun saveDeputadosOnPreList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO || it.post == Politician.Post.DEPUTADA}
                .forEach { deputado -> mapSenadores.put("/${deputado.email.encodeEmail()}/", deputado.toSimpleMap(true)) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })

    }
}