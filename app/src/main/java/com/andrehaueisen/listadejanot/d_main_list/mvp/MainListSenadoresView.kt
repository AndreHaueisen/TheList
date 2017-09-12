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
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.BUNDLE_SENADORES
import com.andrehaueisen.listadejanot.utilities.showSnackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by andre on 4/20/2017.
 */
class MainListSenadoresView : Fragment(), MainListMvpContract.SenadoresView {

    private var mIsSortedByName = true

    companion object NewInstance {
        fun newInstance(): MainListSenadoresView = MainListSenadoresView()
    }

    private lateinit var mSenadoresRecyclerView: RecyclerView
    private lateinit var mEmptyListTextView: TextView
    private val mSenadorList = ArrayList<Politician>()

    interface SenadoresDataFetcher{
        fun subscribeToSenadores()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.d_fragment_main_list_senadores, container, false)
        mEmptyListTextView = view.findViewById(R.id.empty_main_list_text_view)
        setHasOptionsMenu(true)
        setRecyclerView(view)

        return view
    }


    override fun onResume() {
        super.onResume()

        if(!(activity as MainListPresenterActivity).mIsScreenRotation) {
            (activity as MainListSenadoresView.SenadoresDataFetcher).subscribeToSenadores()
        }
    }

    private fun setRecyclerView(view: View) {
        mSenadoresRecyclerView = view.findViewById(R.id.senadores_recycler_view)
        mSenadoresRecyclerView.setHasFixedSize(true)

        val senadoresAdapter = PoliticianListAdapter(activity, mSenadorList)
        mSenadoresRecyclerView.layoutManager = LinearLayoutManager(activity)
        mSenadoresRecyclerView.adapter = senadoresAdapter
    }

    override fun notifySenadoresNewList(senadores: ArrayList<Politician>) {
        if (mSenadorList.isNotEmpty()) mSenadorList.clear()

        changeVisibilityStatus(senadores)

        if(senadores.isNotEmpty()) {
            mSenadorList.addAll(senadores)
            mSenadoresRecyclerView.adapter.notifyDataSetChanged()

        }
    }

    private fun changeVisibilityStatus(data: ArrayList<Politician>){
        if(data.isEmpty()){
            mEmptyListTextView.visibility = View.VISIBLE
            mSenadoresRecyclerView.visibility = View.INVISIBLE
        }else{
            mEmptyListTextView.visibility = View.INVISIBLE
            mSenadoresRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun sortSenadoresList() {
        mIsSortedByName = !mIsSortedByName

        val comparator: Comparator<Politician>
        val message: String
        if (mIsSortedByName) {
            comparator = Politician.Comparators.NAME
            message = getString(R.string.senadores_sorted_by_name)
        } else {
            comparator = Politician.Comparators.VOTE_NUMBER
            message = getString(R.string.senadores_sorted_by_vote_number)
        }

        Observable.just(mSenadorList)
                .doOnComplete {
                    mSenadoresRecyclerView.adapter.notifyDataSetChanged()
                    mSenadoresRecyclerView.showSnackbar(message, Snackbar.LENGTH_SHORT)
                }
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ senadorList -> Collections.sort(senadorList, comparator) },
                        { error -> Log.e(MainListSenadoresView::class.java.simpleName, error.message) })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_main_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_sort_list -> {
                sortSenadoresList()
            }
        }

        return true
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            mSenadorList.addAll(savedInstanceState.getParcelableArrayList(BUNDLE_SENADORES))
            changeVisibilityStatus(mSenadorList)

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

    override fun onDestroy() = super.onDestroy()
}