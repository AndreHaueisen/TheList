package com.andrehaueisen.listadejanot.C_main_list.mvp

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import com.andrehaueisen.listadejanot.C_main_list.PoliticiansPagesAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import kotlinx.android.synthetic.main.activity_main_list.*


/**
 * Created by andre on 4/20/2017.
 */
class MainListView(val mPresenterActivity: MainListPresenterActivity) : MainListMvpContract.View {

    private val mPagerAdapter : ViewPager
    private val mBottomNavigationView: BottomNavigationView
    private var mBundle: Bundle? = null

    init {
        mPresenterActivity.setContentView(R.layout.activity_main_list)
        mPagerAdapter = mPresenterActivity.politicians_pager_adapter
        mBottomNavigationView = mPresenterActivity.navigation
    }

    constructor(presenterActivity: MainListPresenterActivity, bundle: Bundle) : this(presenterActivity){
        mBundle = bundle
    }

    override fun setViews() {
        setPagerAdapter()
        setBottomNavigationView()
    }

    fun setPagerAdapter(){
        mPagerAdapter.adapter = PoliticiansPagesAdapter(mPresenterActivity.supportFragmentManager)
        mPagerAdapter.offscreenPageLimit = 2
        mPagerAdapter.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> mBottomNavigationView.selectedItemId = R.id.navigation_senadores
                    1 -> mBottomNavigationView.selectedItemId = R.id.navigation_deputados
                }

            }
        })
        if(mBundle != null){
            mPagerAdapter.onRestoreInstanceState(mBundle?.getParcelable(Constants.BUNDLE_PAGER_ADAPTER))
        }
    }

    private fun setBottomNavigationView() {
        mBottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.navigation_senadores -> {
                    mPagerAdapter.setCurrentItem(0, true)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_deputados -> {
                    mPagerAdapter.setCurrentItem(1, true)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_notifications -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }

            false
        }
    }

    override fun notifyDeputadoAddition(deputado: Politician) {
        val deputadoFragment = (mPagerAdapter.adapter as PoliticiansPagesAdapter).getItem(1)
        (deputadoFragment as MainListDeputadosView).notifyDeputadoAddition(deputado)
    }

    override fun notifySenadorAddition(senador: Politician) {
        val senadorFragment = (mPagerAdapter.adapter as PoliticiansPagesAdapter).getItem(0)
        (senadorFragment as MainListSenadoresView).notifySenadorAddition(senador)
    }

    override fun onSaveInstanceState() : Bundle {
        val bundle = Bundle()
        bundle.putParcelable(Constants.BUNDLE_PAGER_ADAPTER, mPagerAdapter.onSaveInstanceState())
        return bundle
    }
}