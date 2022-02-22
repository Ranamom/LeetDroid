package com.example.leetdroid.ui.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.leetdroid.R
import com.example.leetdroid.utils.CommonFunctions.Logout.showLogOutDialog
import com.example.leetdroid.utils.CommonUtils.composeEmail
import com.example.leetdroid.utils.CommonUtils.createEmailBody
import com.example.leetdroid.utils.CommonUtils.openLink
import com.example.leetdroid.utils.dialog.AlertDialogShower
import com.example.leetdroid.utils.dialog.AppDialogs
import com.example.leetdroid.utils.extensions.showSnackBar
import com.example.leetdroid.utils.hideSoftKeyboard
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


// TODO: hide keyboard when clicked on bottom navigation items,
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottom_navigation)

        navController = findNavController(R.id.hostFragment)
        setupBottomNavigation()

        drawerLayout = findViewById(R.id.drawer_layout)

        navigationView = findViewById(R.id.navigation_view)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(navigationView, navController)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.reportBug -> {
                    AlertDialogShower(this).show(
                        AppDialogs.ReportBug, {
                            openLink(
                                context = this,
                                "https://github.com/cdhiraj40/LeetDroid/issues/new"
                            )
                        }, {

                        }, {
                            composeEmail(
                                context = this,
                                "chauhandhiraj40@gmail.com",
                                subject = "LeetDroid Bug",
                                body = createEmailBody(context = this)
                            )
                        }
                    )
                }
                R.id.githubProject -> {
                    openLink(
                        context = this,
                        "https://github.com/cdhiraj40/LeetDroid"
                    )
                }

                // TODO add link once adding to play store.
//                R.id.rateUs -> {
//
//                }

                R.id.about -> {
                    showSnackBar(activity = this, "ASdas")
                }
            }

            NavigationUI.onNavDestinationSelected(it, navController)
            true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.logout -> {
                // log out from app
                showLogOutDialog(activity = this, context = this)
                // TODO delete all databases and shared preferences after logging out to avoid any problems with a new user or old
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        hideSoftKeyboard(this)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

}