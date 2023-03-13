package com.example.whowroteitLoader

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

class BookLoader(context: Context, private val q: String) : AsyncTaskLoader<String>(context) {


    override fun loadInBackground(): String? {
        return NetworkUtils.getBookInfo(q)
    }

    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }
}