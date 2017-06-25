package com.andrehaueisen.listadejanot.h_user_vote_list.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.h_user_vote_list.mvp.UserVoteListPresenterActivity
import dagger.Component

/**
 * Created by andre on 6/20/2017.
 */
@UserVoteListScope
@Component(modules = arrayOf(UserVoteListModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface UserVoteListComponent {

    fun inject(userVoteListPresenterActivity: UserVoteListPresenterActivity)
}