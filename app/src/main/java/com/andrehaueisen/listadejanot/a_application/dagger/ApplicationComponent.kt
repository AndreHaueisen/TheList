package com.andrehaueisen.listadejanot.a_application.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import dagger.Component
import javax.inject.Named

/**
 * Created by andre on 4/15/2017.
 */
@ApplicationScope
@Component(modules = arrayOf(ContextModule::class, ApplicationModule::class))
interface ApplicationComponent{

    fun loadFirebaseRepository() : FirebaseRepository

    fun loadFirebaseAuthenticator() : FirebaseAuthenticator

    fun loadContext() : Context

    fun loadUser() : User

    @Named("deputados_list")
    fun loadDeputadosList(): ArrayList<Politician>

    @Named("senadores_list")
    fun loadSenadoresList(): ArrayList<Politician>

    @Named("governadores_list")
    fun loadGovernadoresList(): ArrayList<Politician>

    @Named("presidentes_list")
    fun loadPresidentesList(): ArrayList<Politician>

    @Named("media_highlight_list")
    fun loadMediaHighlightList(): ArrayList<String>

}

