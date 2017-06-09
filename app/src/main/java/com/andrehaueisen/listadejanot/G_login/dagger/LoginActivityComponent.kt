package com.andrehaueisen.listadejanot.G_login.dagger

import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.G_login.LoginActivity
import dagger.Component

/**
 * Created by andre on 6/8/2017.
 */
@LoginActivityScope
@Component(dependencies = arrayOf(ApplicationComponent::class))
interface LoginActivityComponent{

    fun injectFirebaseAuthenticator(loginActivity: LoginActivity)
}