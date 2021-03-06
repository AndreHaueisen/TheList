package com.andrehaueisen.listadejanot.e_main_lists

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import kotlinx.android.synthetic.main.e_activity_main_lists.*

/**
 * Created by andre on 10/24/2017.
 */
class MainListsAdapter(val activity: AppCompatActivity, val politicianList: ArrayList<Politician>, val sortType: SortType) : RecyclerView.Adapter<MainListsAdapter.PoliticianResume>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliticianResume {

        val view = LayoutInflater.from(activity as Context).inflate(R.layout.item_politician_resume, parent, false)
        return PoliticianResume(view)
    }

    override fun getItemCount(): Int = politicianList.size

    override fun onBindViewHolder(holder: PoliticianResume, position: Int) {
        holder.onBindData(position)
    }

    inner class PoliticianResume(politicianView: View) : RecyclerView.ViewHolder(politicianView) {

        private val mScrollIndicatorView = politicianView.findViewById<View>(R.id.horizontal_scroll_indicator)
        private val mCardView: CardView = politicianView.findViewById(R.id.politician_resume_card_view)
        private val mNameTextView: TextView = politicianView.findViewById(R.id.name_text_view)
        private val mOverallGradeRatingBar: RatingBar = politicianView.findViewById(R.id.overall_grade_rating_bar)
        private val mRecommendationsVotesTextView: TextView = politicianView.findViewById(R.id.total_recommendations_votes_text_view)
        private val mCondemnationsVotesTxtView: TextView = politicianView.findViewById(R.id.total_condemnations_votes_text_view)
        private val mOverallGradeTextView: TextView = politicianView.findViewById(R.id.overall_grade_text_view)

        fun onBindData(position: Int) {

            mCardView.setOnClickListener {
                val extras = Bundle()
                extras.putString(INTENT_POLITICIAN_NAME, mNameTextView.text.toString())

                val toolbarPair = Pair(activity.main_lists_toolbar as View, activity.getString(R.string.transition_toolbar))
                val fabMenuPair = Pair(activity.menu_fab as View, activity.getString(R.string.transition_button))
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, fabMenuPair, toolbarPair)

                activity.startNewActivity(PoliticianSelectorPresenterActivity::class.java, extras = extras, options = options.toBundle())
            }

            val politician = politicianList[position]

            mNameTextView.text = politician.name
            mOverallGradeRatingBar.rating = politician.overallGrade
            val stars = mOverallGradeRatingBar.progressDrawable as LayerDrawable
            stars.getDrawable(2).setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimaryLight), PorterDuff.Mode.SRC_ATOP)

            mRecommendationsVotesTextView.text =
                    if (politician.recommendationsCount == 0)
                        activity.getString(R.string.recommendation_votes_zero)
                    else activity.resources.getQuantityString(
                            R.plurals.recommendation_votes,
                            politician.recommendationsCount,
                            politician.recommendationsCount)

            mCondemnationsVotesTxtView.text =
                    if (politician.condemnationsCount == 0)
                        activity.getString(R.string.condemnation_votes_zero)
                    else
                        activity.resources.getQuantityString(
                                R.plurals.condemnation_votes,
                                politician.condemnationsCount,
                                politician.condemnationsCount)

            mOverallGradeTextView.setPoliticianGradeText(politician.overallGrade, R.string.overall_grade)

            when (sortType) {
                SortType.RECOMMENDATIONS_COUNT -> {
                    mRecommendationsVotesTextView.visibility = View.VISIBLE
                    mCondemnationsVotesTxtView.visibility = View.GONE
                    mOverallGradeTextView.visibility = View.GONE
                }
                SortType.CONDEMNATIONS_COUNT -> {
                    mRecommendationsVotesTextView.visibility = View.GONE
                    mCondemnationsVotesTxtView.visibility = View.VISIBLE
                    mOverallGradeTextView.visibility = View.GONE
                }
                SortType.TOP_OVERALL_GRADE, SortType.WORST_OVERALL_GRADE -> {
                    mRecommendationsVotesTextView.visibility = View.GONE
                    mCondemnationsVotesTxtView.visibility = View.GONE
                    mOverallGradeTextView.visibility = View.VISIBLE
                }
            }

            if(politicianList.count() > 1) {

                when (layoutPosition) {
                    0 -> setViewMargin(mScrollIndicatorView, HORIZONTAL_LIST_INDICATOR_MARGIN, HORIZONTAL_LIST_INDICATOR_MARGIN)

                    politicianList.count() - 1 -> setViewMargin(mScrollIndicatorView, top = HORIZONTAL_LIST_INDICATOR_MARGIN, right = HORIZONTAL_LIST_INDICATOR_MARGIN)

                    else -> setViewMargin(mScrollIndicatorView, top = HORIZONTAL_LIST_INDICATOR_MARGIN)
                }

            } else {
                setViewMargin(mScrollIndicatorView, HORIZONTAL_LIST_INDICATOR_MARGIN, HORIZONTAL_LIST_INDICATOR_MARGIN, HORIZONTAL_LIST_INDICATOR_MARGIN)
            }

        }

        private fun setViewMargin(view: View, left: Float = 0F, top: Float = 0F, right: Float = 0F, bottom: Float = 0F){

            val context = activity
            val marginLeft = context.convertDpToPixel(left).toInt()
            val marginTop = context.convertDpToPixel(top).toInt()
            val marginRight = context.convertDpToPixel(right).toInt()
            val marginBottom = context.convertDpToPixel(bottom).toInt()
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.setMargins(marginLeft, marginTop, marginRight, marginBottom) //substitute parameters for left, top, right, bottom
            view.layoutParams = params
        }
    }
}