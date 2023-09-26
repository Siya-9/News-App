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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.database.NewsDatabase
import com.example.newsapp.ui.fragment.CategoryFragment
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
    private lateinit var navController : NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
                    as NavHostFragment
        navController = navHostFragment.navController
        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)

        val newsRepository = NewsRepository(NewsDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
        Log.d("Main", "Start")


        if(savedInstanceState == null){
            val bundle = Bundle().apply {
                putString("category", "trending")
            }
            navController.navigate(R.id.action_categoryFragment_self, bundle)
        }


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
                    val bundle = Bundle().apply {
                        putString("category", "trending")
                    }
                    navController.navigate(R.id.action_categoryFragment_self, bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_saved -> {
                    if(supportFragmentManager.findFragmentById(R.id.fragment_container) is CategoryFragment) {
                        navController.navigate(R.id.action_categoryFragment_to_savedFragment)
                    }else if(supportFragmentManager.findFragmentById(R.id.fragment_container) is SearchFragment) {
                        navController.navigate(R.id.action_searchFragment_to_savedFragment)
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_sports -> {
                    val bundle = Bundle().apply {
                        putString("category", "sports")
                    }
                    navController.navigate(R.id.action_categoryFragment_self, bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)

                }
                R.id.nav_technology -> {
                    val bundle = Bundle().apply {
                        putString("category", "technology")
                    }
                    navController.navigate(R.id.action_categoryFragment_self, bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_business -> {
                    val bundle = Bundle().apply {
                        putString("category", "business")
                    }
                    navController.navigate(R.id.action_categoryFragment_self, bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_health -> {
                    val bundle = Bundle().apply {
                        putString("category", "health")
                    }
                    navController.navigate(R.id.action_categoryFragment_self, bundle)
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    fun onSearchIconClick(view : View){
            navController.navigate(R.id.action_categoryFragment_to_searchFragment)
    }

    override fun onBackPressed() {
        if (!navController.popBackStack()) {
            // Call finish() on your Activity
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
