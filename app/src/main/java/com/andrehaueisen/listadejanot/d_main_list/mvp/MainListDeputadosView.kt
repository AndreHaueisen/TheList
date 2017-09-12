package com.andrehaueisen.listadejanot.d_main_list.mvp

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_main_list.PoliticianListAdapter
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.BUNDLE_DEPUTADOS
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.showSnackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by andre on 4/30/2017.
 */
class MainListDeputadosView : Fragment(), MainListMvpContract.DeputadosView {

    private var mIsSortedByName = true

    companion object NewInstance {
        fun newInstance(): MainListDeputadosView = MainListDeputadosView()
    }

    private lateinit var mDeputadosRecyclerView: RecyclerView
    private lateinit var mEmptyListTextView: TextView
    private val mDeputadosList = ArrayList<Politician>()

    interface DeputadosDataFetcher{
        fun subscribeToDeputados()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.d_fragment_main_list_deputados, container, false)
        mEmptyListTextView = view.findViewById(R.id.empty_main_list_text_view)
        setHasOptionsMenu(true)
        setRecyclerView(view)

        return view
    }

    override fun onResume() {
        super.onResume()

        if(!(activity as MainListPresenterActivity).mIsScreenRotation) {
            (activity as DeputadosDataFetcher).subscribeToDeputados()
        }
    }

    private fun setRecyclerView(view: View) {
        mDeputadosRecyclerView = view.findViewById(R.id.deputados_recycler_view)
        mDeputadosRecyclerView.setHasFixedSize(true)

        val deputadosAdapter = PoliticianListAdapter(activity, mDeputadosList)

        mDeputadosRecyclerView.layoutManager = LinearLayoutManager(activity)
        mDeputadosRecyclerView.adapter = deputadosAdapter
    }

    override fun notifyDeputadosNewList(deputados: ArrayList<Politician>) {
        if (mDeputadosList.isNotEmpty()) mDeputadosList.clear()

        changeVisibilityStatus(deputados)

        if(deputados.isNotEmpty()) {
            mDeputadosList.addAll(deputados)
            mDeputadosRecyclerView.adapter.notifyDataSetChanged()
        }
    }

    private fun changeVisibilityStatus(data: ArrayList<Politician>){
        if(data.isEmpty()){
            mEmptyListTextView.visibility = View.VISIBLE
            mDeputadosRecyclerView.visibility = View.INVISIBLE
        }else{
            mEmptyListTextView.visibility = View.INVISIBLE
            mDeputadosRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun sortDeputadosList() {
        mIsSortedByName = !mIsSortedByName

        val comparator: Comparator<Politician>
        val message: String
        if (mIsSortedByName) {
            comparator = Politician.Comparators.NAME
            message = getString(R.string.deputados_sorted_by_name)
        } else {
            comparator = Politician.Comparators.VOTE_NUMBER
            message = getString(R.string.deputados_sorted_by_vote_number)
        }

        Observable.just(mDeputadosList)
                .doOnComplete {
                    mDeputadosRecyclerView.adapter.notifyDataSetChanged()
                    mDeputadosRecyclerView.showSnackbar(message, Snackbar.LENGTH_SHORT)
                }
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ deputadoList -> Collections.sort(deputadoList, comparator) },
                        { error -> Log.e(MainListSenadoresView::class.java.simpleName, error.message) })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_main_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_sort_list -> {
                sortDeputadosList()
            }
        }

        return true
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            mDeputadosList.addAll(savedInstanceState.getParcelableArrayList(BUNDLE_DEPUTADOS))
            changeVisibilityStatus(mDeputadosList)
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

    override fun onDestroy() = super.onDestroy()
}