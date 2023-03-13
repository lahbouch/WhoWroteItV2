package com.example.whowroteitLoader

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.example.asynctaskloaderdemo.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<String> {
    private lateinit var mBookInput: EditText
    private lateinit var mTitleText: TextView
    private lateinit var mAuteurText: TextView
    private lateinit var mSearchBtn: Button
    private lateinit var mThumbnailImg: ImageView
    lateinit var lm : LoaderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBookInput = findViewById(R.id.bookInput)
        mTitleText = findViewById(R.id.titleText)
        mAuteurText = findViewById(R.id.authorText)
        mSearchBtn = findViewById(R.id.searchButton)
        mThumbnailImg = findViewById(R.id.thumbnail)
        lm = LoaderManager.getInstance(this)


        mSearchBtn.setOnClickListener {
            searchBooks(it)
        }

        if (lm.getLoader<Int>(0) != null){
            lm.initLoader(0,null,this)
        }

    }

    private fun searchBooks(view: View) {
        val q = mBookInput.text.toString()
        if (q.isEmpty()) {
            Toast.makeText(this, "enter a book name", Toast.LENGTH_SHORT).show()
            mAuteurText.text = getString(R.string.no_search_term)
            mAuteurText.visibility = View.VISIBLE
            return
        }
        val inputManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }



        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "connect to the internet!", Toast.LENGTH_SHORT).show()
            return
        }
        mAuteurText.text = getString(R.string.loading)
        mAuteurText.visibility = View.VISIBLE
        val queryBundle = Bundle()
        queryBundle.putString("q", q)
        lm.restartLoader(0, queryBundle, this)

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<String> {
        var q = ""
        if (args != null) {
            q = args.getString("q").toString()
        }
        return BookLoader(this, q = q)
    }

    override fun onLoaderReset(loader: Loader<String>) {
    }


    override fun onLoadFinished(loader: Loader<String>, data: String?) {
        val DEFAULT_THUMBNAIL =
            "https://www.icarda.org/sites/default/files/styles/d02/public/feeds/publications/HPbzT4h0.png?itok=cUpFPYMT"
        try {
            val jsonRes = JSONObject(data)
            val books: JSONArray = jsonRes.getJSONArray("items")

            var i = 0

            var title: String? = null
            var auteurs: String? = null
            var thumbnail: String? = null

            while (i < books.length() && (title == null && auteurs == null)) {
                val book = books.getJSONObject(i)
                val volumeInfo = book.getJSONObject("volumeInfo")

                try {

                    title = volumeInfo.getString("title")
                    auteurs = volumeInfo.getString("authors")
                    if (volumeInfo.has("imageLinks")) {
                        thumbnail = volumeInfo.getJSONObject("imageLinks").getString("thumbnail")
                    } else {
                        thumbnail = DEFAULT_THUMBNAIL
                    }
                    Log.e("TAG", "thumbnail: ${thumbnail}")
                } catch (e: JSONException) {
                    Log.e("TAG", "onPostExecute: ${e.message}")
                }
                i++
            }

            if (title != null && auteurs != null) {
                mTitleText?.text = (title);
                mTitleText?.visibility = View.VISIBLE
                mAuteurText?.text = auteurs;
                mAuteurText?.visibility = View.VISIBLE;
                Picasso.get().load(thumbnail)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .into(mThumbnailImg)
                mThumbnailImg.visibility = View.VISIBLE

            } else {
                mTitleText.setText(R.string.no_result)
                mAuteurText.text = ""
                mThumbnailImg.visibility = View.GONE
            }

        } catch (e: JSONException) {
            Log.e("TAG", "onPostExecute: ${e.message}")
            mTitleText.setText(R.string.no_result)
            mAuteurText.text = ""
            mThumbnailImg.visibility = View.GONE
        }
    }



}
