package com.andrehaueisen.listadejanot.d_main_list

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.View



/**
 * Created by andre on 8/30/2017.
 */
class ScrollAwareFABBehavior : CoordinatorLayout.Behavior<FloatingActionButton> {

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean =
            nestedScrollAxes == View.SCROLL_AXIS_VERTICAL

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
                                child: FloatingActionButton,
                                target: View,
                                dxConsumed: Int,
                                dyConsumed: Int,
                                dxUnconsumed: Int,
                                dyUnconsumed: Int,
                                type: Int) {

        if (dyConsumed == 0 && dyUnconsumed > 0 && child.visibility == View.VISIBLE) {
            child.visibility = View.INVISIBLE
        } else if (dyConsumed != 0 && child.visibility != View.VISIBLE) {
            child.visibility = View.VISIBLE
        } else if (dyConsumed == 0 && dyUnconsumed < 0 && child.visibility != View.VISIBLE){
            child.visibility = View.VISIBLE
        }
    }
}