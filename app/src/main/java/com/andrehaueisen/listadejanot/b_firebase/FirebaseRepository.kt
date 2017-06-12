package com.andrehaueisen.listadejanot.b_firebase

import android.util.Log
import com.andrehaueisen.listadejanot.d_main_list.PoliticianListAdapter
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorMvpContract
import com.andrehaueisen.listadejanot.models.Politician
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

    private val mMainListSenadores = ArrayList<Politician>()
    private val mPreListSenadores = ArrayList<Politician>()
    private val mMainListDeputados = ArrayList<Politician>()
    private val mPreListDeputados = ArrayList<Politician>()

    private val mGenericIndicator = object : GenericTypeIndicator<Politician>() {}

    fun updateSenadorVoteOnBothLists(senador: Politician,
                                     userEmail: String,
                                     viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                     politicianSelectorView: PoliticianSelectorMvpContract.View?) {

        fun updateSenadorVoteOnMainList(senador: Politician,
                                        userEmail: String,
                                        viewHolder: PoliticianListAdapter.PoliticianHolder?) {

            val database = mDatabaseReference
                    .child(LOCATION_SENADORES_MAIN_LIST)
                    .child(senador.email.encodeEmail())

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
                        Log.i(LOG_TAG, dataSnapshot.toString())

                    } else {
                        viewHolder?.initiateAbsolveAnimations(senador)
                        Log.i(LOG_TAG, "Senador removed from main list")
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    if (senador.votesNumber == VOTES_TO_MAIN_LIST_THRESHOLD) {
                        addSenadorOnMainList(senador)

                    } else if (senador.votesNumber == VOTES_TO_MAIN_LIST_THRESHOLD - 1) {
                        removeSenadorFromMainList(senador)
                    }

                    mutableData.value = senador.toSimpleMap()
                    return Transaction.success(mutableData)

                }
            })
        }

        fun updateSenadorVoteOnPreList(senador: Politician,
                                       userEmail: String,
                                       politicianSelectorView: PoliticianSelectorMvpContract.View?) {
            if (senador.post == Politician.Post.SENADOR) {
                val database = mDatabaseReference
                        .child(LOCATION_SENADORES_PRE_LIST)
                        .child(senador.email.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedSenador: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedSenador.email = senador.email
                            updateSenadorVoteOnMainList(updatedSenador, userEmail, viewHolder)

                            senador.condemnedBy = updatedSenador.condemnedBy
                            senador.votesNumber = updatedSenador.votesNumber

                            if (politicianSelectorView is PoliticianSelectorMvpContract.View) {
                                if (updatedSenador.condemnedBy.contains(userEmail.encodeEmail())) {
                                    politicianSelectorView.initiateCondemnAnimations(senador)
                                } else {
                                    politicianSelectorView.initiateAbsolveAnimations(senador)
                                }
                                Log.i(LOG_TAG, dataSnapshot.toString())
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
                            senadorRemote.condemnedBy.add(encodedEmail)
                        }

                        mutableData.value = senadorRemote
                        return Transaction.success(mutableData)
                    }
                })
            }


        }

        updateSenadorVoteOnPreList(senador, userEmail, politicianSelectorView)

    }

    fun updateDeputadoVoteOnBothLists(deputado: Politician,
                                      userEmail: String,
                                      viewHolder: PoliticianListAdapter.PoliticianHolder?,
                                      politicianSelectorView: PoliticianSelectorMvpContract.View?) {

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

                        Log.i(LOG_TAG, dataSnapshot.toString())
                    } else {
                        viewHolder?.initiateAbsolveAnimations(deputado)
                        Log.e(LOG_TAG, error.toString())
                    }
                }

                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                    if (deputado.votesNumber == VOTES_TO_MAIN_LIST_THRESHOLD) {
                        addDeputadoOnMainList(deputado)

                    } else if (deputado.votesNumber == VOTES_TO_MAIN_LIST_THRESHOLD - 1) {
                        removeDeputadoFromMainList(deputado)
                    }

                    mutableData.value = deputado.toSimpleMap()
                    return Transaction.success(mutableData)
                }
            })
        }


        fun updateDeputadoVoteOnPreList(deputado: Politician,
                                        userEmail: String,
                                        politicianSelectorView: PoliticianSelectorMvpContract.View?) {
            if (deputado.post == Politician.Post.DEPUTADO) {
                val database = mDatabaseReference
                        .child(LOCATION_DEPUTADOS_PRE_LIST)
                        .child(deputado.email.encodeEmail())

                database.runTransaction(object : Transaction.Handler {

                    override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot) {

                        if (isSuccessful && dataSnapshot.exists()) {

                            val updatedDeputado: Politician = dataSnapshot.getValue(Politician::class.java)!!
                            updatedDeputado.email = deputado.email
                            updateDeputadoVoteOnMainList(updatedDeputado, userEmail, viewHolder)

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
                            deputadoRemote.condemnedBy.add(encodedEmail)
                        }

                        mutableData.value = deputadoRemote
                        return Transaction.success(mutableData)

                    }
                })
            }
        }

        updateDeputadoVoteOnPreList(deputado, userEmail, politicianSelectorView)
    }


    val mListenerForSenadoresMainList = object : ValueEventListener {

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

    val mListenerForSenadoresPreList = object : ValueEventListener {

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

    val mListenerForDeputadosMainList = object : ValueEventListener {

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

    val mListenerForDeputadosPreList = object : ValueEventListener {

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

    fun onDestroy() {
        mDatabaseReference.removeEventListener(mListenerForSenadoresMainList)
        mDatabaseReference.removeEventListener(mListenerForSenadoresPreList)
        mDatabaseReference.removeEventListener(mListenerForDeputadosMainList)
        mDatabaseReference.removeEventListener(mListenerForDeputadosPreList)

    }

    //TODO put these functions on Firebase functions when possible
    private fun addSenadorOnMainList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_MAIN_LIST).child(senador.email.encodeEmail())
        database.setValue(senador.toSimpleMap(), ({ _, _ ->
            Log.i(LOG_TAG, "New senador on main list")
        }))

    }

    private fun addSenadorOnPreList(senador: Politician) {

        val database = mDatabaseReference.child(LOCATION_SENADORES_PRE_LIST).child(senador.email.encodeEmail())
        database.setValue(senador.toSimpleMap(), ({ error, reference ->
            //TODO notify completion or error

        }))

    }

    private fun addDeputadoOnMainList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST).child(deputado.email.encodeEmail())
        database.setValue(deputado.toSimpleMap(), ({ _, _ ->
            Log.i(LOG_TAG, "New deputado on main list")
        }))

    }

    private fun addDeputadoOnPreList(deputado: Politician) {

        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST).child(deputado.email.encodeEmail())
        database.setValue(deputado.toSimpleMap(), ({ error, reference ->
            //TODO notify completion or error

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
        senadores.filter { it.post == Politician.Post.SENADOR }
                .forEach { senador -> mapSenadores.put("/${senador.email.encodeEmail()}/", senador.toSimpleMap()) }

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
        senadores.filter { it.post == Politician.Post.SENADOR }
                .forEach { senador -> mapSenadores.put("/${senador.email.encodeEmail()}/", senador.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })
    }

    fun saveDeputadosOnMainList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_MAIN_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO }
                .forEach { deputado -> mapSenadores.put("/${deputado.email.encodeEmail()}/", deputado.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                //TODO notify completion or error
            }
        })
    }

    fun saveDeputadosOnPreList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(LOCATION_DEPUTADOS_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO }
                .forEach { deputado -> mapSenadores.put("/${deputado.email.encodeEmail()}/", deputado.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                Log.e(LOG_TAG, error.toString())
            }
        })

    }
}