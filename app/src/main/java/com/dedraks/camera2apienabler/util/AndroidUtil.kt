package com.dedraks.camera2apienabler.util

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import com.dedraks.camera2apienabler.R
import com.google.android.material.snackbar.Snackbar
import eu.chainfire.libsuperuser.Shell


import java.io.*

object AndroidUtil {

    val libName = "libmmcamera_platina_imx576_sunny"
    val sdDir = Environment.getExternalStorageDirectory().path
    val appFolder = "$sdDir/Camera2APIEnabler"
    val bkpDir = "$appFolder/backup"

    fun copyResources(resId: Int, m_context: Context, path: String) {
        val input = m_context.resources.openRawResource(resId)
        val filename = m_context.resources.getResourceEntryName(resId) + ".img"

        val f = File(filename)

        if (f.exists()) f.delete()

        if (!f.exists()) {
            val out = FileOutputStream(File(path, filename))
            input.copyTo(out)
            out.close()
        }
        input.close()
    }

    fun checkCameraApi(context: Context): Int {

        var ret = -1
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                ret = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)!!
                Log.d(
                    "Img",
                    "INFO_SUPPORTED_HARDWARE_LEVEL " + characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)!!
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        return ret
    }

    fun snackbar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    fun enableApi() {
        if (Build.VERSION.SDK_INT < 28) {
            Shell.SU.run("setprop persist.camera.HAL3.enabled 1")
            Shell.SU.run("setprop persist.camera.eis.enable 1")
            Shell.SU.run("setprop persist.vendor.camera.HAL3.enabled 1")
        }
        else {
            Shell.SU.run("setprop persist.vendor.camera.HAL3.enabled 1")
            Shell.SU.run("setprop persist.vendor.camera.eis.enable 1")
        }
    }

    fun disableApi() {
        if (Build.VERSION.SDK_INT < 28) { // Oreo
            Shell.SU.run("setprop persist.camera.HAL3.enabled \"\"")
            Shell.SU.run("setprop persist.camera.eis.enable \"\"")
            Shell.SU.run("setprop persist.vendor.camera.HAL3.enabled \"\"")
        }
        else { // Pie
            Shell.SU.run("setprop persist.vendor.camera.HAL3.enabled \"\"")
            Shell.SU.run("setprop persist.vendor.camera.eis.enable \"\"")
        }
    }

    fun backupLib() {
        Shell.SU.run("mkdir -p $bkpDir")
        Shell.SU.run("cp /vendor/lib/$libName.so $bkpDir/")
    }

    fun restoreLib() {
        Shell.SU.run("mount -o remount,rw /vendor")
        Shell.SU.run("cp $bkpDir/$libName.so /vendor/lib/$libName")
        Shell.SU.run("chmod 0644 /vendor/lib/$libName")
        Shell.SU.run("mount -o remount,ro /vendor")
    }

    fun replaceLib(context: Context) {
        Shell.SU.run("mount -o remount,rw /vendor")
        if (Build.VERSION.SDK_INT < 28) { // Oreo
            AndroidUtil.copyResources(R.raw.oreo_libmmcamera_platina_imx576_sunny, context, sdDir )
            Shell.SU.run("mv -f $sdDir/oreo_$libName.img /vendor/lib/$libName.so")
        }
        else { // Pie
            AndroidUtil.copyResources(R.raw.pie_libmmcamera_platina_imx576_sunny, context, Environment.getExternalStorageDirectory().path )
            Shell.SU.run("mv -f $sdDir/pie_$libName.img /vendor/lib/$libName.so")
        }
        Shell.SU.run("chmod 0644 /vendor/lib/$libName.so")
        Shell.SU.run("mount -o remount,ro /vendor")
    }
}
