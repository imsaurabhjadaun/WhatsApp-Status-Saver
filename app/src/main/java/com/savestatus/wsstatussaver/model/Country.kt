package com.savestatus.wsstatussaver.model

import com.google.gson.annotations.SerializedName

class Country internal constructor(
    @SerializedName("country_code")
    val code: Int,
    @SerializedName("iso_code")
    val isoCode: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("format")
    val format: String?
) {
    fun getId(): String = String.format("%s %s", isoCode, getFormattedCode())

    fun getFormattedCode() = String.format("+%d", code)
}