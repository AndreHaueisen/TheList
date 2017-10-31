package com.andrehaueisen.listadejanot.g_user_vote_list

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import com.facebook.FacebookSdk.getApplicationContext
import com.github.florent37.expectanim.ExpectAnim
import com.google.firebase.database.ServerValue
import java.util.*


/**
 * Created by andre on 6/25/2017.
 */
class UserVotesAdapter(val mActivity: Activity, private val mPoliticians: List<Politician>, val mUser: User, val adapterType: Int) : RecyclerView.Adapter<UserVotesAdapter.VoteHolder>() {

    private val mFirebaseRepository: FirebaseRepository
    private val mFirebaseAuthenticator: FirebaseAuthenticator

    override fun getItemCount() = mPoliticians.size

    init {
        val appComponent = BaseApplication.get(mActivity).getAppComponent()
        mFirebaseRepository = appComponent.loadFirebaseRepository()
        mFirebaseAuthenticator = appComponent.loadFirebaseAuthenticator()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup?, viewType: Int): VoteHolder {
        val voteView = LayoutInflater.from(mActivity).inflate(R.layout.item_vote_resume, viewGroup, false)
        return VoteHolder(voteView)
    }

    override fun onBindViewHolder(voteHolder: VoteHolder?, position: Int) {
        voteHolder?.bindVotesToViews(mPoliticians[position])
    }

    override fun getItemViewType(position: Int) = adapterType

    inner class VoteHolder(voteView: View) : RecyclerView.ViewHolder(voteView) {

        private val mNameTextView: TextView = voteView.findViewById(R.id.name_text_view)
        private val mVoteDateTextView: TextView = voteView.findViewById(R.id.vote_date_text_view)
        private val mRecommendationsVotesTextView: TextView = voteView.findViewById(R.id.total_recommendations_votes_text_view)
        private val mCondemnationsVotesTxtView: TextView = voteView.findViewById(R.id.total_condemnations_votes_text_view)
        private val mVoteButton: ToggleButton = itemView.findViewById(R.id.review_vote_toggle_button)
        private val mOverallGradeRatingBar: RatingBar = itemView.findViewById(R.id.overall_grade_rating_bar)

        internal fun bindVotesToViews(politician: Politician) {

            if (adapterType == SUSPECTS_POLITICIANS_ADAPTER_TYPE) {
                mNameTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.colorSecondaryDark))

                val stars = mOverallGradeRatingBar.progressDrawable as LayerDrawable
                stars.getDrawable(2).setColorFilter(ContextCompat.getColor(mActivity, R.color.colorSecondary), PorterDuff.Mode.SRC_ATOP)
            } else {
                val stars = mOverallGradeRatingBar.progressDrawable as LayerDrawable
                stars.getDrawable(2).setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimaryLight), PorterDuff.Mode.SRC_ATOP)
            }

            mVoteDateTextView.text = formatDateText(politician.email)
            mNameTextView.text = politician.name

            mRecommendationsVotesTextView.text = mActivity.resources.getQuantityString(
                    R.plurals.recommendation_votes,
                    politician.recommendationsCount,
                    politician.recommendationsCount)

            mCondemnationsVotesTxtView.text = mActivity.resources.getQuantityString(
                    R.plurals.condemnation_votes,
                    politician.condemnationsCount,
                    politician.condemnationsCount)

            mOverallGradeRatingBar.rating = politician.overallGrade

            configureVoteButton(politician)
        }

        private fun configureVoteButton(politician: Politician) {

            fun updateCount(adapterType: Int, changeAmount: Int) {
                if (adapterType == WILL_VOTE_POLITICIANS_ADAPTER_TYPE) {
                    val newCount = politician.recommendationsCount + changeAmount
                    ExpectAnim().animateVoteTextChange(mRecommendationsVotesTextView, adapterType, newCount)
                } else {
                    val newCount = politician.condemnationsCount + changeAmount
                    ExpectAnim().animateVoteTextChange(mCondemnationsVotesTxtView, adapterType, newCount)
                }
            }

            fun initiateVoteProcess() {
                val ADD_ONE_VOTE = 1
                val REMOVE_ONE_VOTE = -1
                val userEmail = mFirebaseAuthenticator.getUserEmail()!!
                val politicianEncodedEmail = politician.email?.encodeEmail()

                val listAction: ListAction

                if (adapterType == WILL_VOTE_POLITICIANS_ADAPTER_TYPE) {
                    if (mUser.recommendations.containsKey(politicianEncodedEmail)) {
                        //mUser.recommendations.remove(politicianEncodedEmail)
                        listAction = ListAction.REMOVE_FROM_LISTS
                        updateCount(adapterType, REMOVE_ONE_VOTE)
                    } else {
                        mUser.recommendations.put(politicianEncodedEmail!!, ServerValue.TIMESTAMP)
                        listAction = ListAction.ADD_TO_VOTE_LIST
                        updateCount(adapterType, ADD_ONE_VOTE)
                    }
                } else {
                    if (mUser.condemnations.containsKey(politicianEncodedEmail)) {
                        //mUser.condemnations.remove(politicianEncodedEmail)
                        listAction = ListAction.REMOVE_FROM_LISTS
                        updateCount(adapterType, REMOVE_ONE_VOTE)
                    } else {
                        mUser.condemnations.put(politicianEncodedEmail!!, ServerValue.TIMESTAMP)
                        listAction = ListAction.ADD_TO_SUSPECT_LIST
                        updateCount(adapterType, ADD_ONE_VOTE)
                    }
                }
                mFirebaseRepository.handleListChangeOnDatabase(listAction, politician, userEmail)

            }

            mVoteButton.isChecked = true
            mVoteButton.setOnClickListener {
                if (mActivity.isConnectedToInternet()) {

                    mActivity.setResult(Activity.RESULT_OK)

                    if (mFirebaseAuthenticator.isUserLoggedIn()) {
                        initiateVoteProcess()

                    } else {
                        mActivity.startNewActivity(LoginActivity::class.java)
                        mActivity.finish()
                    }

                } else {
                    mActivity.showToast(mActivity.getString(R.string.no_network))
                    mVoteButton.isChecked = !mVoteButton.isChecked
                }
            }
        }

        private fun formatDateText(userVoteEmail: String?): String {

            val timestamp = if (adapterType == SUSPECTS_POLITICIANS_ADAPTER_TYPE)
                mUser.condemnations[userVoteEmail?.encodeEmail()] as Long
            else
                mUser.recommendations[userVoteEmail?.encodeEmail()] as Long

            val date = Date(timestamp)
            val dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext())

            return mActivity.getString(R.string.your_vote_date, dateFormat.format(date))
        }

    }
}