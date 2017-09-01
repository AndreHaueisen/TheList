package com.andrehaueisen.listadejanot.h_user_vote_list

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.g_login.LoginActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.FacebookSdk.getApplicationContext
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.util.*



/**
 * Created by andre on 6/25/2017.
 */
class UserVotesAdapter(val mActivity: Activity, private val mUserVotes: List<Politician>, val mUser: User): RecyclerView.Adapter<UserVotesAdapter.VoteHolder>() {

    private val mFirebaseRepository: FirebaseRepository
    private val mFirebaseAuthenticator: FirebaseAuthenticator
    private val mGlide = Glide.with(mActivity)

    override fun getItemCount() = mUserVotes.size

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
        voteHolder?.bindVotesToViews(mUserVotes[position])
    }

    inner class VoteHolder(voteView: View): RecyclerView.ViewHolder(voteView){

        private val mThumbnailImage: ImageView = voteView.findViewById<ImageView>(R.id.face_thumbnail_image_view)
        private val mNameTextView: TextView = voteView.findViewById<TextView>(R.id.name_text_view)
        private val mVoteDateTextView: TextView = voteView.findViewById<TextView>(R.id.vote_date_text_view)
        private val mTotalVotesTextView: TextView = voteView.findViewById<TextView>(R.id.total_votes_text_view)
        private val mMissingVotesTextView: TextView = voteView.findViewById<TextView>(R.id.missing_votes_text_view)
        private val mVoteButton: ToggleButton = itemView.findViewById<ToggleButton>(R.id.review_vote_toggle_button)

        internal fun bindVotesToViews(userVote: Politician){

            mGlide.load(userVote.image)
                    .bitmapTransform(CropCircleTransformation(mActivity))
                    .crossFade()
                    .placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mThumbnailImage)
            val minimumVotesToMainList = mActivity.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
            val missingVotes = minimumVotesToMainList - userVote.votesNumber
            if(missingVotes > 0) {
                mMissingVotesTextView.text = mActivity.resources.getQuantityString(R.plurals.missing_to_banned, missingVotes.toInt(), missingVotes)
            }else{
                mMissingVotesTextView.text = mActivity.getString(R.string.banned)
            }

            mVoteDateTextView.text = formatDateText(userVote.email)
            mNameTextView.text = userVote.name
            mTotalVotesTextView.text = mActivity
                    .resources
                    .getQuantityString(R.plurals.votes, userVote.votesNumber.toInt(), userVote.votesNumber)

            configureVoteButton(userVote)
        }

        private fun configureVoteButton(userVote: Politician){

            fun initiateVoteProcess(){
                val userEmail = mFirebaseAuthenticator.getUserEmail()!!
                if (userVote.post == Politician.Post.DEPUTADO || userVote.post == Politician.Post.DEPUTADA) {
                    mFirebaseRepository.handleDeputadoVoteOnDatabase(userVote, userEmail, null, null)
                } else {
                    mFirebaseRepository.handleSenadorVoteOnDatabase(userVote, userEmail, null, null)
                }
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

                }else{
                    mActivity.showToast(mActivity.getString(R.string.no_network))
                    mVoteButton.isChecked = !mVoteButton.isChecked
                }
            }
        }

        private fun formatDateText(userVoteEmail: String): String{

            val timestamp = mUser.condemnations[userVoteEmail.encodeEmail()] as Long
            val date = Date(timestamp)
            val dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext())

            return mActivity.getString(R.string.your_vote_date, dateFormat.format(date))
        }


    }
}