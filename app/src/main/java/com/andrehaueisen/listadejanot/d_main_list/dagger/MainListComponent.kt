package com.andrehaueisen.listadejanot.d_main_list.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListPresenterActivity
import dagger.Component

/**
 * Created by andre on 4/21/2017.
 */
@MainListScope
@Component(modules = arrayOf(MainListModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface MainListComponent{

    fun inject(mainListActivity: MainListPresenterActivity)


}
