package com.e_rickshawbatteryunlock.developerasaad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Hilt dependency injection.
 *
 * Annotated with [HiltAndroidApp] so that Hilt generates the application-level
 * component and wires all injection sites at startup.
 */
@HiltAndroidApp
class BatteryUnlockApplication : Application()
