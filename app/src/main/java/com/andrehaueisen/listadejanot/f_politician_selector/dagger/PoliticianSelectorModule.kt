package com.andrehaueisen.listadejanot.f_politician_selector.dagger

import android.content.ContentResolver
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorModel
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.SinglePoliticianModel
import com.andrehaueisen.listadejanot.images.ImageFetcherModel
import com.andrehaueisen.listadejanot.images.ImageFetcherService
import com.andrehaueisen.listadejanot.models.Politician
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Created by andre on 5/11/2017.
 */
@Module
class PoliticianSelectorModule(private val mLoaderManager: LoaderManager, private val contentResolver: ContentResolver) {

    @PoliticianSelectorScope
    @Provides
    fun provideLoaderManager(): LoaderManager = mLoaderManager

    @PoliticianSelectorScope
    @Provides
    fun provideSelectorModel(@Named("deputados_list") deputados: ArrayList<Politician>,
                             @Named("senadores_list") senadores: ArrayList<Politician>,
                             @Named("governadores_list") governadores: ArrayList<Politician>,
                             @Named("presidentes_list") presidentes: ArrayList<Politician>): PoliticianSelectorModel =
            PoliticianSelectorModel(deputados, senadores, governadores, presidentes)

    @PoliticianSelectorScope
    @Provides
    fun provideIndividualSelectorModel(
            firebaseRepository: FirebaseRepository,
            firebaseAuthenticator: FirebaseAuthenticator,
            selectorModel: PoliticianSelectorModel): SinglePoliticianModel = SinglePoliticianModel(firebaseRepository, firebaseAuthenticator, selectorModel)

    @PoliticianSelectorScope
    @Provides
    fun provideImageFetcherModel(imageFetcherService: ImageFetcherService) = ImageFetcherModel(imageFetcherService, contentResolver)

}
