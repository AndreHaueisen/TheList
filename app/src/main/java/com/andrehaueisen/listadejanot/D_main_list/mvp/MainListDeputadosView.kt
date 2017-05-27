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
import com.andrehaueisen.listadejanot.utilities.BUNDLE_DEPUTADOS
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER

/**
 * Created by andre on 4/30/2017.
 */
class MainListDeputadosView : Fragment(), MainListMvpContract.DeputadosView{

    companion object NewInstance{
        fun newInstance() : MainListDeputadosView {
            return MainListDeputadosView()
        }
    }

    private lateinit var mDeputadosRecyclerView: RecyclerView
    private val mDeputadosList = ArrayList<Politician>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.d_fragment_main_list_deputados, container, false)
        setRecyclerView(view)

        return view
    }

    private fun setRecyclerView(view: View) {
        mDeputadosRecyclerView = view.findViewById(R.id.deputados_recycler_view) as RecyclerView
        mDeputadosRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        val deputadosAdapter = PoliticianListAdapter(activity, mDeputadosList)

        mDeputadosRecyclerView.layoutManager = layoutManager
        mDeputadosRecyclerView.adapter = deputadosAdapter
    }

    override fun notifyDeputadosNewList(deputados: ArrayList<Politician>) {
        if(mDeputadosList.isNotEmpty()) mDeputadosList.clear()

        mDeputadosList.addAll(deputados)
        mDeputadosRecyclerView.adapter.notifyDataSetChanged()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null) {
            mDeputadosList.addAll(savedInstanceState.getParcelableArrayList<Politician>(BUNDLE_DEPUTADOS))
            mDeputadosRecyclerView.layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_MANAGER))
            mDeputadosRecyclerView.adapter.notifyDataSetChanged()
            savedInstanceState.clear()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_DEPUTADOS, mDeputadosList)
        outState.putParcelable(BUNDLE_MANAGER, mDeputadosRecyclerView.layoutManager.onSaveInstanceState())

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}