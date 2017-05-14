package com.andrehaueisen.listadejanot.E_add_politician.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.E_add_politician.mvp.PoliticianSelectorModel
import com.andrehaueisen.listadejanot.models.Politician
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/11/2017.
 */
@Module
class AddPoliticianModule(val mLoaderManager: LoaderManager, val mSenadoresMainList: ArrayList<Politician>, val mDeputadosMainList: ArrayList<Politician>) {

    @AddPoliticianScope
    @Provides
    fun provideLoaderManager() : LoaderManager {
        return mLoaderManager
    }

    @AddPoliticianScope
    @Provides
    fun provideAddPoliticianModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository) : PoliticianSelectorModel {
        return PoliticianSelectorModel(context, loaderManager, firebaseRepository, mSenadoresMainList, mDeputadosMainList)
    }

}
