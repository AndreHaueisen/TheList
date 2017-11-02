package com.andrehaueisen.listadejanot.i_main_lists_choices.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.i_main_lists_choices.mvp.MainListsChoicesPresenterActivity
import dagger.Component


/**
 * Created by andre on 10/22/2017.
 */
@MainListsChoicesScope
@Component(modules = arrayOf(MainListsChoicesModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface MainListsChoicesComponent {

    fun inject(mainListsChoicesPresenterActivity: MainListsChoicesPresenterActivity)
}
