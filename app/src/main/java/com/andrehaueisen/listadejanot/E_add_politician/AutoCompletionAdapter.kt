package com.andrehaueisen.listadejanot.E_add_politician

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import java.util.*


/**
 * Created by andre on 5/13/2017.
 */
class AutoCompletionAdapter(mContext: Context, layoutId: Int, var mPoliticiansList: ArrayList<Politician>) : ArrayAdapter<Politician>(
        mContext,
        layoutId,
        mPoliticiansList) {

    private val mPoliticianListOriginalCopy = ArrayList<Politician>()
    private val mPoliticianFilter = PoliticiansFilter()

    init {
        mPoliticianListOriginalCopy.addAll(mPoliticiansList)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_politician_identifier, parent, false)
        }

        val politician = getItem(position)

        if (politician != null) {
            val nameTextView = view?.findViewById(R.id.name_text_view) as TextView
            val postTextView = view.findViewById(R.id.post_text_view) as TextView

            nameTextView.text = politician.name
            postTextView.text = politician.post.toString()

        }

        return view!!
    }

    override fun getFilter(): Filter {
        return mPoliticianFilter
    }

    inner class PoliticiansFilter : Filter() {
        //TODO solve java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val results = FilterResults()

            if (mPoliticiansList.size != mPoliticianListOriginalCopy.size) {
                mPoliticiansList.clear()
                mPoliticiansList.addAll(mPoliticianListOriginalCopy)
            }

            if (constraint != null) {
                val filteredList = mPoliticiansList.filter { it.name.startsWith(constraint, true) }
                results.values = filteredList
                results.count = filteredList.size
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {

            if (results.count > 0) {
                mPoliticiansList.clear()
                mPoliticiansList.addAll(results.values as ArrayList<Politician>)
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}