package com.andrehaueisen.listadejanot.g_user_vote_list.dagger

import android.content.Context
import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListModel
import com.andrehaueisen.listadejanot.models.User
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 6/20/2017.
 */
@UserVoteListScope
@Module
class UserVoteListModule(private val loaderManager: LoaderManager) {

    @UserVoteListScope
    @Provides
    fun provideLoaderManager(): LoaderManager = loaderManager

    @UserVoteListScope
    @Provides
    fun provideUserVoteListModel(context: Context,
                                 firebaseRepository: FirebaseRepository,
                                 firebaseAuthenticator: FirebaseAuthenticator,
                                 user: User): UserVoteListModel = UserVoteListModel(context, loaderManager, firebaseRepository, firebaseAuthenticator, user)

}