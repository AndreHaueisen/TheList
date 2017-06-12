package com.andrehaueisen.listadejanot.d_main_list

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.g_login.LoginActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.github.florent37.expectanim.core.Expectations.sameCenterAs


/**
 * Created by andre on 4/24/2017.
 */
class PoliticianListAdapter(val activity: Activity, val politicianList: ArrayList<Politician>) : RecyclerView.Adapter<PoliticianListAdapter.PoliticianHolder>() {

    private val mFirebaseRepository: FirebaseRepository
    private val mFirebaseAuthenticator: FirebaseAuthenticator

    private val VIEW_TYPE_DEPUTADO = 0
    private val VIEW_TYPE_SENADOR = 1
    private val mGlide = Glide.with(activity)

    private val mCardObjectAnimatorAbsolve = ObjectAnimator().animateBackgroundToColor(activity, R.color.colorAccentDark, R.color.colorPrimaryDark, "cardBackgroundColor")
    private val mMoldViewObjectAnimatorAbsolve = ObjectAnimator().animateBackgroundToColor(activity, R.color.colorAccent, R.color.colorPrimary, "backgroundColor")

    private val mCardObjectAnimatorCondemn = ObjectAnimator().animateBackgroundToColor(activity, R.color.colorPrimaryDark, R.color.colorAccentDark, "cardBackgroundColor")
    private val mMoldViewObjectAnimatorCondemn = ObjectAnimator().animateBackgroundToColor(activity, R.color.colorPrimary, R.color.colorAccent, "backgroundColor")

    init {
        val appComponent = BaseApplication.get(activity).getAppComponent()
        mFirebaseRepository = appComponent.loadFirebaseRepository()
        mFirebaseAuthenticator = appComponent.loadFirebaseAuthenticator()
    }

