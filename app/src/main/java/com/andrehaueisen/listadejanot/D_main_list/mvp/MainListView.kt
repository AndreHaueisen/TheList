package com.andrehaueisen.listadejanot.D_main_list.mvp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.andrehaueisen.listadejanot.D_main_list.PoliticiansPagesAdapter
import com.andrehaueisen.listadejanot.E_add_politician.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import kotlinx.android.synthetic.main.d_activity_main_list.*


/**
 * Created by andre on 4/20/2017.
 */
class MainListView(val mPresenterActivity: MainListPresenterActivity) : MainListMvpContract.View {

    private val mPagerAdapter : ViewPager
    private val mBottomNavigationView: BottomNavigationView
    private var mBundle: Bundle? = null
    private val mToolbar : Toolbar
    private val mToolbarTitleTextView: TextView
    private var mMainListSenadores : ArrayList<Politician>? = null
    private var mMainListDeputados : ArrayList<Politician>? = null


    init {
        mPresenterActivity.setContentView(R.layout.d_activity_main_list)
        mPagerAdapter = mPresenterActivity.politicians_pager_adapter
        mBottomNavigationView = mPresenterActivity.navigation
        mToolbar = mPresenterActivity.toolbar
        mToolbarTitleTextView = mPresenterActivity.toolbar_title
    }

    constructor(presenterActivity: MainListPresenterActivity, bundle: Bundle) : this(presenterActivity){
        mBundle = bundle
    }

    override fun setViews() {
        setToolbar()
        setPagerAdapter()
        setBottomNavigationView()
    }

    fun setToolbar(){
        mPresenterActivity.setSupportActionBar(mToolbar)
        mPresenterActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    fun setPagerAdapter(){
        mPagerAdapter.adapter = PoliticiansPagesAdapter(mPresenterActivity.supportFragmentManager)
        mPagerAdapter.offscreenPageLimit = 2
        mPagerAdapter.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mBottomNavigationView.selectedItemId = R.id.navigation_senadores
                        mToolbarTitleTextView.text = mPresenterActivity.getString(R.string.banned_senadores_title) }
                    1 -> {
                        mBottomNavigationView.selectedItemId = R.id.navigation_deputados
                        mToolbarTitleTextView.text = mPresenterActivity.getString(R.string.banned_deputados_title) }
                }

            }
        })
        if (mBundle != null) {
            mPagerAdapter.onRestoreInstanceState(mBundle?.getParcelable(Constants.BUNDLE_PAGER_ADAPTER))
        }
    }

    private fun setBottomNavigationView() {
        mBottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.navigation_senadores -> {
                    mPagerAdapter.setCurrentItem(0, true)
                    mToolbarTitleTextView.text = mPresenterActivity.getString(R.string.banned_senadores_title)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_deputados -> {
                    mPagerAdapter.setCurrentItem(1, true)
                    mToolbarTitleTextView.text = mPresenterActivity.getString(R.string.banned_deputados_title)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_notifications -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }

            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?) {
        mPresenterActivity.menuInflater.inflate(R.menu.menu_main_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.action_add_politician_to_main_list -> {
                val intent = Intent(mPresenterActivity, PoliticianSelectorPresenterActivity::class.java)

                if(mMainListDeputados != null) {
                    val noPicDeputadosList = ArrayList<Politician>()
                    noPicDeputadosList.addAll(mMainListDeputados as ArrayList<Politician>)
                    noPicDeputadosList.forEach { it.image = null }
                    intent.putParcelableArrayListExtra(Constants.INTENT_DEPUTADOS_MAIN_LIST, noPicDeputadosList)
                }
                if(mMainListDeputados != null) {
                    val noPicSenadoresList = ArrayList<Politician>()
                    noPicSenadoresList.addAll(mMainListSenadores as ArrayList<Politician>)
                    noPicSenadoresList.forEach { it.image = null }
                    intent.putParcelableArrayListExtra(Constants.INTENT_SENADORES_MAIN_LIST, noPicSenadoresList)
                }
                mPresenterActivity.startActivity(intent)
            }
        }

        return true
    }

    override fun notifyDeputadosNewList(deputados: ArrayList<Politician>) {
        mMainListDeputados = deputados
        val deputadoFragment = (mPagerAdapter.adapter as PoliticiansPagesAdapter).getItem(1)
        (deputadoFragment as MainListDeputadosView).notifyDeputadosNewList(deputados)
    }

    override fun notifySenadoresNewList(senadores: ArrayList<Politician>) {
        mMainListSenadores = senadores
        val senadorFragment = (mPagerAdapter.adapter as PoliticiansPagesAdapter).getItem(0)
        (senadorFragment as MainListSenadoresView).notifySenadoresNewList(senadores)
    }

    override fun onSaveInstanceState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(Constants.BUNDLE_PAGER_ADAPTER, mPagerAdapter.onSaveInstanceState())
        return bundle
    }
}