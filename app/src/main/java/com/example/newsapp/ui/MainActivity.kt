package com.example.newsapp.ui
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.newsapp.R
import com.example.newsapp.R.layout.activity_main
import com.example.newsapp.database.NewsDatabase
import com.example.newsapp.ui.fragment.CategoryFragment
import com.example.newsapp.ui.fragment.SavedFragment
import com.example.newsapp.ui.fragment.SearchFragment
import com.example.newsapp.viewmodel.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.NewsViewModelProviderFactory
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newsRepository = NewsRepository(NewsDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
        Log.d("Main", "Start")
        setContentView(activity_main)

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CategoryFragment("trending"))
                .commit()
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



        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item clicks here
            when (menuItem.itemId) {
                R.id.nav_trending -> {
                    // Navigate to the Trending fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CategoryFragment("trending"))
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_saved -> {
                    // Navigate to the Saved fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SavedFragment())
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_sports -> {
                    // Navigate to the Saved fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CategoryFragment("sports"))
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)

                }
                R.id.nav_technology -> {
                    // Navigate to the Saved fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CategoryFragment("technology"))
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_business -> {
                    // Navigate to the Saved fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CategoryFragment("business"))
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_health -> {
                    // Navigate to the Saved fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CategoryFragment("health"))
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

    }

    fun onSearchIconClick(view : View){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SearchFragment())
            .addToBackStack(null)
            .commit()
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
