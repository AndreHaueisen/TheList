package com.andrehaueisen.listadejanot.D_main_list

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

        //@BindView(R.id.politician_image_view)
        val mPoliticianImageView: ImageView
        //@BindView(R.id.name_text_view)
        val mNameTextView : TextView
       // @Nullable @BindView(R.id.email_text_view)
        val mEmailTextView : TextView
       // @BindView(R.id.votes_number_text_view)
        lateinit var mVotesNumberTextView : TextView
       // @BindView(R.id.add_to_vote_count_image_view)
        lateinit var mVoteImageView : ImageView

        init {
            mPoliticianImageView = itemView.findViewById(R.id.politician_image_view) as ImageView
            mNameTextView = itemView.findViewById(R.id.name_text_view) as TextView
            mEmailTextView = itemView.findViewById(R.id.email_text_view) as TextView
        }

        internal fun bindDataToView(politician: Politician){

            Glide.with(context)
                    .load(politician.image)
                    .bitmapTransform(mRoundendCornerTranformation)
                    .crossFade()
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