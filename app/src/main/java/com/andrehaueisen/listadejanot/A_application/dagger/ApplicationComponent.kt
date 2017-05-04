package com.andrehaueisen.listadejanot.A_application.dagger

import com.google.firebase.database.DatabaseReference
import dagger.Component

/**
 * Created by andre on 4/15/2017.
 */
@ApplicationScope
@Component(modules = arrayOf(ContextModule::class, ApplicationModule::class))
interface ApplicationComponent{

    fun databaseReference() : DatabaseReference
}

