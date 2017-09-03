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
import com.andrehaueisen.listadejanot.utilities.BUNDLE_GOVERNADORES
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.showSnackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by andre on 8/8/2017.
 */
class MainListGovernadoresView: Fragment(), MainListMvpContract.GovernadoresView {

    private var mIsSortedByName = true

    companion object NewInstance {
        fun newInstance(): MainListGovernadoresView = MainListGovernadoresView()
    }

    private lateinit var mGovernadoresRecyclerView: RecyclerView
    private lateinit var mEmptyListTextView: TextView
    private val mGovernadorList = ArrayList<Politician>()

    interface GovernadoresDataFetcher{
        fun subscribeToGovernadores()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.d_fragment_main_list_governadores, container, false)
        mEmptyListTextView = view.findViewById(R.id.empty_main_list_text_view)
        setHasOptionsMenu(true)
        setRecyclerView(view)

        return view
    }

    override fun onResume() {
        super.onResume()

        if(!(activity as MainListPresenterActivity).mIsScreenRotation) {
            (activity as MainListGovernadoresView.GovernadoresDataFetcher).subscribeToGovernadores()
        }
    }

    private fun setRecyclerView(view: View) {
        mGovernadoresRecyclerView = view.findViewById<RecyclerView>(R.id.governadores_recycler_view)
        mGovernadoresRecyclerView.setHasFixedSize(true)

        val governadoresAdapter = PoliticianListAdapter(activity, mGovernadorList)
        mGovernadoresRecyclerView.layoutManager = LinearLayoutManager(activity)
        mGovernadoresRecyclerView.adapter = governadoresAdapter
    }

    override fun notifyGovernadoresNewList(governadores: ArrayList<Politician>) {
        if (mGovernadorList.isNotEmpty()) mGovernadorList.clear()

        changeVisibilityStatus(governadores)

        if(governadores.isNotEmpty()) {

            mGovernadorList.addAll(governadores)
            mGovernadoresRecyclerView.adapter.notifyDataSetChanged()
        }
    }

    private fun changeVisibilityStatus(data: ArrayList<Politician>){
        if(data.isEmpty()){
            mEmptyListTextView.visibility = View.VISIBLE
            mGovernadoresRecyclerView.visibility = View.INVISIBLE
        }else{
            mEmptyListTextView.visibility = View.INVISIBLE
            mGovernadoresRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun sortGovernadoresList() {
        mIsSortedByName = !mIsSortedByName

        val comparator: Comparator<Politician>
        val message: String
        if (mIsSortedByName) {
            comparator = Politician.Comparators.NAME
            message = getString(R.string.governadores_sorted_by_name)
        } else {
            comparator = Politician.Comparators.VOTE_NUMBER
            message = getString(R.string.governadores_sorted_by_vote_number)
        }

        Observable.just(mGovernadorList)
                .doOnComplete {
                    mGovernadoresRecyclerView.adapter.notifyDataSetChanged()
                    mGovernadoresRecyclerView.showSnackbar(message, Snackbar.LENGTH_SHORT)
                }
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ governadorList -> Collections.sort(governadorList, comparator) },
                        { error -> Log.e(MainListGovernadoresView::class.java.simpleName, error.message) })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_main_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_sort_list -> {
                sortGovernadoresList()
            }
        }

        return true
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            mGovernadorList.addAll(savedInstanceState.getParcelableArrayList<Politician>(BUNDLE_GOVERNADORES))
            changeVisibilityStatus(mGovernadorList)
            mGovernadoresRecyclerView.layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_MANAGER))
            mGovernadoresRecyclerView.adapter.notifyDataSetChanged()
            savedInstanceState.clear()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_GOVERNADORES, mGovernadorList)
        outState.putParcelable(BUNDLE_MANAGER, mGovernadoresRecyclerView.layoutManager.onSaveInstanceState())

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() = super.onDestroy()
}