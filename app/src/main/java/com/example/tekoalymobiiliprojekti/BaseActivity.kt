package com.example.tekoalymobiiliprojekti

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.view.MenuItem

open abstract class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var bottomNav: BottomNavigationView
    lateinit var toolbar: Toolbar
    lateinit var toggle: ActionBarDrawerToggle

    abstract fun getSelectedBottomNavItemId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base) // Tämä on layout, johon eri näkymät injektoidaan

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNav = findViewById(R.id.bottom_navigation)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        //Ylä menu toiminnallisuus
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.map -> {
                    startActivity(Intent(this, KarttaActivity::class.java))
                    true
                }

                R.id.save -> {
                    startActivity(Intent(this, MuistioActivity::class.java))
                    true
                }
                else -> false
            }.also { drawerLayout.closeDrawers() }
        }
        //Ala navin toiminnallisuus
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    if (getSelectedBottomNavItemId() != R.id.home) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    true
                }
                R.id.map -> {
                    if (getSelectedBottomNavItemId() != R.id.map) {
                        startActivity(Intent(this, KarttaActivity::class.java))
                    }
                    true
                }
                R.id.save -> {
                    if (getSelectedBottomNavItemId() != R.id.save) {
                        startActivity(Intent(this, MuistioActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = getSelectedBottomNavItemId()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun setContent(layoutResID: Int) {
        val container: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(layoutResID, container, true)
    }



}
