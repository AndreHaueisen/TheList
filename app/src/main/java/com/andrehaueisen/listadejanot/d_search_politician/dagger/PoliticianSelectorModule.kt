package com.andrehaueisen.listadejanot.d_search_politician.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.utilities.ImageFetcherModel
import com.andrehaueisen.listadejanot.utilities.ImageFetcherService
import com.andrehaueisen.listadejanot.d_search_politician.mvp.PoliticianSelectorModel
import com.andrehaueisen.listadejanot.d_search_politician.mvp.SinglePoliticianModel
import com.andrehaueisen.listadejanot.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/11/2017.
 */
@Module
class PoliticianSelectorModule(private val mLoaderManager: LoaderManager) {

    @PoliticianSelectorScope
    @Provides
    fun provideLoaderManager(): LoaderManager = mLoaderManager

    @PoliticianSelectorScope
    @Provides
    fun provideSelectorModel(context: Context, loaderManager: LoaderManager, firebaseRepository: FirebaseRepository): PoliticianSelectorModel = PoliticianSelectorModel(context, loaderManager, firebaseRepository)

    @PoliticianSelectorScope
    @Provides
    fun provideIndividualSelectorModel(
            context: Context,
            loaderManager: LoaderManager,
            firebaseRepository: FirebaseRepository,
            firebaseAuthenticator: FirebaseAuthenticator,
            selectorModel: PoliticianSelectorModel): SinglePoliticianModel = SinglePoliticianModel(context, loaderManager, firebaseRepository, firebaseAuthenticator, selectorModel)

    @PoliticianSelectorScope
    @Provides
    fun provideImageFetcherModel(imageFetcherService: ImageFetcherService) = ImageFetcherModel(imageFetcherService)

    @PoliticianSelectorScope
    @Provides
    fun provideValueEventListener(user: User) = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val refreshedUser: User = dataSnapshot?.getValue(User::class.java) ?: User()

            user.refreshUser(refreshedUser)
        }

        override fun onCancelled(error: DatabaseError?) {}
    }

}
