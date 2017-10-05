package com.andrehaueisen.listadejanot.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Image(@SerializedName("contextLink") @Expose var contextLink: String? = null,
                 @SerializedName("height") @Expose var height: Int? = null,
                 @SerializedName("width") @Expose var width: Int? = null,
                 @SerializedName("byteSize") @Expose var byteSize: Int? = null,
                 @SerializedName("thumbnailLink") @Expose var thumbnailLink: String? = null,
                 @SerializedName("thumbnailHeight") @Expose var thumbnailHeight: Int? = null,
                 @SerializedName("thumbnailWidth") @Expose var thumbnailWidth: Int? = null)




