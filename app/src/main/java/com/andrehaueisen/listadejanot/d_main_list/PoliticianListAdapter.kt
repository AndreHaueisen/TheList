package com.andrehaueisen.listadejanot.d_main_list

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.FragmentTransaction
import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListPresenterActivity
import com.andrehaueisen.listadejanot.g_login.LoginActivity
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
class PoliticianListAdapter(val activity: Activity, val politicianList: ArrayList<Politician>) : RecyclerView.Adapter<PoliticianListAdapter.PoliticianHolder>() {

    private val mFirebaseRepository: FirebaseRepository
    private val mFirebaseAuthenticator: FirebaseAuthenticator

    private val VIEW_TYPE_DEPUTADO = 0
    private val VIEW_TYPE_SENADOR = 1
    private val VIEW_TYPE_GOVERNADOR = 2
    private val mGlide = Glide.with(activity)

    private val mCardObjectAnimatorAbsolve = ObjectAnimator().animatePropertyToColor(activity, R.color.colorSemiTransparentCondemn, R.color.colorSemiTransparentAbsolve, "cardBackgroundColor")
    private val mCardObjectAnimatorCondemn = ObjectAnimator().animatePropertyToColor(activity, R.color.colorSemiTransparentAbsolve, R.color.colorSemiTransparentCondemn, "cardBackgroundColor")

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

        } else if(viewType == VIEW_TYPE_SENADOR) {
            view = inflater.inflate(R.layout.item_senador, parent, false)

        }else{
            view = inflater.inflate(R.layout.item_governador, parent, false)
        }

        return PoliticianHolder(view)
    }

    override fun onBindViewHolder(holder: PoliticianHolder, position: Int) {
        holder.bindDataToView(politicianList[position])

    }

    override fun getItemViewType(position: Int): Int {

        when (politicianList[position].post!!) {
            Politician.Post.DEPUTADO, Politician.Post.DEPUTADA -> return VIEW_TYPE_DEPUTADO

            Politician.Post.SENADOR, Politician.Post.SENADORA -> return VIEW_TYPE_SENADOR

            Politician.Post.GOVERNADOR,Politician.Post.GOVERNADORA -> return VIEW_TYPE_GOVERNADOR
        }

    }

    inner class PoliticianHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mCardView: CardView = itemView.findViewById<CardView>(R.id.card_view)
        private val mPoliticianImageView: ImageView = itemView.findViewById<ImageView>(R.id.politician_image_view)
        private val mPlusOneAnimationTextView: TextView = itemView.findViewById<TextView>(R.id.plus_one_text_view)
        private val mNameTextView: TextView = itemView.findViewById<TextView>(R.id.name_text_view)
        private val mEmailTextView = itemView.findViewById<TextView>(R.id.email_text_view) as TextView
        private val mVotesNumberTextView: TextView = itemView.findViewById<TextView>(R.id.votes_number_text_view)
        private val mAnimatedBadgeImageView: ImageView = itemView.findViewById<ImageView>(R.id.badge_image_view)
        private val mOpinionsButton: Button = itemView.findViewById<Button>(R.id.opinion_button)
        private val mVoteButton: ToggleButton = itemView.findViewById<ToggleButton>(R.id.add_to_vote_count_toggle_button)
        private val mPoliticianThiefAnimation = activity.getDrawable(R.drawable.politician_thief_animated_vector) as AnimatedVectorDrawable
        private val mThiefPoliticianAnimation = activity.getDrawable(R.drawable.thief_politician_animated_vector) as AnimatedVectorDrawable

        internal fun bindDataToView(politician: Politician) {

            fun setVoteButtonClickListener() {
                mVoteButton.setOnClickListener {

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

                    if (activity.isConnectedToInternet()) {

                        if (mFirebaseAuthenticator.isUserLoggedIn()) {
                            initiateVoteProcess()

                        } else {
                            activity.startNewActivity(LoginActivity::class.java)
                            activity.finish()
                        }

                    } else {
                        activity.showToast(activity.getString(R.string.no_network))
                        mVoteButton.isChecked = !mVoteButton.isChecked
                    }
                }
            }

            fun setOpinionsButtonClickListener(){
                mOpinionsButton.setOnClickListener {

                    val fragmentTransaction: FragmentTransaction = activity.fragmentManager.beginTransaction()
                    val prev = activity.fragmentManager.findFragmentByTag("dialog")
                    if (prev != null) {
                        fragmentTransaction.remove(prev)
                    }
                    fragmentTransaction.addToBackStack(null)

                    val bundle = Bundle()
                    bundle.putString(BUNDLE_POLITICIAN_EMAIL, politician.email)
                    bundle.putString(BUNDLE_POLITICIAN_NAME, politician.name)
                    bundle.putByteArray(BUNDLE_POLITICIAN_IMAGE, politician.image)
                    if(mFirebaseAuthenticator.getUserEmail() != null){
                        bundle.putString(BUNDLE_USER_EMAIL, mFirebaseAuthenticator.getUserEmail())
                    }

                    val dialogFragment = OpinionsDialog.newInstance(bundle, mFirebaseRepository)
                    dialogFragment.show(fragmentTransaction, "dialog")
                }
            }

            fun setPoliticianImageViewClickListener() {
                mPoliticianImageView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY, "${politician.name} corrupção")
                    activity.startActivity(intent)

                }
            }

            setInitialVisualStatus(politician)
            setInitialDataStatus(politician)

            setVoteButtonClickListener()
            setOpinionsButtonClickListener()
            setPoliticianImageViewClickListener()

        }

        private fun setInitialVisualStatus(politician: Politician) {

            fun hasUserVotedOnThisPolitician() = politician.condemnedBy.contains(mFirebaseAuthenticator.getUserEmail()?.encodeEmail())

            if (hasUserVotedOnThisPolitician()) {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorSemiTransparentCondemn))
                mVoteButton.isChecked = true
                mAnimatedBadgeImageView.setImageDrawable(mThiefPoliticianAnimation)
                mAnimatedBadgeImageView.animateVectorDrawable(
                        mPoliticianThiefAnimation,
                        mThiefPoliticianAnimation,
                        useInitialToFinalFlow = true)
                mAnimatedBadgeImageView.contentDescription = activity.getString(R.string.description_badge_thief_politician)
                ExpectAnim()
                        .expect(mPlusOneAnimationTextView)
                        .toBe(sameCenterAs(mVotesNumberTextView, true, true))
                        .toAnimation().setNow()

            } else {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorSemiTransparentAbsolve))
                mVoteButton.isChecked = false
                mAnimatedBadgeImageView.setImageDrawable(mPoliticianThiefAnimation)
                mAnimatedBadgeImageView.animateVectorDrawable(
                        mPoliticianThiefAnimation,
                        mThiefPoliticianAnimation,
                        useInitialToFinalFlow = false)
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
            mEmailTextView.text = politician.email

        }

        fun initiateAbsolveAnimations(politician: Politician) {
            mCardObjectAnimatorAbsolve.target = mCardView

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.play(mCardObjectAnimatorAbsolve)
            animatorSet.start()

            mPlusOneAnimationTextView.text = activity.getString(R.string.minus_one)
            startCountAnimation(politician, isUpVote = false)
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

            mAnimatedBadgeImageView.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = true)
            mAnimatedBadgeImageView.contentDescription = activity.getString(R.string.description_badge_thief_politician)
        }

        private fun startCountAnimation(politician: Politician, isUpVote: Boolean) {
            val updatedVoteCount = politician.votesNumber
            val currentVoteCount = mVotesNumberTextView.text.toString().toInt()

            fun initiateVoteExpectAnim(){
                if(isUpVote) {
                    ExpectAnim().plusOneCondemnAnimation(itemView, politician)
                }else{
                    ExpectAnim().minusOneAbsolveAnimation(itemView, politician)
                }
            }

            if(updatedVoteCount.toInt() == currentVoteCount + 1 || updatedVoteCount.toInt() == currentVoteCount - 1){
                initiateVoteExpectAnim()

            }else {
                val MAX_VALUE_TO_ANIMATE: Long
                if (isUpVote) {
                    MAX_VALUE_TO_ANIMATE = updatedVoteCount - 1
                } else {
                    MAX_VALUE_TO_ANIMATE = updatedVoteCount + 1
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

        fun notifyPoliticianAddedToMainList(email: String) {
            if (politicianList.indexOfFirst { listPolitician -> listPolitician.email == email } == -1) {
                (activity as MainListPresenterActivity).subscribeToModel()
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