package com.andrehaueisen.listadejanot.h_user_vote_list

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.VOTES_TO_MAIN_LIST_THRESHOLD
import com.andrehaueisen.listadejanot.utilities.encodeEmail
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by andre on 6/25/2017.
 */
class UserVotesAdapter(val mActivity: Activity, val mUserVotes: List<Politician>, val mUser: User): RecyclerView.Adapter<UserVotesAdapter.VoteHolder>() {

    private val mGlide = Glide.with(mActivity)

    override fun getItemCount() = mUserVotes.size

    override fun onCreateViewHolder(viewGroup: ViewGroup?, viewType: Int): VoteHolder {
        val voteView = LayoutInflater.from(mActivity).inflate(R.layout.item_vote_resume, viewGroup, false)
        return VoteHolder(voteView)
    }

    override fun onBindViewHolder(voteHolder: VoteHolder?, position: Int) {
        voteHolder?.bindVotesToViews(mUserVotes[position])
    }

    inner class VoteHolder(voteView: View): RecyclerView.ViewHolder(voteView){

        val mThumbnailImage: ImageView = voteView.findViewById(R.id.face_thumbnail_image_view) as ImageView
        val mNameTextView: TextView = voteView.findViewById(R.id.name_text_view) as TextView
        val mVoteDateTextView: TextView = voteView.findViewById(R.id.vote_date_text_view) as TextView
        val mTotalVotesTextView: TextView = voteView.findViewById(R.id.total_votes_text_view) as TextView
        val mMissingVotesTextView: TextView = voteView.findViewById(R.id.missing_votes_text_view) as TextView

        internal fun bindVotesToViews(userVote: Politician){

            mGlide.load(userVote.image)
                    .crossFade()
                    .placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mThumbnailImage)

            val missingVotes = VOTES_TO_MAIN_LIST_THRESHOLD - userVote.votesNumber
            if(missingVotes > 0) {
                mMissingVotesTextView.text = missingVotes.toString()
            }else{
                mMissingVotesTextView.text = mActivity.getString(R.string.banned)
            }


            mVoteDateTextView.text = convertTimeStamp(userVote.email)
            mNameTextView.text = userVote.name
            mTotalVotesTextView.text = userVote.votesNumber.toString()
        }

        private fun convertTimeStamp(userVoteEmail: String): String{
            val timestamp = mUser.condemnations[userVoteEmail.encodeEmail()] as Long
            val date = Date(timestamp)
            val simpleDateFormat =  SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

            simpleDateFormat.format(date)
            return date.toString()
        }


    }
}