    override fun getItemCount(): Int {
        return politicianList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliticianHolder {

        val inflater = LayoutInflater.from(activity)
        val view: View
        if (viewType == VIEW_TYPE_DEPUTADO) {
            view = inflater.inflate(R.layout.item_deputado, parent, false)
            return PoliticianHolder(view)
        } else {
            view = inflater.inflate(R.layout.item_senador, parent, false)
            return PoliticianHolder(view)
        }
    }

    override fun onBindViewHolder(holder: PoliticianHolder, position: Int) {
        holder.bindDataToView(politicianList[position])

    }

    override fun getItemViewType(position: Int): Int {

        if (politicianList[position].post == Politician.Post.DEPUTADO) {
            return VIEW_TYPE_DEPUTADO
        } else {
            return VIEW_TYPE_SENADOR
        }
    }

    inner class PoliticianHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mCardView: CardView = itemView.findViewById(R.id.card_view) as CardView
        private val mMoldView: View = itemView.findViewById(R.id.mold_view)
        private val mPoliticianImageView: ImageView = itemView.findViewById(R.id.politician_image_view) as ImageView
        private val mPlusOneAnimationTextView: TextView = itemView.findViewById(R.id.plus_one_text_view) as TextView
        private val mNameTextView: TextView = itemView.findViewById(R.id.name_text_view) as TextView
        private val mEmailTextView = itemView.findViewById(R.id.email_text_view) as TextView
        private val mVotesNumberTextView : TextView = itemView.findViewById(R.id.votes_number_text_view) as TextView
        private val mAnimatedBadgeImageView: ImageView = itemView.findViewById(R.id.badge_image_view) as ImageView
        private val mVoteButton: ToggleButton = itemView.findViewById(R.id.add_to_vote_count_toggle_button) as ToggleButton
        private val mPoliticianThiefAnimation = activity.getDrawable(R.drawable.politician_thief_animated_vector) as AnimatedVectorDrawable
        private val mThiefPoliticianAnimation = activity.getDrawable(R.drawable.thief_politician_animated_vector) as AnimatedVectorDrawable

        internal fun bindDataToView(politician: Politician) {

            setInitialVisualStatus(politician)
            setInitialDataStatus(politician)

            mVoteButton.setOnClickListener {
                if(mFirebaseAuthenticator.isUserLoggedIn()) {

                    val userEmail = mFirebaseAuthenticator.getUserEmail()!!
                    if (politician.post == Politician.Post.DEPUTADO) {
                        mFirebaseRepository.updateDeputadoVoteOnBothLists(politician, userEmail, this@PoliticianHolder, null)
                    } else {
                        mFirebaseRepository.updateSenadorVoteOnBothLists(politician, userEmail, this@PoliticianHolder, null)
                    }

                } else {
                    activity.startNewActivity(LoginActivity::class.java)
                    activity.finish()
                }
            }
        }

        private fun setInitialVisualStatus(politician: Politician){

            fun hasUserVotedOnThisPolitician() = politician.condemnedBy.contains(mFirebaseAuthenticator.getUserEmail()?.encodeEmail())

            if (hasUserVotedOnThisPolitician()) {
                mCardView.setCardBackgroundColor( ContextCompat.getColor(activity, R.color.colorAccentDark) )
                mMoldView.setBackgroundColor( ContextCompat.getColor(activity, R.color.colorAccent) )
                mVoteButton.isChecked = true
                mAnimatedBadgeImageView.setImageDrawable(mThiefPoliticianAnimation)
                mAnimatedBadgeImageView.animateVectorDrawable(
                        mPoliticianThiefAnimation,
                        mThiefPoliticianAnimation,
                        useInitialToFinalFlow = true)
                ExpectAnim()
                        .expect(mPlusOneAnimationTextView)
                        .toBe(sameCenterAs(mVotesNumberTextView, true, true))
                        .toAnimation().setNow()

            } else {
                mCardView.setCardBackgroundColor( ContextCompat.getColor(activity, R.color.colorPrimaryDark) )
                mMoldView.setBackgroundColor( ContextCompat.getColor(activity, R.color.colorPrimary) )
                mVoteButton.isChecked = false
                mAnimatedBadgeImageView.setImageDrawable(mPoliticianThiefAnimation)
                mAnimatedBadgeImageView.animateVectorDrawable(
                        mPoliticianThiefAnimation,
                        mThiefPoliticianAnimation,
                        useInitialToFinalFlow = false)
            }

            ExpectAnim()
                    .expect(mPlusOneAnimationTextView)
                    .toBe(Expectations.alpha(0.0f))
                    .toAnimation()
                    .setNow()
        }

        private fun setInitialDataStatus(politician: Politician){
            mGlide.load(politician.image)
                    .crossFade()
                    .placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mPoliticianImageView)
            mNameTextView.text = politician.name
            mVotesNumberTextView.text = politician.votesNumber.toString()
            mEmailTextView.text = politician.email

        }

        fun initiateAbsolveAnimations(politician: Politician) {
            mCardObjectAnimatorAbsolve.target = mCardView
            mMoldViewObjectAnimatorAbsolve.target = mMoldView

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.playTogether(mCardObjectAnimatorAbsolve, mMoldViewObjectAnimatorAbsolve)
            animatorSet.start()

            mVotesNumberTextView.text = politician.votesNumber.toString()
            mPlusOneAnimationTextView.text = activity.getString(R.string.minus_one)
            ExpectAnim().minusOneAbsolveAnimation(itemView, politician)
            mAnimatedBadgeImageView.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = false)
        }

        fun initiateCondemnAnimations(politician: Politician) {
            mCardObjectAnimatorCondemn.target = mCardView
            mMoldViewObjectAnimatorCondemn.target = mMoldView

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.playTogether(mCardObjectAnimatorCondemn, mMoldViewObjectAnimatorCondemn)
            animatorSet.start()

            mVotesNumberTextView.text = politician.votesNumber.toString()
            mPlusOneAnimationTextView.text = activity.getString(R.string.plus_one)
            ExpectAnim().plusOneCondemnAnimation(itemView, politician)
            mAnimatedBadgeImageView.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = true)
        }
    }
}