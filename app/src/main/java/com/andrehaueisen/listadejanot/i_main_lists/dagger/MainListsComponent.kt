package com.andrehaueisen.listadejanot.i_main_lists.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.i_main_lists.mvp.MainListsPresenterActivity
import dagger.Component


/**
 * Created by andre on 10/22/2017.
 */
@MainListsScope
@Component(modules = arrayOf(MainListsModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface MainListsComponent {

    fun inject(mainListsPresenterActivity: MainListsPresenterActivity)
}
