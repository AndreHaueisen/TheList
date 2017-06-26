package com.andrehaueisen.listadejanot.h_user_vote_list.mvp

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.h_user_vote_list.UserVotesAdapter
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import kotlinx.android.synthetic.main.h_activity_user_vote_list.*


/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListView(val mPresenterActivity: UserVoteListPresenterActivity) : UserVoteListMvpContract.View {

    init {
        mPresenterActivity.setContentView(R.layout.h_activity_user_vote_list)
    }

    fun setViews(savedState: Bundle?) {
        setToolbar()
        setRecyclerView(savedState)

    }

    private fun setRecyclerView(savedState: Bundle?) {

        with(mPresenterActivity) {
            votes_recycler_view.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)

            if (savedState != null) {
                votes_recycler_view.adapter = UserVotesAdapter(this, getUserVotesList(), getUser())
                layoutManager.onRestoreInstanceState(savedState.getParcelable(BUNDLE_MANAGER))
                changeListVisibility()
            }

            votes_recycler_view.layoutManager = layoutManager

            val divider = DividerItemDecoration(this, layoutManager.orientation)
            votes_recycler_view.addItemDecoration(divider)
        }
    }

    private fun setToolbar() {
        with(mPresenterActivity) {
            setSupportActionBar(user_vote_list_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun notifyVotesListReady(user: User) {
        with(mPresenterActivity) {
            votes_recycler_view.adapter = UserVotesAdapter(this, getUserVotesList(), user)
            changeListVisibility()
        }
    }

    private fun changeListVisibility() {
        with(mPresenterActivity) {
            if (getUserVotesList().size == 0) {
                votes_recycler_view.visibility = View.GONE
                empty_vote_list_text_view.visibility = View.VISIBLE
            } else {
                votes_recycler_view.visibility = View.VISIBLE
                empty_vote_list_text_view.visibility = View.GONE
            }
        }
    }
}