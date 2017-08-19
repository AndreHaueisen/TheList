package com.andrehaueisen.listadejanot.e_search_politician

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import java.text.Normalizer
import java.util.regex.Pattern


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
            val nameTextView = view?.findViewById<TextView>(R.id.name_text_view)
            val postTextView = view?.findViewById<TextView>(R.id.post_text_view)

            nameTextView?.text = politician.name
            postTextView?.text = politician.post.name

        }

        return view!!
    }

    override fun getFilter(): Filter = mPoliticianFilter

    inner class PoliticiansFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val results = FilterResults()

            val filteredConstraint = constraint?.toString()?.stripAccents()
            if (filteredConstraint != null) {
                val filteredList = mOriginalPoliticianList.filter { it.name.stripAccents().startsWith(filteredConstraint, true) }
                results.values = filteredList
                results.count = filteredList.size
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) =
                if (results.count > 0) {
                    mNonReliablePoliticiansList.clear()
                    mNonReliablePoliticiansList.addAll(results.values as ArrayList<Politician>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
    }

    private fun String.stripAccents(): String {

        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")//$NON-NLS-1$
        val decomposed = Normalizer.normalize(this, Normalizer.Form.NFD)
        // Note that this doesn't correctly remove ligatures...
        return pattern.matcher(decomposed).replaceAll("")//$NON-NLS-1$
    }

}