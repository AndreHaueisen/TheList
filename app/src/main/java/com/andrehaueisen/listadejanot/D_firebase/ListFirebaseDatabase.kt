package com.andrehaueisen.listadejanot.D_firebase

import com.andrehaueisen.listadejanot.A_application.BaseApplication
import com.andrehaueisen.listadejanot.D_firebase.dagger.DaggerFirebaseComponent
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import com.google.firebase.database.*

/**
 * Created by andre on 5/3/2017.
 */
class ListFirebaseDatabase {

    val mBaseApplication = BaseApplication()
    val mDatabaseReference: DatabaseReference = DaggerFirebaseComponent.builder()
            .applicationComponent(mBaseApplication.getAppComponent())
            .build()
            .databaseReference()

    fun saveUserIdOnLogin(uid: String, userEmail: String) {
        val database = mDatabaseReference
                .child(Constants.LOCATION_UID_MAPPINGS)

        val uidMapping = mapOf(Pair(uid, userEmail))
        database.setValue(uidMapping)
    }

    fun updateSenadorVoteOnMainList(senador: Politician, userEmail: String){
        val database = mDatabaseReference
                .child(Constants.LOCATION_SENADORES_MAIN_LIST)
                .child(senador.email.encodeEmail())

        database.runTransaction(object : Transaction.Handler{

            override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot?) {
                //TODO notify completion or error
            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val senadorRemote : Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                val encodedEmail = userEmail.encodeEmail()
                if(senadorRemote.condemnedBy.contains(encodedEmail)){
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

    fun updateSenadorVoteOnPreList(senador: Politician, userEmail: String){
        val database = mDatabaseReference
                .child(Constants.LOCATION_SENADORES_PRE_LIST)
                .child(senador.email.encodeEmail())

        database.runTransaction(object : Transaction.Handler{

            override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot?) {
                //TODO notify completion or error
            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val senadorRemote : Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                val encodedEmail = userEmail.encodeEmail()
                if(senadorRemote.condemnedBy.contains(encodedEmail)){
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

    fun updateDeputadoVoteOnMainList(deputado: Politician, userEmail: String){
        val database = mDatabaseReference
                .child(Constants.LOCATION_DEPUTADOS_MAIN_LIST)
                .child(deputado.email.encodeEmail())

        database.runTransaction(object : Transaction.Handler{

            override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot?) {
                //TODO notify completion or error
            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val deputadoRemote : Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                val encodedEmail = userEmail.encodeEmail()
                if(deputadoRemote.condemnedBy.contains(encodedEmail)){
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

    fun updateDeputadoVoteOnPreList(deputado: Politician, userEmail: String){
        val database = mDatabaseReference
                .child(Constants.LOCATION_DEPUTADOS_PRE_LIST)
                .child(deputado.email.encodeEmail())

        database.runTransaction(object : Transaction.Handler{

            override fun onComplete(error: DatabaseError?, isSuccessful: Boolean, dataSnapshot: DataSnapshot?) {
                //TODO notify completion or error
            }

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val deputadoRemote : Politician = mutableData.getValue(Politician::class.java) ?: return Transaction.success(mutableData)

                val encodedEmail = userEmail.encodeEmail()
                if(deputadoRemote.condemnedBy.contains(encodedEmail)){
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

    fun addSenadorOnMainList(senador: Politician){
        if (senador.post == Politician.Post.SENADOR){
            val database = mDatabaseReference.child(Constants.LOCATION_SENADORES_MAIN_LIST).child(senador.email.encodeEmail())
            database.setValue(senador.toSimpleMap(), ({error, reference ->
                //TODO notify completion or error

            }))
        }
    }

    fun addSenadorOnPreList(senador: Politician){
        if (senador.post == Politician.Post.SENADOR){
            val database = mDatabaseReference.child(Constants.LOCATION_SENADORES_PRE_LIST).child(senador.email.encodeEmail())
            database.setValue(senador.toSimpleMap(), ({error, reference ->
                //TODO notify completion or error

            }))
        }
    }

    fun addDeputadoOnMainList(deputado: Politician){
        if (deputado.post == Politician.Post.DEPUTADO){
            val database = mDatabaseReference.child(Constants.LOCATION_DEPUTADOS_MAIN_LIST).child(deputado.email.encodeEmail())
            database.setValue(deputado.toSimpleMap(), ({ error, reference ->
                //TODO notify completion or error

            }))
        }
    }

    fun addDeputadoOnPreList(deputado: Politician){
        if (deputado.post == Politician.Post.DEPUTADO){
            val database = mDatabaseReference.child(Constants.LOCATION_DEPUTADOS_PRE_LIST).child(deputado.email.encodeEmail())
            database.setValue(deputado.toSimpleMap(), ({ error, reference ->
                //TODO notify completion or error

            }))
        }
    }

    fun saveSenadoresOnMainList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(Constants.LOCATION_SENADORES_MAIN_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR }
                .forEach { senador -> mapSenadores.put("/${senador.email.encodeEmail()}/", senador.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                //TODO notify completion or error
            }
        })
    }

    fun saveSenadoresOnPreList(senadores: ArrayList<Politician>) {
        val database = mDatabaseReference.child(Constants.LOCATION_SENADORES_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        senadores.filter { it.post == Politician.Post.SENADOR }
                .forEach { senador -> mapSenadores.put("/${senador.email.encodeEmail()}/", senador.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                //TODO notify completion or error
            }
        })
    }

    fun saveDeputadosOnMainList(deputados: ArrayList<Politician>) {
        val database = mDatabaseReference.child(Constants.LOCATION_DEPUTADOS_MAIN_LIST)

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
        val database = mDatabaseReference.child(Constants.LOCATION_DEPUTADOS_PRE_LIST)

        val mapSenadores = mutableMapOf<String, Any>()
        deputados.filter { it.post == Politician.Post.DEPUTADO }
                .forEach { deputado -> mapSenadores.put("/${deputado.email.encodeEmail()}/", deputado.toSimpleMap()) }

        database.updateChildren(mapSenadores, object : DatabaseReference.CompletionListener {

            override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                //TODO notify completion or error
            }
        })

    }

    private fun String.encodeEmail(): String {
        return this.replace('.', ',')
    }

    private fun String.decodeEmail(): String {
        return this.replace(',', '.')
    }

}