package com.andrehaueisen.listadejanot.D_main_list.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.D_main_list.mvp.MainListModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 4/21/2017.
 */

@Module
class MainListModule(val loaderManager: LoaderManager){

    @MainListScope
    @Provides
    fun provideLoaderManager() : LoaderManager{
        return loaderManager
    }

    @MainListScope
    @Provides
    fun provideMainListModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository) : MainListModel {
        return MainListModel(context, loaderManager, firebaseRepository)
    }
}
