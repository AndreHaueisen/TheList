package com.andrehaueisen.listadejanot.d_main_list.mvp

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
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


/**
 * Created by andre on 4/20/2017.
 */
class MainListSenadoresView : Fragment(), MainListMvpContract.SenadoresView {

    private var mIsSortedByName = true

    companion object NewInstance {
        fun newInstance(): MainListSenadoresView {
            return MainListSenadoresView()
        }
    }

    private lateinit var mSenadoresRecyclerView: RecyclerView
    private val mSenadorList = ArrayList<Politician>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.d_fragment_main_list_senadores, container, false)
        setHasOptionsMenu(true)
        setRecyclerView(view)

        return view
    }

    private fun setRecyclerView(view: View) {
        mSenadoresRecyclerView = view.findViewById(R.id.senadores_recycler_view) as RecyclerView
        mSenadoresRecyclerView.setHasFixedSize(true)

        val senadoresAdapter = PoliticianListAdapter(activity, mSenadorList)
        mSenadoresRecyclerView.layoutManager = LinearLayoutManager(activity)
        mSenadoresRecyclerView.adapter = senadoresAdapter
    }

    override fun notifySenadoresNewList(senadores: ArrayList<Politician>) {
        if (mSenadorList.isNotEmpty()) mSenadorList.clear()

        mSenadorList.addAll(senadores)
        mSenadoresRecyclerView.adapter.notifyDataSetChanged()
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