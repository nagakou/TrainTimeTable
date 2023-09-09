package com.example.traintimetable

import android.R.id




class ListData {
    var time: String? = null
    var type: String? = null
    var bound: String? = null


    fun settingTime(time: String) {
        this.time = time
    }

    fun gettingTime(): String? {
        return time
    }

    fun settingType(type: String) {
        this.type = type
    }

    fun gettingType(): String? {
        return type
    }

    fun settingBound(bound: String) {
        this.bound = bound
    }

    fun gettingBound(): String? {
        return bound
    }
}
