package com.dedraks.camera2apienabler.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dedraks.camera2apienabler.R
import com.dedraks.camera2apienabler.util.AndroidUtil
import com.google.android.material.navigation.NavigationView
import eu.chainfire.libsuperuser.Shell
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpActionBar()

        textViewMain.text = getDeviceInfo()

        setupPermissions()

        button_enable_api.setOnClickListener {
            if (checkRoot(it))
                AndroidUtil.enableApi()
        }
        button_disable_api.setOnClickListener {
            if (checkRoot(it))
                AndroidUtil.disableApi()
        }
        button_backup_library.setOnClickListener {
            if (checkRoot(it))
                AndroidUtil.backupLib()
        }
        button_replace_library.setOnClickListener {
            if (checkRoot(it))
                AndroidUtil.replaceLib(context!!)
        }
        button_restore_library.setOnClickListener {
            if (checkRoot(it))
                AndroidUtil.restoreLib()
        }
        button_reboot.setOnClickListener {
            if (checkRoot(it))
                Shell.SU.run("reboot")
        }
    }

    private fun setUpActionBar() {

        lateinit var appBarConfiguration: AppBarConfiguration
        val navController = (activity as AppCompatActivity).findNavController(R.id.nav_host_fragment)

        // Set the start destination to Main Fragment so you won't get a back button in this fragment
        navController.graph.startDestination = R.id.main_fragment

        val drawerLayout : DrawerLayout? = (activity as AppCompatActivity).findViewById(R.id.drawer_layout)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.main_fragment, R.id.about_fragment),
            drawerLayout)

        (activity as AppCompatActivity).setupActionBarWithNavController(navController, appBarConfiguration)

        val sideNavView = (activity as AppCompatActivity).findViewById<NavigationView>(R.id.nav_view2)
        sideNavView?.setupWithNavController(navController)

        (activity as AppCompatActivity).supportActionBar?.show()

        (activity as MainActivity).appBarConfiguration = appBarConfiguration
        (activity as MainActivity).navController = navController
    }

    private val WRITE_REQUEST_CODE = 101

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(activity!!,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AndroidUtil.snackbar(button_enable_api, getString(R.string.permission_denied))
                }
            }
        }
    }

    private fun getDeviceInfo(): String {
        var infoText = Build.MODEL + "\nAndroid "
        infoText += Build.VERSION.RELEASE
        infoText += "\nCamera API: "
        infoText += if (AndroidUtil.checkCameraApi(context!!) > 2) "Enabled" else "Disabled"

        return infoText
    }

    private fun checkRoot(view: View) = if (Shell.SU.available()) { true } else { AndroidUtil.snackbar(view, getString(R.string.root_needed)); false }
}
