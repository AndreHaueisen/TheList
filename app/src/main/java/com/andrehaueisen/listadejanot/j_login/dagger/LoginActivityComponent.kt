package com.andrehaueisen.listadejanot.j_login.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.j_login.LoginActivity
import dagger.Component

/**
 * Created by andre on 6/8/2017.
 */
@LoginActivityScope
@Component(dependencies = arrayOf(ApplicationComponent::class))
interface LoginActivityComponent{

    fun injectFirebaseAuthenticator(loginActivity: LoginActivity)
}