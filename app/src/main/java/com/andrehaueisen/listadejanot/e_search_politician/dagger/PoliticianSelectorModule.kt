package com.andrehaueisen.listadejanot.e_search_politician.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.e_search_politician.mvp.ImageFetcherModel
import com.andrehaueisen.listadejanot.e_search_politician.mvp.ImageFetcherService
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorModel
import com.andrehaueisen.listadejanot.e_search_politician.mvp.SinglePoliticianModel
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
class PoliticianSelectorModule(private val mLoaderManager: LoaderManager, private val mUser: User) {

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
    fun provideImageFetcherModel(imageFetcherService: ImageFetcherService, context: Context) = ImageFetcherModel(imageFetcherService, context)

    @PoliticianSelectorScope
    @Provides
    fun provideValueEventListener() = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val refreshedUser: User = dataSnapshot?.getValue(User::class.java) ?: User()

            mUser.refreshUser(refreshedUser)
        }

        override fun onCancelled(error: DatabaseError?) {}
    }


}
