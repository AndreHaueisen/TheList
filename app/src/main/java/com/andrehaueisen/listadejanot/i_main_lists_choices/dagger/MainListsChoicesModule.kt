package com.andrehaueisen.listadejanot.i_main_lists_choices.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.i_main_lists_choices.mvp.MainListsChoicesModel
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.decodeEmail
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject
import javax.inject.Named

/**
 * Created by andre on 10/22/2017.
 */

@Module
class MainListsChoicesModule(private val mLoaderManager: LoaderManager) {

    private val mListReadyPublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    @MainListsChoicesScope
    @Provides
    @Named("deputados_listener")
    fun provideDeputadosValueEventListener(@Named("deputados_list") deputados: ArrayList<Politician>): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (deputados.isNotEmpty()) {
                    deputados.clear()
                }

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    val genericIndicator = object : GenericTypeIndicator<Politician>() {}

                    dataSnapshot.children.forEach { snapshot ->
                        if (snapshot != null) {
                            val senador = (snapshot.getValue(genericIndicator) as Politician)
                            senador.email = snapshot.key.decodeEmail()
                            deputados.add(senador)
                        }
                    }
                    mListReadyPublishSubject.onNext(true)
                }
            }

            override fun onCancelled(error: DatabaseError?) {}
        }
    }

    @MainListsChoicesScope
    @Provides
    @Named("senadores_listener")
    fun provideSenadoresValueEventListener(@Named("senadores_list") senadores: ArrayList<Politician>): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (senadores.isNotEmpty()) {
                    senadores.clear()
                }

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    val genericIndicator = object : GenericTypeIndicator<Politician>() {}

                    dataSnapshot.children.forEach { snapshot ->
                        if (snapshot != null) {
                            val senador = (snapshot.getValue(genericIndicator) as Politician)
                            senador.email = snapshot.key.decodeEmail()
                            senadores.add(senador)
                        }
                    }
                }
                mListReadyPublishSubject.onNext(true)
            }

            override fun onCancelled(error: DatabaseError?) {}
        }
    }

    @MainListsChoicesScope
    @Provides
    @Named("governadores_listener")
    fun provideGovernadoresValueEventListener(@Named("governadores_list") governadores: ArrayList<Politician>): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (governadores.isNotEmpty()) {
                    governadores.clear()
                }

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    val genericIndicator = object : GenericTypeIndicator<Politician>() {}

                    dataSnapshot.children.forEach { snapshot ->
                        if (snapshot != null) {
                            val senador = (snapshot.getValue(genericIndicator) as Politician)
                            senador.email = snapshot.key.decodeEmail()
                            governadores.add(senador)
                        }
                    }
                }
                mListReadyPublishSubject.onNext(true)
            }

            override fun onCancelled(error: DatabaseError?) {}
        }
    }

    @MainListsChoicesScope
    @Provides
    @Named("user_listener")
    fun provideUserValueEventListener(user: User) = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val refreshedUser: User = dataSnapshot?.getValue(User::class.java) ?: User()

            user.refreshUser(refreshedUser)
        }

        override fun onCancelled(error: DatabaseError?) {}
    }

    @MainListsChoicesScope
    @Provides
    fun provideLoaderManager(): LoaderManager = mLoaderManager

    @MainListsChoicesScope
    @Provides
    fun provideMainListsModel(@Named("deputados_list") deputados: ArrayList<Politician>,
                              @Named("senadores_list") senadores: ArrayList<Politician>,
                              @Named("governadores_list") governadores: ArrayList<Politician>,
                              context: Context,
                              loaderManager: LoaderManager)

            = MainListsChoicesModel(deputados, senadores, governadores, loaderManager, context, mListReadyPublishSubject)

}
