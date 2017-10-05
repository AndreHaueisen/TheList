package com.andrehaueisen.listadejanot.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Item(@SerializedName("kind") @Expose var kind: String? = null,
                @SerializedName("title") @Expose var title: String? = null,
                @SerializedName("htmlTitle") @Expose var htmlTitle: String? = null,
                @SerializedName("link") @Expose var link: String? = null,
                @SerializedName("displayLink") @Expose var displayLink: String? = null,
                @SerializedName("snippet") @Expose var snippet: String? = null,
                @SerializedName("htmlSnippet") @Expose var htmlSnippet: String? = null,
                @SerializedName("mime") @Expose var mime: String? = null,
                @SerializedName("image") @Expose var image: Image? = null)



