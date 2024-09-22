package com.savestatus.wsstatussaver.interfaces

import com.savestatus.wsstatussaver.model.Country

interface ICountryCallback {
    fun countryClick(country: Country)
}