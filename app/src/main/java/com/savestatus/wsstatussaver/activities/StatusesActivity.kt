package com.savestatus.wsstatussaver.activities

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationBarView
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.WhatSaveViewModel
import com.savestatus.wsstatussaver.activities.base.AbsBaseActivity
import com.savestatus.wsstatussaver.dialogs.UpdateDialog
import com.savestatus.wsstatussaver.extensions.WHATSAVE_ANIM_TIME
import com.savestatus.wsstatussaver.extensions.currentFragment
import com.savestatus.wsstatussaver.extensions.getPreferredClient
import com.savestatus.wsstatussaver.extensions.hide
import com.savestatus.wsstatussaver.extensions.show
import com.savestatus.wsstatussaver.extensions.surfaceColor
import com.savestatus.wsstatussaver.extensions.whichFragment
import com.savestatus.wsstatussaver.fragments.base.AbsStatusesFragment
import com.savestatus.wsstatussaver.update.isAbleToUpdate
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatusesActivity : AbsBaseActivity(), NavigationBarView.OnItemReselectedListener,
    NavController.OnDestinationChangedListener {

    private val viewModel by viewModel<WhatSaveViewModel>()
    private lateinit var contentView: FrameLayout
    private lateinit var navigationView: NavigationBarView

    @ColorInt
    private var navigationBarColor: Int = 0
    private var navigationBarColorAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        contentView = findViewById(R.id.main_container)
        navigationView = findViewById(R.id.navigation_view)
        navigationView.setOnItemReselectedListener(this)

        navigationBarColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorSurfaceContainer,
            Color.BLACK
        )

        val navController = whichFragment<NavHostFragment>(R.id.main_container)?.navController
        if (navController != null) {
            navController.addOnDestinationChangedListener(this)
            navigationView.setupWithNavController(navController)
        }

        /*if (savedInstanceState == null) {
            //searchUpdate()
        }*/
    }

    /*private fun searchUpdate() {
        if (isAbleToUpdate()) {
            viewModel.getLatestUpdate().observe(this) { updateInfo ->
                if (updateInfo.isDownloadable(this)) {
                    UpdateDialog.create(updateInfo).show(supportFragmentManager, "UPDATE_FOUND")
                }
            }
        }
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.setupWhatsAppMenuItem(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.main_container).navigate(R.id.settingsFragment)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when (destination.id) {
            R.id.homeFragment,
            R.id.savedFragment,
            R.id.toolsFragment -> hideBottomBar(false)
            else -> hideBottomBar(true)
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val currentFragment = currentFragment(R.id.main_container)
        if (currentFragment is AbsStatusesFragment) {
            currentFragment.scrollToTop()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.main_container).navigateUp()

    override fun onDestroy() {
        findNavController(R.id.main_container).removeOnDestinationChangedListener(this)
        super.onDestroy()
    }

    private fun hideBottomBar(hide: Boolean) {
        if (hide) {
            navigationView.hide()
            animateNavigationBarColor(surfaceColor())
        }else {
            navigationView.show()
            animateNavigationBarColor(navigationBarColor)
        }
        val contentPadding = if (!hide) resources.getDimensionPixelSize(R.dimen.bottom_nav_height) else 0
        contentView.updatePadding(bottom = contentPadding)
    }

    private fun animateNavigationBarColor(color: Int) {
        navigationBarColorAnimator?.cancel()
        navigationBarColorAnimator = ValueAnimator
            .ofArgb(window.navigationBarColor, color).apply {
                duration = WHATSAVE_ANIM_TIME
                interpolator = PathInterpolator(0.4f, 0f, 1f, 1f)
                addUpdateListener { animation: ValueAnimator ->
                    setNavigationBarColor(animation.animatedValue as Int)
                }
                start()
            }
    }
}

fun Menu.setupWhatsAppMenuItem(activity: FragmentActivity) {
    this.removeItem(R.id.action_launch_client)

    val client = activity.getPreferredClient()
    if (client != null) {
        this.add(
            Menu.NONE, R.id.action_launch_client,
            Menu.FIRST, activity.getString(R.string.launch_x_client, client.displayName)
        )
            .setIcon(R.drawable.ic_open_in_new_24dp)
            .setIntent(client.getLaunchIntent(activity.packageManager))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }
}