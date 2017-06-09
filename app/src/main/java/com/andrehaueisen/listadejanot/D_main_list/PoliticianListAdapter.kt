package com.andrehaueisen.listadejanot.D_main_list

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
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
import com.andrehaueisen.listadejanot.A_application.BaseApplication
import com.andrehaueisen.listadejanot.B_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.G_login.LoginActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.encodeEmail
import com.andrehaueisen.listadejanot.utilities.minusOneAbsolveAnimation
import com.andrehaueisen.listadejanot.utilities.plusOneCondemnAnimation
import com.andrehaueisen.listadejanot.utilities.startNewActivity
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

    private val mCardObjectAnimatorAbsolve: ObjectAnimator
    private val mMoldViewObjectAnimatorAbsolve: ObjectAnimator

    private val mCardObjectAnimatorCondemn: ObjectAnimator
    private val mMoldViewObjectAnimatorCondemn: ObjectAnimator

    init {
        mCardObjectAnimatorAbsolve = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(activity, R.color.colorAccentDark),
                ContextCompat.getColor(activity, R.color.colorPrimaryDark))
        mCardObjectAnimatorAbsolve.setEvaluator(ArgbEvaluator())
        mCardObjectAnimatorAbsolve.duration = 1000

        mMoldViewObjectAnimatorAbsolve = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(activity, R.color.colorAccent),
                ContextCompat.getColor(activity, R.color.colorPrimary))
        mMoldViewObjectAnimatorAbsolve.setEvaluator(ArgbEvaluator())
        mMoldViewObjectAnimatorAbsolve.duration = 1000

        mCardObjectAnimatorCondemn = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(activity, R.color.colorPrimaryDark),
                ContextCompat.getColor(activity, R.color.colorAccentDark))
        mCardObjectAnimatorCondemn.setEvaluator(ArgbEvaluator())
        mCardObjectAnimatorCondemn.duration = 1000

        mMoldViewObjectAnimatorCondemn = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(activity, R.color.colorPrimary),
                ContextCompat.getColor(activity, R.color.colorAccent))
        mMoldViewObjectAnimatorCondemn.setEvaluator(ArgbEvaluator())
        mMoldViewObjectAnimatorCondemn.duration = 1000

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

        private var mIsShowingPoliticianDrawable = true

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

            if (politician.condemnedBy.contains(mFirebaseAuthenticator.getUserEmail()?.encodeEmail())) {
                mCardView.background = ContextCompat.getDrawable(activity, R.color.colorAccentDark)
                mMoldView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent))
                mVoteButton.isChecked = true
                mAnimatedBadgeImageView.setImageDrawable(mThiefPoliticianAnimation)
                mIsShowingPoliticianDrawable = true
                ExpectAnim()
                        .expect(mPlusOneAnimationTextView)
                        .toBe(sameCenterAs(mVotesNumberTextView, true, true))
                        .toAnimation().setNow()
                changeButtonAnimation()

            } else {
                mCardView.background = ContextCompat.getDrawable(activity, R.color.colorPrimaryDark)
                mMoldView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                mVoteButton.isChecked = false
                mAnimatedBadgeImageView.setImageDrawable(mPoliticianThiefAnimation)
                mIsShowingPoliticianDrawable = false
                changeButtonAnimation()

            }
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

            ExpectAnim()
                    .expect(mPlusOneAnimationTextView)
                    .toBe(Expectations.alpha(0.0f))
                    .toAnimation()
                    .setNow()
        }

        fun initiateAbsolveAnimations(politician: Politician) {

            //TODO do minus one mVotesNumberTextView
            mVotesNumberTextView.text = politician.votesNumber.toString()

            mCardObjectAnimatorAbsolve.target = mCardView
            mCardObjectAnimatorAbsolve.setEvaluator(ArgbEvaluator())
            mCardObjectAnimatorAbsolve.duration = 1000

            mMoldViewObjectAnimatorAbsolve.target = mMoldView
            mMoldViewObjectAnimatorAbsolve.setEvaluator(ArgbEvaluator())
            mMoldViewObjectAnimatorAbsolve.duration = 1000

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.playTogether(mCardObjectAnimatorAbsolve, mMoldViewObjectAnimatorAbsolve)
            animatorSet.start()

            mPlusOneAnimationTextView.text = activity.getString(R.string.minus_one)
            ExpectAnim().minusOneAbsolveAnimation(itemView, politician)
            changeButtonAnimation()
        }

        fun initiateCondemnAnimations(politician: Politician) {

            //TODO do plus one mVotesNumberTextView animation
            mVotesNumberTextView.text = politician.votesNumber.toString()

            mCardObjectAnimatorCondemn.target = mCardView
            mCardObjectAnimatorCondemn.setEvaluator(ArgbEvaluator())
            mCardObjectAnimatorCondemn.duration = 1000

            mMoldViewObjectAnimatorCondemn.target = mMoldView
            mMoldViewObjectAnimatorCondemn.setEvaluator(ArgbEvaluator())
            mMoldViewObjectAnimatorCondemn.duration = 1000

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.playTogether(mCardObjectAnimatorCondemn, mMoldViewObjectAnimatorCondemn)
            animatorSet.start()

            mPlusOneAnimationTextView.text = activity.getString(R.string.plus_one)
            ExpectAnim().plusOneCondemnAnimation(itemView, politician)
            changeButtonAnimation()
        }

        private fun changeButtonAnimation(){

            if(mAnimatedBadgeImageView.drawable == mPoliticianThiefAnimation && mIsShowingPoliticianDrawable){
                (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
                mIsShowingPoliticianDrawable = false

            }else if(mAnimatedBadgeImageView.drawable == mPoliticianThiefAnimation && !mIsShowingPoliticianDrawable){
                mAnimatedBadgeImageView.setImageDrawable(mThiefPoliticianAnimation)
                (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
                mIsShowingPoliticianDrawable = true

            }else if(mAnimatedBadgeImageView.drawable == mThiefPoliticianAnimation && mIsShowingPoliticianDrawable){
                mAnimatedBadgeImageView.setImageDrawable(mPoliticianThiefAnimation)
                (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
                mIsShowingPoliticianDrawable = false

            }else if(mAnimatedBadgeImageView.drawable == mThiefPoliticianAnimation && !mIsShowingPoliticianDrawable){
                (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
                mIsShowingPoliticianDrawable = true
            }

        }
    }
}