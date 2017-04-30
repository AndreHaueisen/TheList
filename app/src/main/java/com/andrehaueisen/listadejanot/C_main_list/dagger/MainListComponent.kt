package com.andrehaueisen.listadejanot.C_main_list.dagger

import com.andrehaueisen.listadejanot.C_main_list.mvp.MainListPresenterActivity
import dagger.Component

/**
 * Created by andre on 4/21/2017.
 */

@Component(modules = arrayOf(MainListModule::class))
interface MainListComponent{

    fun injectModel(mainListActivity: MainListPresenterActivity)


}
