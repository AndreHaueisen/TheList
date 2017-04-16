package com.andrehaueisen.listadejanot.application.dagger

import dagger.Component

/**
 * Created by andre on 4/15/2017.
 */
@ApplicationScope
@Component(modules = arrayOf(ContextModule::class))
interface ApplicationComponent{

}