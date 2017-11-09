package com.andrehaueisen.listadejanot.d_main_lists_choices.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.d_main_lists_choices.mvp.MainListsChoicesModel
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
                            val deputado = (snapshot.getValue(genericIndicator) as Politician)
                            deputado.email = snapshot.key.decodeEmail()
                            deputados.add(deputado)
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
                            val governador = (snapshot.getValue(genericIndicator) as Politician)
                            governador.email = snapshot.key.decodeEmail()
                            governadores.add(governador)
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
    @Named("presidentes_listener")
    fun providePresidentesValueEventListener(@Named("presidentes_list") presidentes: ArrayList<Politician>): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (presidentes.isNotEmpty()) {
                    presidentes.clear()
                }

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    val genericIndicator = object : GenericTypeIndicator<Politician>() {}

                    dataSnapshot.children.forEach { snapshot ->
                        if (snapshot != null) {
                            val presidente = (snapshot.getValue(genericIndicator) as Politician)
                            presidente.email = snapshot.key.decodeEmail()
                            presidentes.add(presidente)
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
    @Named("media_highlight_listener")
    fun provideMediaHighlightListener(@Named("media_highlight_list") mediaHighlight: ArrayList<String>) = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            dataSnapshot?.children?.forEach { snapshot ->
                val highlight = snapshot?.getValue(String::class.java)!!
                mediaHighlight.add(highlight.decodeEmail())

            }
        }

        override fun onCancelled(p0: DatabaseError?) {}
    }


    @MainListsChoicesScope
    @Provides
    fun provideLoaderManager(): LoaderManager = mLoaderManager

    @MainListsChoicesScope
    @Provides
    fun provideMainListsModel(@Named("deputados_list") deputados: ArrayList<Politician>,
                              @Named("senadores_list") senadores: ArrayList<Politician>,
                              @Named("governadores_list") governadores: ArrayList<Politician>,
                              @Named("presidentes_list") presidentes: ArrayList<Politician>,
                              @Named("media_highlight_list") mediaHighlight: ArrayList<String>,
                              context: Context,
                              loaderManager: LoaderManager)

            = MainListsChoicesModel(deputados, senadores, governadores, presidentes, mediaHighlight, loaderManager, context, mListReadyPublishSubject)

}
