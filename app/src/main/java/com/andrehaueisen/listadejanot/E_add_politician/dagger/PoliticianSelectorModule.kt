package com.andrehaueisen.listadejanot.E_add_politician.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.B_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.E_add_politician.mvp.PoliticianSelectorModel
import com.andrehaueisen.listadejanot.E_add_politician.mvp.SinglePoliticianModel
import com.andrehaueisen.listadejanot.models.Politician
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/11/2017.
 */
@Module
class PoliticianSelectorModule(
        val mLoaderManager: LoaderManager,
        val mSenadoresMainList: ArrayList<Politician>,
        val mDeputadosMainList: ArrayList<Politician>) {

    @PoliticianSelectorScope
    @Provides
    fun provideLoaderManager() : LoaderManager {
        return mLoaderManager
    }

    @PoliticianSelectorScope
    @Provides
    fun provideSelectorModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository) : PoliticianSelectorModel {
        return PoliticianSelectorModel(context, loaderManager, firebaseRepository, mSenadoresMainList, mDeputadosMainList)
    }

    @PoliticianSelectorScope
    @Provides
    fun provideIndividualSelectorModel(
            context: Context,
            loaderManager: LoaderManager,
            firebaseRepository: FirebaseRepository,
            firebaseAuthenticator: FirebaseAuthenticator,
            selectorModel: PoliticianSelectorModel): SinglePoliticianModel {

        return SinglePoliticianModel(context, loaderManager, firebaseRepository, firebaseAuthenticator, selectorModel.getSearchablePoliticiansList())
    }

}
