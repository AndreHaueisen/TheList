package com.andrehaueisen.listadejanot.E_add_politician.dagger

import com.andrehaueisen.listadejanot.D_main_list.mvp.MainListModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/11/2017.
 */
@Module
class AddPoliticianModule {

    @AddPoliticianScope
    @Provides
    fun provideMainListModel(mainListModel: MainListModel): MainListModel{
        return mainListModel
    }

}
