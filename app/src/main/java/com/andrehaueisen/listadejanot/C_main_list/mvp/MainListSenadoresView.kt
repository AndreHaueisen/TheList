package com.andrehaueisen.listadejanot.C_main_list.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.listadejanot.C_main_list.PoliticianListAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants


/**
 * Created by andre on 4/20/2017.
 */
class MainListSenadoresView : Fragment(), MainListMvpContract.SenadoresView {

    companion object NewInstance{
        fun newInstance() : MainListSenadoresView{
            return MainListSenadoresView()
        }
    }
    private lateinit var mSenadoresRecyclerView: RecyclerView
    private var mSenadorList = ArrayList<Politician>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_main_list_senadores, container, false)
        setRecyclerView(view)

        return view
    }

    private fun setRecyclerView(view: View) {

        mSenadoresRecyclerView = view.findViewById(R.id.senadores_recycler_view) as RecyclerView
        mSenadoresRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        val senadoresAdapter = PoliticianListAdapter(context, mSenadorList)
        mSenadoresRecyclerView.layoutManager = layoutManager
        mSenadoresRecyclerView.adapter = senadoresAdapter
    }

    override fun notifySenadorAddition(senador: Politician) {
        mSenadorList.add(senador)
        mSenadoresRecyclerView.adapter.notifyItemInserted(mSenadorList.size)
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null) {
            mSenadorList.addAll(savedInstanceState.getParcelableArrayList<Politician>(Constants.BUNDLE_SENADORES))
            mSenadoresRecyclerView.layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(Constants.BUNDLE_MANAGER))
            mSenadoresRecyclerView.adapter.notifyDataSetChanged()
            savedInstanceState.clear()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(Constants.BUNDLE_SENADORES, mSenadorList)
        outState.putParcelable(Constants.BUNDLE_MANAGER, mSenadoresRecyclerView.layoutManager.onSaveInstanceState())

        super.onSaveInstanceState(outState)
    }
}