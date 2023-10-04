package com.example.newsapp.ui
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.newsapp.R
import com.example.newsapp.database.NewsDatabase
import com.example.newsapp.ui.fragment.CategoryFragment
import com.example.newsapp.ui.fragment.SavedFragment
import com.example.newsapp.ui.fragment.SearchFragment
import com.example.newsapp.viewmodel.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.NewsViewModelProviderFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var isListening = false
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var viewModel: NewsViewModel

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(
            this
        )
    }

    private val allowPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }


    private fun startListen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object: RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                makeText(this@MainActivity,"Speak", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {
                makeText(this@MainActivity,"Listening", Toast.LENGTH_SHORT).show()
            }

            override fun onRmsChanged(p0: Float) { }
            override fun onBufferReceived(p0: ByteArray?) { }
            override fun onEndOfSpeech() {}
            override fun onError(p0: Int) {}
            override fun onResults(bundle: Bundle?) {
                bundle?.let {
                    Log.d("Voice", "on result called")
                    val result = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d("Voice", "$result")
                    if (result != null) {
                        val string = result[0].lowercase()
                        Toast.makeText(this@MainActivity, string, Toast.LENGTH_SHORT).show()
                        if(string.contains("sports")){
                            openSportsFragment()
                        }else if(string.contains("trending")){
                            Log.d("Voice", "enter trending")
                            openTrendingFragment()
                        }else if(string.contains("business")){
                            openBusinessFragment()
                        }else if(string.contains("health")){
                            openHealthFragment()
                        }else if(result[0].contains("technology")){
                            openTechnologyFragment()
                        }else if(result[0].contains("saved")){
                            openSavedFragment()
                        }else if(result[0].contains("settings")){
                            openSettingsActivity()
                        }else if(result[0].contains("search for")){
                            val searchText = string.drop((string.indexOf("search") + 10))
                            openSearchFragment(searchText)
                        }else if(result[0].contains("search")){
                        val searchText = string.drop((string.indexOf("search") + 7))
                        openSearchFragment(searchText)
                        }else{
                            makeText(this@MainActivity,"Command not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onPartialResults(p0: Bundle?) { }
            override fun onEvent(p0: Int, p1: Bundle?) { }
        })
        speechRecognizer.startListening(intent)
    }

    private fun openSearchFragment(searchText : String) {
        viewModel.keyword = searchText
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SearchFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openSavedFragment() {
        findViewById<ImageView>(R.id.ic_search).visibility = GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SavedFragment(), "SavedFragment")
            .commit()
    }

    private fun openTechnologyFragment() {
        viewModel.category = "technology"
        findViewById<ImageView>(R.id.ic_search).visibility = VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoryFragment())
            .commit()
    }

    private fun openHealthFragment() {
        viewModel.category = "health"
        findViewById<ImageView>(R.id.ic_search).visibility = VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoryFragment())
            .commit()
    }

    private fun openBusinessFragment() {
        viewModel.category = "business"
        findViewById<ImageView>(R.id.ic_search).visibility = VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoryFragment())
            .commit()
    }

    private fun openTrendingFragment() {
        viewModel.category = "trending"
        findViewById<ImageView>(R.id.ic_search).visibility = VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoryFragment())
            .commit()
    }

    private fun openSportsFragment() {
        viewModel.category = "sports"
        findViewById<ImageView>(R.id.ic_search).visibility = VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoryFragment())
            .commit()
    }


    private fun getPermissionOverO(context: Context, call: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("Voice", "Permission granted")
                call.invoke()
            } else {
                Log.d("Voice", "Permission not granted")
                allowPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newsDao=NewsDatabase(this).getNewsDao()
        val newsRepository = NewsRepository(NewsDatabase(this),newsDao)
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]

        if(savedInstanceState == null){
          viewModel.category = "trending"
          openTrendingFragment()
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            findViewById(R.id.toolbar),
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)


       findViewById<FloatingActionButton>(R.id.fab_voice).setOnTouchListener { _, motionEvent ->
           when (motionEvent.action) {
               MotionEvent.ACTION_UP -> {
                   speechRecognizer.stopListening()
                   return@setOnTouchListener true
               }
               MotionEvent.ACTION_DOWN -> {
                   getPermissionOverO(this) {
                       startListen()
                   }
                   return@setOnTouchListener true
               }
               else -> {
                   return@setOnTouchListener true
               }
           }
       }


        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            findViewById<ImageView>(R.id.ic_search).visibility = VISIBLE
            when (menuItem.itemId) {
                R.id.nav_trending -> {
                    openTrendingFragment()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_saved -> {
                    openSavedFragment()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_sports -> {
                    openSportsFragment()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_technology -> {
                   openTechnologyFragment()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_business -> {
                    openBusinessFragment()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_health -> {
                  openHealthFragment()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    openSettingsActivity()
                }
            }
            true
        }

    }

    fun onSearchIconClick(view : View){
        openSearchFragment("")
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStack()
        }
        else{
            finish()
        }
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
