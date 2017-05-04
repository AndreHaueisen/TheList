package com.andrehaueisen.listadejanot.D_firebase.dagger

import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.google.firebase.database.DatabaseReference
import dagger.Component

/**
 * Created by andre on 5/3/2017.
 */
@FirebaseScope
@Component(dependencies = arrayOf(ApplicationComponent::class))
interface FirebaseComponent {

    fun databaseReference() : DatabaseReference
}