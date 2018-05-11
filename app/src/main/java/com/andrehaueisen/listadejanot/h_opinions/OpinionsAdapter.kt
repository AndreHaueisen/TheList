package com.andrehaueisen.listadejanot.h_opinions

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.andrehaueisen.listadejanot.R

/**
 * Created by andre on 8/1/2017.
 */
class OpinionsAdapter(val context: Context, opinionsMap: HashMap<String, String>): RecyclerView.Adapter<OpinionsAdapter.OpinionViewHolder>(){

    private var opinions = opinionsMap.values.toMutableList()
    private var emailKeys = opinionsMap.keys.toMutableSet()

    fun resetData(){
        opinions.clear()
        emailKeys.clear()
        notifyDataSetChanged()
    }

    fun addItem(opinion: String, emailKey: String){
        opinions.add(opinion)
        emailKeys.add(emailKey)
        notifyItemInserted(opinions.size)
    }

    fun changeItem(opinion: String, emailKey: String){
        val position = emailKeys.indexOf(emailKey)
        opinions[position] = opinion

        notifyItemChanged(position)
    }

    fun removeItem(emailKey: String){
        val position = emailKeys.indexOf(emailKey)
        emailKeys.remove(emailKey)
        opinions.removeAt(position)

        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpinionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_opinion, parent, false)
        return OpinionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OpinionViewHolder, position: Int) {
        holder.onBindView(position)
    }

    override fun getItemCount(): Int = opinions.size

    inner class OpinionViewHolder(view: View): RecyclerView.ViewHolder(view){

        private val opinionTextView = view.findViewById<TextView>(R.id.opinion_text_view)
        private val userNameTextView = view.findViewById<TextView>(R.id.opinion_owner_text_view)

        fun onBindView(position: Int){
            opinionTextView.text = opinions[position]
            userNameTextView.text = emailKeys.elementAt(position).substringBefore('@')
        }
    }
}