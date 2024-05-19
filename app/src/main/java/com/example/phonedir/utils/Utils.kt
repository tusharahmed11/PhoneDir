package com.example.phonedir.utils


object Utils {

    fun durationFormat(duration: String) : String{
        var durationFormatted: String? = null
        durationFormatted = if (duration.toInt() < 60) {
            "$duration sec"
        } else {
            val min = duration.toInt() / 60
            val sec = duration.toInt() % 60
            if (sec == 0) "$min min" else "$min min $sec sec"
        }
        return durationFormatted
    }

}