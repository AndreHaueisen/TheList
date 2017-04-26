package com.andrehaueisen.listadejanot.A_application.dagger

import com.andrehaueisen.listadejanot.A_application.Application
import dagger.Component

/**
 * Created by andre on 4/15/2017.
 */
@ApplicationScope
@Component(modules = arrayOf(ContextModule::class, AccountModule::class))
interface ApplicationComponent{


    fun injectAccount(application: Application)
}

