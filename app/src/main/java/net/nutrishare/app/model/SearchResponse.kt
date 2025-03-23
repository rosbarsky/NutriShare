package net.nutrishare.app.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("products") val products: List<Product>
)
