package com.example.eventapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.eventapp.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.example.eventapp.network.AppConfig
import com.example.eventapp.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    internal lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        binding.appBarMain.fabCreateEvent?.setOnClickListener {
            navController.navigate(R.id.nav_create_event)
        }

        // Top-level destinations where the hamburger icon should be shown
        val topLevelDestinations = setOf(
            R.id.nav_events, R.id.nav_chat, R.id.nav_travel, R.id.nav_communities,
            R.id.nav_clubs, R.id.nav_profile, R.id.nav_scanner, R.id.nav_settings
        )
        
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView?.setupWithNavController(navController)
        binding.appBarMain.contentMain.bottomNavView?.setupWithNavController(navController)

        // Only show FAB on Events page
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_events) {
                binding.appBarMain.fabCreateEvent?.show()
            } else {
                binding.appBarMain.fabCreateEvent?.hide()
            }
        }

        // Setup dynamic sidebar header
        setupNavHeader()
    }

    private fun setupNavHeader() {
        val navView: NavigationView = binding.navView ?: return
        val headerView = navView.getHeaderView(0) ?: return
        
        val repository = (application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        val profileViewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        val nameView = headerView.findViewById<android.widget.TextView>(R.id.nav_header_name)
        val collegeView = headerView.findViewById<android.widget.TextView>(R.id.nav_header_college)

        val navImageView = headerView.findViewById<android.widget.ImageView>(R.id.nav_header_image)

        profileViewModel.user.observe(this) { user ->
            user?.let {
                nameView.text = it.fullName ?: getString(R.string.nav_header_user_name)
                collegeView.text = it.collegeName ?: getString(R.string.nav_header_user_info)

                val imageUrl = it.profileImageUrl
                if (!imageUrl.isNullOrEmpty() && navImageView != null) {
                    val fullUrl = if (imageUrl.startsWith("http")) imageUrl
                    else "${AppConfig.BASE_URL}${imageUrl}"
                    Glide.with(this)
                        .load(fullUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .circleCrop()
                        .into(navImageView)
                }
            }
        }

        // Listen for real-time notifications
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.wsMessages.collect { json ->
                    val type = json.optString("type")
                    if (type == "notification") {
                        val title = json.optString("title")
                        val message = json.optString("message")
                        
                        runOnUiThread {
                            val snackBar = com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "$title: $message",
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                            )
                            snackBar.setBackgroundTint(getColor(R.color.primaryColor))
                            snackBar.setTextColor(getColor(R.color.white))
                            snackBar.show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}