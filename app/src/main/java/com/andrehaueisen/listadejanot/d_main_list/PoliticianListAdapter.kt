package com.andrehaueisen.listadejanot.d_main_list

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import co.ceryle.radiorealbutton.RadioRealButtonGroup
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.g_login.LoginActivity
import com.andrehaueisen.listadejanot.i_opinions.OpinionsActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.github.florent37.expectanim.core.Expectations.sameCenterAs
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


/**
 * Created by andre on 4/24/2017.
 */
class PoliticianListAdapter(val activity: FragmentActivity, val politicianList: ArrayList<Politician>) : RecyclerView.Adapter<PoliticianListAdapter.PoliticianHolder>() {

    private val mFirebaseRepository: FirebaseRepository
    private val mFirebaseAuthenticator: FirebaseAuthenticator

    private val mGlide = Glide.with(activity)

    private val mCardObjectAnimatorAbsolve = ObjectAnimator().animatePropertyToColor(activity, R.color.colorSemiTransparentCondemn, R.color.colorSemiTransparentAbsolve, "cardBackgroundColor")
    private val mCardObjectAnimatorCondemn = ObjectAnimator().animatePropertyToColor(activity, R.color.colorSemiTransparentAbsolve, R.color.colorSemiTransparentCondemn, "cardBackgroundColor")

    init {
        val appComponent = BaseApplication.get(activity).getAppComponent()
        mFirebaseRepository = appComponent.loadFirebaseRepository()
        mFirebaseAuthenticator = appComponent.loadFirebaseAuthenticator()
    }

    override fun getItemCount(): Int = politicianList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliticianHolder {

        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.item_politician, parent, false)

