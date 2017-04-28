package com.andrehaueisen.listadejanot.C_main_list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Created by andre on 4/24/2017.
 */
class PoliticianListAdapter(val context: Context, val politicianList: ArrayList<Politician>) : RecyclerView.Adapter<PoliticianListAdapter.PoliticianHolder>() {

    private val VIEW_TYPE_DEPUTADO = 0
    private val VIEW_TYPE_SENADOR = 1
    private val mRoundendCornerTranformation = RoundedCornersTransformation(context, 10, 0)

    override fun getItemCount(): Int {
        return politicianList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliticianHolder {
        val inflater = LayoutInflater.from(context)
        val view : View
        if (viewType == VIEW_TYPE_DEPUTADO) {
            view = inflater.inflate(R.layout.item_deputado, parent, false)
            return PoliticianHolder(VIEW_TYPE_DEPUTADO, view)
        } else {
            view = inflater.inflate(R.layout.item_senador, parent, false)
            return PoliticianHolder(VIEW_TYPE_SENADOR, view)
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

    inner class PoliticianHolder(view_type : Int, itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var mEmailTextView : TextView

        init {
            if(view_type == VIEW_TYPE_SENADOR){
                mEmailTextView  = itemView.findViewById(R.id.email_text_view) as TextView
            }
        }

        val mPoliticianImageView : ImageView = itemView.findViewById(R.id.politician_image_view) as ImageView
        val mNameTextView : TextView = itemView.findViewById(R.id.name_text_view) as TextView

        //val mVotesNumberTextView : TextView
        //val mVoteImageView : ImageView

        internal fun bindDataToView(politician: Politician){

            Glide.with(context)
                    .load(politician.image)
                    .bitmapTransform(mRoundendCornerTranformation)
                    .crossFade()
                    //.placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mPoliticianImageView)
            mNameTextView.text = politician.name
            // mVotesNumberTextView.text = //voteNumber

            if(politician.post == Politician.Post.SENADOR){
                mEmailTextView.text = politician.email
            }

            //mVoteImageView.setOnClickListener { view ->  }
        }

    }
}