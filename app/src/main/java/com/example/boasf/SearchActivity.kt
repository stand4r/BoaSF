@file:Suppress("OVERRIDE_DEPRECATION")

package com.example.boasf

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.Typeface.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Gravity.*
import android.view.Menu
import android.view.View
import android.view.View.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputEditText
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.util.*
import kotlin.concurrent.thread


private const val URLSEARCH = "https://avidreaders.ru/s/"
private const val REQUEST_CODE_SPEECH = 100

@Suppress("DEPRECATION")
class SearchActivity : AppCompatActivity() {
    private var flag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        supportActionBar?.title = "BoaSF"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val uri: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu)
        return true
    }

    private fun toPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun onBackPressed() {
        if (flag) {
            search()
        } else {
            finish()
        }
    }

    fun btnSearch(view: View) {
        search()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun search() {
        flag = false
        val scrollView = findViewById<ScrollView>(R.id.scrollView2)
        val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
        val btnSearch = findViewById<Button>(R.id.buttonSearch)
        val nameInput = findViewById<TextInputEditText>(R.id.bookInput)
        btnSearch.isEnabled = false
        val name = nameInput.text.toString()
        if (name in arrayListOf("05.07.2019", "05072019", "05.07.19", "050719", "572019")) {
            openViewLove()
        } else {
            if (name != "") {
                try {
                    scrollLayout.removeAllViews()
                } catch (_: Exception) {
                }
                thread {
                    addCardAuthor()
                    getAuthors(name)
                    addCardBook()
                    getBooks(name)
                }
                scrollView.visibility = VISIBLE
            } else {
                try {
                    scrollLayout.removeAllViews()
                } catch (_: Exception) {
                }
                addInputEmpty()
            }
            btnSearch.isEnabled = true
        }
    }

    private fun openViewLove() {
        val intent = Intent()
    }

    // -------------------------------------------------Microphone----------------------------------
    fun speak(@Suppress("UNUSED_PARAMETER") view: View) {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите название книги или автора")
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SPEECH -> {
                if (resultCode == RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val inp = findViewById<TextInputEditText>(R.id.bookInput)
                    inp.setText(result?.get(0).toString().capitalize(Locale.ROOT))
                }
            }
        }
    }


    // --------------------------------------------------GET BOOKS----------------------------------
    private fun getBooks(name: String) {
        if (name != "") {
            val res: Connection.Response = Jsoup
                .connect(URLSEARCH + name)
                .cookie("list_view_full_books", "1")
                .method(Connection.Method.GET)
                .execute()
            val doc: Document = res.parse()
            val divs = doc.select("div.card_info")
            if (divs.size != 0) {
                for (i in 0 until divs.size) {
                    runOnUiThread {
                        val nameBook = divs[i].select("div.book_name").select("a").text().toString()
                        val urlBook = divs[i].select("a.btn").attr("href").toString()
                        val genreBook = if (divs[i].select("a.genre").size > 0) {
                            divs[i].select("a.genre").text().toString()
                        } else {
                            divs[i].select("span").text().toString()
                        }
                        addCard(
                            nameBook,
                            urlBook,
                            genreBook
                        )
                    }
                }
            } else {
                runOnUiThread { addCardNull() }
            }
        }
    }

    private fun addCardAuthor() {
        runOnUiThread {
            val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
            val txt = TextView(this)
            txt.text = "Авторы"
            txt.textSize = 27F
            txt.textAlignment = TEXT_ALIGNMENT_CENTER
            txt.setPadding(0, toPx(50), 0, toPx(17))
            parentLayout.addView(txt)
        }
    }

    private fun addCardBook() {
        runOnUiThread {
            val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
            val txt = TextView(this)
            txt.text = "Книги"
            txt.textSize = 25F
            txt.textAlignment = TEXT_ALIGNMENT_CENTER
            txt.setPadding(0, toPx(50), 0, toPx(17))
            parentLayout.addView(txt)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addCard(name: String, url: String, author: String) {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val card = CardView(this)
        val linear = LinearLayout(this)
        val txt = TextView(this)
        val btn = Button(this)
        val txtGenre = TextView(this)
        val linearParent = LinearLayout(this)


        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )
        params.gravity = CENTER
        params.setMargins(0, 0, 0, toPx(20))
        card.layoutParams = params
        card.radius = 20.0F
        card.useCompatPadding = true
        card.elevation = 25.0F


        val params3 = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )
        linearParent.layoutParams = params3
        linearParent.orientation = LinearLayout.VERTICAL
        linearParent.setBackgroundColor(parseColor("#5777CA"))

        val params2 = LinearLayout.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        params2.setMargins(15, 0, 0, 10)
        linear.layoutParams = params2
        linear.orientation = LinearLayout.HORIZONTAL
        linear.gravity = TOP
        linear.setBackgroundColor(parseColor("#5777CA"))


        txt.textSize = 15.0F
        txt.setTextColor(Color.WHITE)
        txt.text = name
        txt.setTypeface(null, BOLD)
        txt.minLines = 1
        txt.maxLines = 2
        txt.textAlignment = TEXT_ALIGNMENT_TEXT_START
        txt.height = toPx(55)
        txt.width = toPx(255)
        txt.setPadding(toPx(5), 0, toPx(10), 5)
        txt.gravity = CENTER_HORIZONTAL and CENTER_VERTICAL


        btn.width = toPx(30)
        btn.height = WRAP_CONTENT
        btn.text = "Скачать"
        btn.setPadding(0, 0, toPx(10), 0)
        btn.setBackgroundColor(parseColor("#7734699B"))
        btn.setTextColor(Color.WHITE)
        btn.isAllCaps = false
        btn.textSize = 14.0F
        btn.setOnClickListener {
            downloadBook(url, name)
        }

        txtGenre.textSize = 14.0F
        txtGenre.setTextColor(parseColor("#ECECEC"))
        txtGenre.text = "       $author"
        txtGenre.isSingleLine = true
        txtGenre.textAlignment = TEXT_ALIGNMENT_TEXT_START
        txtGenre.setTypeface(null, ITALIC)
        txtGenre.height = toPx(33)
        txtGenre.width = toPx(310)
        txtGenre.setPadding(0, toPx(7), toPx(20), 0)
        txtGenre.gravity = CENTER_VERTICAL and CENTER_HORIZONTAL


        linear.addView(txt)
        linear.addView(btn)
        linearParent.addView(linear)
        linearParent.addView(txtGenre)
        card.addView(linearParent)
        parentLayout.addView(card)
    }

    private fun downloadBook(url: String, name: String) {
        var flag = false
        thread {
            for (i in arrayListOf("?f=fb2", "?f=epub", "?f=djvu")) {
                try {
                    val res: Connection.Response = Jsoup
                        .connect(url)
                        .cookie("list_view_full_books", "1")
                        .method(Connection.Method.GET)
                        .execute()
                    val doc: Document = res.parse()
                    val str = doc.select("a.btn")
                    val urlDownload = str.attr("href").toString().split("?")[0] + i
                    val res2 = Jsoup
                        .connect(urlDownload)
                        .cookie("list_view_full_books", "1")
                        .method(Connection.Method.GET)
                        .execute()
                    val doc2: Document = res2.parse()
                    val urlFile = doc2.select("div.dnld-info").select("a").attr("href").toString()
                    val res3 = Jsoup
                        .connect(urlFile)
                        .ignoreContentType(true)
                        .referrer(urlDownload)
                        .method(Connection.Method.GET)
                        .execute()
                    val bytes = res3.bodyAsBytes()
                    val nameFile = res3.headers()["Content-Disposition"].toString()
                        .split(";")[1].split("=")[1].drop(1)
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        nameFile
                    ).writeBytes(bytes)
                    runOnUiThread {
                        Toast.makeText(this, "Книга '$name' скачана", Toast.LENGTH_LONG).show()
                    }
                    flag = true
                } catch (e: Exception) {
                    Log.i("error", e.message.toString())
                }
            }
            runOnUiThread {
                if (!flag) {
                    Toast.makeText(
                        this,
                        "Книга '$name' не скачана\nОшибка...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // --------------------------------------------------GET AUTHORS--------------------------------
    @SuppressLint("SetTextI18n")
    private fun getAuthors(name: String) {
        if (name != "") {
            val res: Connection.Response = Jsoup
                .connect(URLSEARCH + name)
                .cookie("list_view_full_books", "1")
                .method(Connection.Method.GET)
                .execute()
            val doc: Document = res.parse()
            val divs = doc.select("div.slider").select("a")
            if (divs.size != 0) {
                for (i in 0 until divs.size) {
                    runOnUiThread {
                        val urlGenre = divs[i].attr("href").toString()
                        val nameGenre = divs[i].select("div.popular_name").text().toString()
                        addCardGenre(
                            nameGenre,
                            urlGenre
                        )
                    }
                }
            } else {
                runOnUiThread { addCardNull() }
            }
        }
    }

    private fun addCardGenre(nameGenre: String, urlGenre: String) {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val card = CardView(this)
        val linear = LinearLayout(this)
        val txt = TextView(this)
        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
        )
        params.setMargins(0, 0, 0, toPx(20))
        params.gravity = CENTER
        card.layoutParams = params
        card.radius = 35.0F
        card.useCompatPadding = true
        card.elevation = 25.0F
        val params2 = LinearLayout.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        params2.setMargins(0, 0, 0, 0)
        linear.layoutParams = params2
        linear.orientation = LinearLayout.HORIZONTAL
        linear.setBackgroundColor(parseColor("#5777CA"))
        linear.updatePadding(20, 0, 0, 0)
        linear.setOnClickListener {
            selectGenre(urlGenre)
        }

        txt.textSize = 15.0F
        txt.updatePadding(toPx(10), toPx(3), toPx(30), 0)
        txt.setTextColor(Color.WHITE)
        txt.text = nameGenre
        txt.maxLines = 2
        txt.setTypeface(null, BOLD)
        txt.textAlignment = TEXT_ALIGNMENT_TEXT_START
        txt.width = toPx(330)
        txt.height = toPx(63)
        txt.gravity = CENTER_VERTICAL
        linear.addView(txt)
        card.addView(linear)
        parentLayout.addView(card)
    }

    private fun selectGenre(urlGenre: String) {
        flag = true
        val scrollView = findViewById<ScrollView>(R.id.scrollView2)
        val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
        val btnSearch = findViewById<Button>(R.id.buttonSearch)
        btnSearch.isEnabled = false
        scrollLayout.removeAllViews()
        thread {
            val res: Connection.Response = Jsoup
                .connect(urlGenre)
                .cookie("list_view_full_books", "1")
                .method(Connection.Method.GET)
                .execute()
            val doc: Document = res.parse()
            val divs = doc.select("div.card_info")
            if (divs.size != 0) {
                for (i in 0 until divs.size) {
                    runOnUiThread {
                        val nameBook =
                            divs[i].select("div.book_name").select("a").text().toString()
                        val urlBook = divs[i].select("a.btn").attr("href").toString()
                        val genreBook = if (divs[i].select("a.genre").size > 0) {
                            divs[i].select("a.genre").text().toString()
                        } else {
                            divs[i].select("span").text().toString()
                        }
                        addCard(
                            nameBook,
                            urlBook,
                            genreBook
                        )
                    }
                }
            } else {
                runOnUiThread { addCardNull() }
            }
        }
        scrollView.visibility = VISIBLE
        btnSearch.isEnabled = true
    }

    // --------------------------------------------------BAD RESULT---------------------------------
    private fun addCardNull() {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val card = CardView(this)
        val txt = TextView(this)
        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
        )
        params.setMargins(40, 20, 40, 20)
        params.gravity = CENTER
        card.layoutParams = params
        card.radius = 30.0F
        card.useCompatPadding = true
        card.elevation = 25.0F
        txt.textSize = 16.0F
        txt.setTextColor(Color.WHITE)
        txt.setBackgroundColor(parseColor("#5777CA"))
        txt.text = "Ничего не найдено"
        txt.height = toPx(50)
        txt.width = toPx(350)
        txt.maxLines = 1
        txt.setTypeface(null, ITALIC)
        txt.textAlignment = TEXT_ALIGNMENT_CENTER
        txt.gravity = CENTER_VERTICAL
        card.addView(txt)
        parentLayout.addView(card)
    }

    private fun addInputEmpty() {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val scroll = findViewById<ScrollView>(R.id.scrollView2)
        val card = CardView(this)
        val txt = TextView(this)
        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
        )
        params.setMargins(40, 20, 40, 20)
        params.gravity = CENTER
        card.layoutParams = params
        card.radius = 30.0F
        card.useCompatPadding = true
        card.elevation = 25.0F
        txt.textSize = 16.0F
        txt.setTextColor(Color.WHITE)
        txt.setBackgroundColor(parseColor("#5777CA"))
        txt.text = "Введите название книги или автора"
        txt.height = toPx(50)
        txt.width = toPx(350)
        txt.maxLines = 1
        txt.setTypeface(null, ITALIC)
        txt.textAlignment = TEXT_ALIGNMENT_CENTER
        txt.gravity = CENTER_VERTICAL
        card.addView(txt)
        parentLayout.addView(card)
        scroll.visibility = VISIBLE
    }
}