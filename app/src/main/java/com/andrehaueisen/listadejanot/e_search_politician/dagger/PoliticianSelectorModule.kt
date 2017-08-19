package com.andrehaueisen.listadejanot.e_search_politician.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorModel
import com.andrehaueisen.listadejanot.e_search_politician.mvp.SinglePoliticianModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/11/2017.
 */
@Module
class PoliticianSelectorModule(private val mLoaderManager: LoaderManager) {

    @PoliticianSelectorScope
    @Provides
    fun provideLoaderManager() : LoaderManager = mLoaderManager

    @PoliticianSelectorScope
    @Provides
    fun provideSelectorModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository) : PoliticianSelectorModel = PoliticianSelectorModel(context, loaderManager, firebaseRepository)

    @PoliticianSelectorScope
    @Provides
    fun provideIndividualSelectorModel(
            context: Context,
            loaderManager: LoaderManager,
            firebaseRepository: FirebaseRepository,
            firebaseAuthenticator: FirebaseAuthenticator,
            selectorModel: PoliticianSelectorModel): SinglePoliticianModel = SinglePoliticianModel(context, loaderManager, firebaseRepository, firebaseAuthenticator, selectorModel)

}
