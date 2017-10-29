package com.andrehaueisen.listadejanot.i_main_lists

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.SortType
import com.andrehaueisen.listadejanot.utilities.changeTextStyle
import com.andrehaueisen.listadejanot.utilities.setPoliticianGradeText

/**
 * Created by andre on 10/24/2017.
 */
class MainListsAdapter(val context: Context, val politicianList: ArrayList<Politician>) : RecyclerView.Adapter<MainListsAdapter.PoliticianResume>() {

    private var sortType: SortType? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PoliticianResume {

        val view = LayoutInflater.from(context).inflate(R.layout.item_politician_resume, parent, false)

        return PoliticianResume(view)
    }

    override fun getItemCount(): Int = politicianList.size

    override fun onBindViewHolder(holder: PoliticianResume?, position: Int) {
        holder?.onBindData(position)
    }

    fun changeSortType(sortType: SortType) {
        this.sortType = sortType
    }

    inner class PoliticianResume(politicianView: View) : RecyclerView.ViewHolder(politicianView) {

        private val mNameTextView: TextView = politicianView.findViewById(R.id.name_text_view)
        private val mOverallGradeRatingBar: RatingBar = politicianView.findViewById(R.id.overall_grade_rating_bar)
        private val mRecommendationsVotesTextView: TextView = politicianView.findViewById(R.id.total_recommendations_votes_text_view)
        private val mCondemnationsVotesTxtView: TextView = politicianView.findViewById(R.id.total_condemnations_votes_text_view)
        private val mOverallGradeTextView: TextView = politicianView.findViewById(R.id.overall_grade_text_view)

        fun onBindData(position: Int) {

            val politician = politicianList[position]

            mNameTextView.text = politician.name
            mOverallGradeRatingBar.rating = politician.overallGrade
            val stars = mOverallGradeRatingBar.progressDrawable as LayerDrawable
            stars.getDrawable(2).setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryLight), PorterDuff.Mode.SRC_ATOP)

            mRecommendationsVotesTextView.text = context.resources.getQuantityString(
                    R.plurals.recommendation_votes,
                    politician.recommendationsCount,
                    politician.recommendationsCount)
            mCondemnationsVotesTxtView.text = context.resources.getQuantityString(
                    R.plurals.condemnation_votes,
                    politician.condemnationsCount,
                    politician.condemnationsCount)

            mOverallGradeTextView.setPoliticianGradeText(politician.overallGrade, R.string.overall_grade)

            when (sortType) {
                SortType.RECOMMENDATIONS_COUNT -> {
                    mRecommendationsVotesTextView.changeTextStyle(Typeface.BOLD_ITALIC)
                    mCondemnationsVotesTxtView.changeTextStyle(Typeface.NORMAL)
                    mOverallGradeTextView.changeTextStyle(Typeface.NORMAL)
                }
                SortType.CONDEMNATIONS_COUNT -> {
                    mRecommendationsVotesTextView.changeTextStyle(Typeface.NORMAL)
                    mCondemnationsVotesTxtView.changeTextStyle(Typeface.BOLD_ITALIC)
                    mOverallGradeTextView.changeTextStyle(Typeface.NORMAL)
                }
                SortType.OVERALL_GRADE -> {
                    mRecommendationsVotesTextView.changeTextStyle(Typeface.NORMAL)
                    mCondemnationsVotesTxtView.changeTextStyle(Typeface.NORMAL)
                    mOverallGradeTextView.changeTextStyle(Typeface.BOLD_ITALIC)
                }
            }
        }
    }
}