        return PoliticianHolder(view)
    }

    override fun onBindViewHolder(holder: PoliticianHolder, position: Int) = holder.bindDataToView(politicianList[position])


    inner class PoliticianHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mCardView: CardView = itemView.findViewById<CardView>(R.id.card_view)
        private val mPoliticianImageView: ImageView = itemView.findViewById<ImageView>(R.id.politician_image_view)
        private val mPlusOneAnimationTextView: TextView = itemView.findViewById<TextView>(R.id.plus_one_text_view)
        private val mNameTextView: TextView = itemView.findViewById<TextView>(R.id.name_text_view)
        private val mSearchButton = itemView.findViewById<ImageButton>(R.id.search_on_web_button) as ImageButton
        private val mEmailButton = itemView.findViewById<ImageButton>(R.id.email_button)
        private val mVotesNumberTextView: TextView = itemView.findViewById<TextView>(R.id.votes_number_text_view)
        private val mAnimatedBadgeImageView: ImageView = itemView.findViewById<ImageView>(R.id.badge_image_view)
        private val mOpinionsButton: ImageButton = itemView.findViewById<ImageButton>(R.id.opinions_button)
        private val mVoteButton: RadioRealButtonGroup = itemView.findViewById<RadioRealButtonGroup>(R.id.vote_radio_group)
        private val mPoliticianThiefAnimation = activity.getDrawable(R.drawable.anim_politician_thief) as AnimatedVectorDrawable
        private val mThiefPoliticianAnimation = activity.getDrawable(R.drawable.anim_thief_politician) as AnimatedVectorDrawable
        private var mLastButtonPosition = 0

        internal fun bindDataToView(politician: Politician) {

            fun setVoteButtonClickListener() = mVoteButton.setOnClickedButtonListener { _, position ->

                fun initiateVoteProcess() {
                    val userEmail = mFirebaseAuthenticator.getUserEmail()!!
                    ExpectAnim().startRefreshingTitleAnimation(itemView)
                    when(politician.post){
                        Politician.Post.DEPUTADO, Politician.Post.DEPUTADA ->
                            mFirebaseRepository.handleDeputadoVoteOnDatabase(politician, userEmail, this@PoliticianHolder, null)

                        Politician.Post.SENADOR, Politician.Post.SENADORA ->
                            mFirebaseRepository.handleSenadorVoteOnDatabase(politician, userEmail, this@PoliticianHolder, null)

                        Politician.Post.GOVERNADOR, Politician.Post.GOVERNADORA ->
                            mFirebaseRepository.handleGovernadorVoteOnDatabase(politician, userEmail, this@PoliticianHolder, null)
                    }
                }

                if(position != mLastButtonPosition) {
                    if (activity.isConnectedToInternet()) {

                        if (mFirebaseAuthenticator.isUserLoggedIn()) {
                            if(position != mLastButtonPosition) {
                                initiateVoteProcess()
                                mLastButtonPosition = position
                            }
                        } else {
                            activity.startNewActivity(LoginActivity::class.java)
                            activity.finish()
                        }

                    } else {
                        activity.showToast(activity.getString(R.string.no_network))
                        mVoteButton.position = mLastButtonPosition
                    }


                }
            }

            fun setOpinionsButtonClickListener() = mOpinionsButton.setOnClickListener {

                val extras = Bundle()
                extras.putString(BUNDLE_POLITICIAN_EMAIL, politician.email)
                extras.putString(BUNDLE_POLITICIAN_NAME, politician.name)
                extras.putByteArray(BUNDLE_POLITICIAN_IMAGE, politician.image)
                if(mFirebaseAuthenticator.getUserEmail() != null){
                    extras.putString(BUNDLE_USER_EMAIL, mFirebaseAuthenticator.getUserEmail())
                }

                activity.startNewActivity(OpinionsActivity::class.java, null, extras)
            }

            fun setSearchButtonClickListener() = mSearchButton.setOnClickListener{
                with(activity) {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY, "${politician.name} corrupção")

                    if (intent.resolveActivity(packageManager) != null){
                        startActivity(intent)
                    }else{
                        val alertDialog = AlertDialog.Builder(this)
                                .createNeutralDialog(getString(R.string.dialog_title_no_app_detected), getString(R.string.dialog_message_no_browser_app))
                        alertDialog.show()
                    }

                }
            }

            fun setEmailButtonClickListener() = mEmailButton.setOnClickListener {

                with(activity) {
                    val intent = Intent(Intent.ACTION_SENDTO)

                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(politician.email))

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(Intent.createChooser(intent, getString(R.string.intent_email_chooser)))

                    } else {
                        val alertDialog = AlertDialog.Builder(this)
                                .createNeutralDialog(getString(R.string.dialog_title_no_app_detected), getString(R.string.dialog_message_no_email_app))
                        alertDialog.show()
                    }
                }
            }


            setInitialVisualStatus(politician)
            setInitialDataStatus(politician)

            setVoteButtonClickListener()
            setOpinionsButtonClickListener()
            setSearchButtonClickListener()
            setEmailButtonClickListener()
        }

        private fun setInitialVisualStatus(politician: Politician) {

            fun hasUserVotedOnThisPolitician() = politician.condemnedBy.contains(mFirebaseAuthenticator.getUserEmail()?.encodeEmail())

            if (hasUserVotedOnThisPolitician()) {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorSemiTransparentCondemn))

                val badgeTransition = mAnimatedBadgeImageView.background as TransitionDrawable
                badgeTransition.startTransition(DEFAULT_ANIMATIONS_DURATION.toInt())
                mLastButtonPosition = 0
                mVoteButton.position = mLastButtonPosition

                mAnimatedBadgeImageView.setImageDrawable(mThiefPoliticianAnimation)
                mAnimatedBadgeImageView.contentDescription = activity.getString(R.string.description_badge_thief_politician)
                ExpectAnim()
                        .expect(mPlusOneAnimationTextView)
                        .toBe(sameCenterAs(mVotesNumberTextView, true, true))
                        .toAnimation().setNow()

            } else {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorSemiTransparentAbsolve))
                mLastButtonPosition = 1
                mVoteButton.position = mLastButtonPosition

                mAnimatedBadgeImageView.background = ContextCompat.getDrawable(activity, R.drawable.transition_badge_background)
                mAnimatedBadgeImageView.setImageDrawable(mPoliticianThiefAnimation)
                mAnimatedBadgeImageView.contentDescription = activity.getString(R.string.description_badge_honest_politician)
            }

            ExpectAnim()
                    .expect(mPlusOneAnimationTextView)
                    .toBe(Expectations.alpha(0.0f))
                    .toAnimation()
                    .setNow()
        }

        private fun setInitialDataStatus(politician: Politician) {

            val RADIUS: Int
            val MARGIN: Int

            if(politician.post == Politician.Post.DEPUTADO || politician.post == Politician.Post.DEPUTADA){
                RADIUS = 5
                MARGIN = 2

            }else{
                RADIUS = 10
                MARGIN = 5
            }

            mGlide.load(politician.image)
                    .crossFade()
                    .bitmapTransform(RoundedCornersTransformation(activity, RADIUS, MARGIN))
                    .placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mPoliticianImageView)

            mPoliticianImageView.contentDescription = activity.getString(R.string.description_politician_image, politician.name)
            mNameTextView.text = politician.name
            mVotesNumberTextView.text = politician.votesNumber.toString()

        }

        fun initiateAbsolveAnimations(politician: Politician) {
            mCardObjectAnimatorAbsolve.target = mCardView

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.play(mCardObjectAnimatorAbsolve)
            animatorSet.start()

            mPlusOneAnimationTextView.text = activity.getString(R.string.minus_one)
            startCountAnimation(politician, isUpVote = false)

            val badgeTransition = mAnimatedBadgeImageView.background as TransitionDrawable
            badgeTransition.reverseTransition(DEFAULT_ANIMATIONS_DURATION.toInt())

            mAnimatedBadgeImageView.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = false)
            mAnimatedBadgeImageView.contentDescription = activity.getString(R.string.description_badge_honest_politician)
        }

        fun initiateCondemnAnimations(politician: Politician) {
            mCardObjectAnimatorCondemn.target = mCardView

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.play(mCardObjectAnimatorCondemn)
            animatorSet.start()

            mPlusOneAnimationTextView.text = activity.getString(R.string.plus_one)
            startCountAnimation(politician, isUpVote = true)

            val badgeTransition = mAnimatedBadgeImageView.background as TransitionDrawable
            badgeTransition.startTransition(DEFAULT_ANIMATIONS_DURATION.toInt())

            mAnimatedBadgeImageView.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = true)
            mAnimatedBadgeImageView.contentDescription = activity.getString(R.string.description_badge_thief_politician)
        }

        private fun startCountAnimation(politician: Politician, isUpVote: Boolean) {
            val updatedVoteCount = politician.votesNumber
            val currentVoteCount = mVotesNumberTextView.text.toString().toInt()

            fun initiateVoteExpectAnim() = if(isUpVote) {
                ExpectAnim().plusOneCondemnAnimation(itemView, politician)
            }else{
                ExpectAnim().minusOneAbsolveAnimation(itemView, politician)
            }

            if(updatedVoteCount.toInt() == currentVoteCount + 1 || updatedVoteCount.toInt() == currentVoteCount - 1){
                initiateVoteExpectAnim()

            }else {
                val MAX_VALUE_TO_ANIMATE = if (isUpVote) {
                    updatedVoteCount - 1
                } else {
                    updatedVoteCount + 1
                }

                val animator = ValueAnimator.ofInt(currentVoteCount, MAX_VALUE_TO_ANIMATE.toInt())

                animator.duration = QUICK_ANIMATIONS_DURATION
                animator.addUpdateListener { animation ->

                    val animatedValue = animation.animatedValue.toString()
                    mVotesNumberTextView.text = animatedValue
                    val isLastValue = animatedValue == MAX_VALUE_TO_ANIMATE.toString()
                    if (isLastValue) {
                        initiateVoteExpectAnim()
                    }
                }
                animator.start()
            }
        }

        fun notifyPoliticianRemovedFromMainList(removedPolitician: Politician) {

            politicianList
                    .indexOfFirst { listPolitician -> listPolitician.email == removedPolitician.email }
                    .also { index ->
                        if (index != -1) {
                            val politician = politicianList[index]
                            activity.showToast(activity.getString(R.string.absolved_politician, politician.name))

                            politicianList.removeAt(index)
                            notifyItemRemoved(index)

                        }
                    }

        }
    }
}