package com.andrehaueisen.listadejanot.models

import android.content.Context

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import retrofit2.http.Url

data class Items(@SerializedName("kind") var kind: String? = null,
                 @SerializedName("url") var url: Url? = null,
                 @SerializedName("queries") val queries: String? = null,
                 @SerializedName("context") var context: Context? = null,
                 @SerializedName("searchInformation") val searchInformation: String? = null,
                 @SerializedName("items") @Expose var items: List<Item>? = null)
