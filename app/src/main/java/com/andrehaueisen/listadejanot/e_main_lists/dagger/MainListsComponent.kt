package com.andrehaueisen.listadejanot.e_main_lists.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.e_main_lists.MainListsPresenterActivity
import dagger.Component

/**
 * Created by andre on 11/2/2017.
 */
@MainListsScope
@Component(dependencies = arrayOf(ApplicationComponent::class))
interface MainListsComponent {

    fun inject(mainListsPresenterActivity: MainListsPresenterActivity)
}