package com.andrehaueisen.listadejanot.d_main_list.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 4/21/2017.
 */

@Module
class MainListModule(private val loaderManager: LoaderManager){

    @MainListScope
    @Provides
    fun provideLoaderManager() : LoaderManager = loaderManager

    @MainListScope
    @Provides
    fun provideMainListModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository) : MainListModel = MainListModel(context, loaderManager, firebaseRepository)
}
