package com.andrehaueisen.listadejanot.g_user_vote_list.dagger

import android.support.v4.app.LoaderManager
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListModel
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Created by andre on 6/20/2017.
 */
@Module
class UserVoteListModule(private val loaderManager: LoaderManager) {

    @UserVoteListScope
    @Provides
    fun provideLoaderManager(): LoaderManager = loaderManager

    @UserVoteListScope
    @Provides
    fun provideUserVoteListModel(@Named("deputados_list") deputados: ArrayList<Politician>,
                                 @Named("senadores_list") senadores: ArrayList<Politician>,
                                 @Named("governadores_list") governadores: ArrayList<Politician>,
                                 @Named("presidentes_list") presidentes: ArrayList<Politician>,
                                 user: User): UserVoteListModel =
            UserVoteListModel(deputados, senadores, governadores, presidentes, user)

}