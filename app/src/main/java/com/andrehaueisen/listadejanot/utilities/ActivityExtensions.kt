package com.andrehaueisen.listadejanot.utilities

import android.app.Activity
import android.content.Intent

fun <T> Activity.startNewActivity(classToInit: Class<T>){
    val intent = Intent(this, classToInit)
    this.startActivity(intent)
}
