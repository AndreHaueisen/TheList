package com.andrehaueisen.listadejanot.D_main_list.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.listadejanot.D_main_list.PoliticianListAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.BUNDLE_SENADORES


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
    private val mSenadorList = ArrayList<Politician>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.d_fragment_main_list_senadores, container, false)
        setRecyclerView(view)

        return view
    }

    private fun setRecyclerView(view: View) {

        mSenadoresRecyclerView = view.findViewById(R.id.senadores_recycler_view) as RecyclerView
        mSenadoresRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        val senadoresAdapter = PoliticianListAdapter(activity, mSenadorList)
        mSenadoresRecyclerView.layoutManager = layoutManager
        mSenadoresRecyclerView.adapter = senadoresAdapter
    }

    override fun notifySenadoresNewList(senadores: ArrayList<Politician>) {
        if(mSenadorList.isNotEmpty()) mSenadorList.clear()

        mSenadorList.addAll(senadores)
        mSenadoresRecyclerView.adapter.notifyDataSetChanged()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null) {
            mSenadorList.addAll(savedInstanceState.getParcelableArrayList<Politician>(BUNDLE_SENADORES))
            mSenadoresRecyclerView.layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_MANAGER))
            mSenadoresRecyclerView.adapter.notifyDataSetChanged()
            savedInstanceState.clear()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_SENADORES, mSenadorList)
        outState.putParcelable(BUNDLE_MANAGER, mSenadoresRecyclerView.layoutManager.onSaveInstanceState())

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}