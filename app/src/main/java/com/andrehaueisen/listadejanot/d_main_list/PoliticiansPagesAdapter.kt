package com.andrehaueisen.listadejanot.d_main_list

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListDeputadosView
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListGovernadoresView
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListSenadoresView

/**
 * Created by andre on 4/30/2017.
 */
class PoliticiansPagesAdapter(private val fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val mFragmentsList: List<Fragment>

    init {
        val senadoresFragment = MainListSenadoresView.newInstance()
        val deputadosFragment = MainListDeputadosView.newInstance()
        val governadoresFragment = MainListGovernadoresView.newInstance()

        mFragmentsList = List(3, { index ->

            when (index) {
                0 -> senadoresFragment
                1 -> deputadosFragment
                2 -> governadoresFragment
                else -> senadoresFragment
            }
        })
    }

    override fun getItem(position: Int): Fragment = mFragmentsList[position]

    fun getFragmentTag(viewPagerId: Int, fragmentPosition: Int): Fragment {
        val fragmentTag = "android:switcher:$viewPagerId:$fragmentPosition"

        return fragmentManager.findFragmentByTag(fragmentTag)
    }

    override fun getCount(): Int = mFragmentsList.size

}