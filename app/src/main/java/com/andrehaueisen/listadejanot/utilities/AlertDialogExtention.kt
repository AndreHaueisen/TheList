package com.andrehaueisen.listadejanot.utilities

import android.support.v7.app.AlertDialog

/**
 * Created by andre on 8/26/2017.
 */
fun AlertDialog.Builder.createNeutralDialog(title: String? = null, message: String? = null): AlertDialog.Builder{

    title?.let { this.setTitle(title) }
    message?.let { this.setMessage(message) }
    this.setNeutralButton(android.R.string.ok, {dialog, _ -> dialog.dismiss()})

    return this
}
