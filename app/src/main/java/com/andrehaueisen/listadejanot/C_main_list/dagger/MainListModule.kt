package com.andrehaueisen.listadejanot.C_main_list.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.C_main_list.mvp.MainListModel
import com.andrehaueisen.listadejanot.D_firebase.FirebaseRepository
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 4/21/2017.
 */

@Module
class MainListModule(val context: Context, val loaderManager: LoaderManager){

    @MainListScope
    @Provides
    fun provideContext() : Context{
        return context
    }

    @MainListScope
    @Provides
    fun provideLoaderManager() : LoaderManager{
        return loaderManager
    }

    @MainListScope
    @Provides
    fun provideListFirebaseDatabase(context: Context) : FirebaseRepository {
        return FirebaseRepository(context)
    }

    @MainListScope
    @Provides
    fun provideMainListModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository) : MainListModel {
        return MainListModel(context, loaderManager, firebaseRepository)
    }
}
