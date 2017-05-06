package com.andrehaueisen.listadejanot.D_main_list

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
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
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy



/**
 * Created by andre on 4/24/2017.
 */
class PoliticianListAdapter(val context: Context, val politicianList: ArrayList<Politician>) : RecyclerView.Adapter<PoliticianListAdapter.PoliticianHolder>() {

    private val VIEW_TYPE_DEPUTADO = 0
    private val VIEW_TYPE_SENADOR = 1
    private val mGlide = Glide.with(context)

    val mCardObjectAnimatorAbsolve: ObjectAnimator
    val mMoldViewObjectAnimatorAbsolve: ObjectAnimator

    val mCardObjectAnimatorCondemn: ObjectAnimator
    val mMoldViewObjectAnimatorCondemn: ObjectAnimator

    init {
        mCardObjectAnimatorAbsolve = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(context, R.color.colorAccentDark),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))
        mCardObjectAnimatorAbsolve.setEvaluator(ArgbEvaluator())
        mCardObjectAnimatorAbsolve.duration = 1000

        mMoldViewObjectAnimatorAbsolve = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimary))
        mMoldViewObjectAnimatorAbsolve.setEvaluator(ArgbEvaluator())
        mMoldViewObjectAnimatorAbsolve.duration = 1000

        mCardObjectAnimatorCondemn = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(context, R.color.colorPrimaryDark),
                ContextCompat.getColor(context, R.color.colorAccentDark))
        mCardObjectAnimatorCondemn.setEvaluator(ArgbEvaluator())
        mCardObjectAnimatorCondemn.duration = 1000

        mMoldViewObjectAnimatorCondemn = ObjectAnimator.ofInt(null, "backgroundColor",
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent))
        mMoldViewObjectAnimatorCondemn.setEvaluator(ArgbEvaluator())
        mMoldViewObjectAnimatorCondemn.duration = 1000

    }

    override fun getItemCount(): Int {
        return politicianList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliticianHolder {
        val inflater = LayoutInflater.from(context)
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

        val mCardView: CardView = itemView.findViewById(R.id.card_view) as CardView
        val mMoldView: View = itemView.findViewById(R.id.mold_image_view)
        val mPoliticianImageView: ImageView = itemView.findViewById(R.id.politician_image_view) as ImageView
        val mNameTextView: TextView = itemView.findViewById(R.id.name_text_view) as TextView
        val mEmailTextView = itemView.findViewById(R.id.email_text_view) as TextView

        //val mVotesNumberTextView : TextView
        val mVoteButton: ToggleButton = itemView.findViewById(R.id.add_to_vote_count_image_view) as ToggleButton

        internal fun bindDataToView(politician: Politician) {

            if (politician.hasPersonVote){
                mCardView.background = ContextCompat.getDrawable(context, R.color.colorAccentDark)
                mMoldView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                mVoteButton.isChecked = true
            }else{
                mCardView.background = ContextCompat.getDrawable(context, R.color.colorPrimaryDark)
                mMoldView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                mVoteButton.isChecked = false
            }

            mGlide.load(politician.image)
                    .crossFade()
                    .placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mPoliticianImageView)
            mNameTextView.text = politician.name
            // mVotesNumberTextView.text = //voteNumber
            mEmailTextView.text = politician.email


            mVoteButton.backgroundTintList = ContextCompat.getColorStateList(context, R.drawable.selector_vote_button)
            mVoteButton.setOnClickListener {

                if (politician.hasPersonVote){
                    setAbsolveAnimations(politician)

                }else {
                    setCondemnAnimations(politician)
                }
            }
        }

        private fun setAbsolveAnimations(politician: Politician){
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

            politician.hasPersonVote = false
        }

        private fun setCondemnAnimations(politician: Politician){
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

            politician.hasPersonVote = true

        }



    }
}