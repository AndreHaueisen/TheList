package com.andrehaueisen.listadejanot.E_add_politician.dagger

import com.andrehaueisen.listadejanot.D_main_list.dagger.MainListComponent
import dagger.Component

/**
 * Created by andre on 5/11/2017.
 */
@AddPoliticianScope
@Component(dependencies = arrayOf(MainListComponent::class))
interface AddPoliticianComponent {

    /*fun injectAddPoliticianModel(addPoliticianPresenterActivity: AddPoliticianPresenterActivity)*/
}