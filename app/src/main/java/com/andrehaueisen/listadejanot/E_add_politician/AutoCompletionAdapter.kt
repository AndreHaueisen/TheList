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


/**
 * Created by andre on 5/13/2017.
 */
class AutoCompletionAdapter(mContext: Context,
                            layoutId: Int,
                            val mNonReliablePoliticiansList: ArrayList<Politician>,
                            val mOriginalPoliticianList: ArrayList<Politician>) : ArrayAdapter<Politician>(
        mContext,
        layoutId,
        mNonReliablePoliticiansList) {


    private val mPoliticianFilter = PoliticiansFilter()

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

            if (mNonReliablePoliticiansList.size != mOriginalPoliticianList.size) {
                mNonReliablePoliticiansList.clear()
                mNonReliablePoliticiansList.addAll(mOriginalPoliticianList)
            }

            if (constraint != null) {
                val filteredList = mNonReliablePoliticiansList.filter { it.name.startsWith(constraint, true) }
                results.values = filteredList
                results.count = filteredList.size
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {

            if (results.count > 0) {
                mNonReliablePoliticiansList.clear()
                mNonReliablePoliticiansList.addAll(results.values as ArrayList<Politician>)
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}