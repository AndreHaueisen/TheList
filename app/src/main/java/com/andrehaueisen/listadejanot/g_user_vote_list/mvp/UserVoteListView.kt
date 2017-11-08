package com.andrehaueisen.listadejanot.g_user_vote_list.mvp

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import co.ceryle.radiorealbutton.RadioRealButton
import co.ceryle.radiorealbutton.RadioRealButtonGroup
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.g_user_vote_list.UserVotesAdapter
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import kotlinx.android.synthetic.main.g_activity_user_vote_list.*




/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListView(private val mPresenterActivity: UserVoteListPresenterActivity) {

    private val mSuspectsList = ArrayList<Politician>()
    private val mSuspectsAdapter = UserVotesAdapter(mPresenterActivity, mSuspectsList, mPresenterActivity.getUser(), SUSPECTS_POLITICIANS_ADAPTER_TYPE)
    private val mWillVoteList = ArrayList<Politician>()
    private val mWillVoteAdapter = UserVotesAdapter(mPresenterActivity, mWillVoteList, mPresenterActivity.getUser(), WILL_VOTE_POLITICIANS_ADAPTER_TYPE)
    private var mCurrentShowingList = SUSPECTS_POLITICIANS_ADAPTER_TYPE

    init {
        mPresenterActivity.setContentView(R.layout.g_activity_user_vote_list)
    }

    fun setViews(savedState: Bundle?) {
        setToolbar()
        setRecyclerView(savedState)
        setRadioGroup()
    }

    private fun setToolbar() {
        with(mPresenterActivity) {
            setSupportActionBar(user_vote_list_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setRecyclerView(savedState: Bundle?) = with(mPresenterActivity) {
        votes_recycler_view.setHasFixedSize(true)
        votes_recycler_view.setLayoutAnimation()
        val layoutManager = LinearLayoutManager(this)

        if (savedState != null) {
            mCurrentShowingList = savedState.getInt(BUNDLE_CURRENT_SHOWING_LIST)

            if (mCurrentShowingList == SUSPECTS_POLITICIANS_ADAPTER_TYPE)
                votes_recycler_view.adapter = mSuspectsAdapter
            else
                votes_recycler_view.adapter = mWillVoteAdapter

            layoutManager.onRestoreInstanceState(savedState.getParcelable(BUNDLE_MANAGER))
            votes_recycler_view.layoutManager = layoutManager

            changeListVisibility()
            notifyOnUserListsPoliticiansReady(getUser())

        }else {
            votes_recycler_view.adapter = mSuspectsAdapter
            votes_recycler_view.layoutManager = layoutManager
        }
    }

    private fun setRadioGroup() {
        with(mPresenterActivity) {
            lists_radio_group.position = 0
            lists_radio_group.setOnPositionChangedListener(object : RadioRealButtonGroup.OnPositionChangedListener {
                override fun onPositionChanged(button: RadioRealButton?, currentPosition: Int, lastPosition: Int) {

                    if (currentPosition == 0) {
                        mCurrentShowingList = SUSPECTS_POLITICIANS_ADAPTER_TYPE
                        votes_recycler_view.swapAdapter(mSuspectsAdapter, false)

                    } else {
                        mCurrentShowingList = WILL_VOTE_POLITICIANS_ADAPTER_TYPE
                        votes_recycler_view.swapAdapter(mWillVoteAdapter, false)
                    }
                    votes_recycler_view.startLayoutAnimation()
                    changeListVisibility()

                }
            })
        }
    }

    fun getCurrentShowingList() = mCurrentShowingList

    fun notifyOnUserListsPoliticiansReady(user: User) = with(mPresenterActivity) {
        val suspectList = getOnUserListsPoliticians().filter { politician -> user.condemnations.containsKey(politician.email?.encodeEmail()) }
        val willVoteList = getOnUserListsPoliticians().filter { politician -> user.recommendations.containsKey(politician.email?.encodeEmail()) }

        mSuspectsList.clear()
        mSuspectsList.addAll(suspectList)
        mWillVoteList.clear()
        mWillVoteList.addAll(willVoteList)

        changeListVisibility()

        if(mCurrentShowingList == SUSPECTS_POLITICIANS_ADAPTER_TYPE) {
            mSuspectsList.forEachIndexed { index, _ -> votes_recycler_view.adapter.notifyItemInserted(index) }
        }else{
            mWillVoteList.forEachIndexed { index, _ -> votes_recycler_view.adapter.notifyItemInserted(index) }
        }

    }

    private fun changeListVisibility() = with(mPresenterActivity) {

        if (votes_recycler_view.adapter.itemCount == 0) {
            votes_recycler_view.visibility = View.GONE
            empty_vote_list_text_view.visibility = View.VISIBLE
            empty_vote_list_text_view.startAnimation(AnimationUtils.loadAnimation(empty_vote_list_text_view.context, R.anim.item_animation_grow_from_center))
        } else {
            votes_recycler_view.visibility = View.VISIBLE
            empty_vote_list_text_view.visibility = View.GONE
        }
    }
}