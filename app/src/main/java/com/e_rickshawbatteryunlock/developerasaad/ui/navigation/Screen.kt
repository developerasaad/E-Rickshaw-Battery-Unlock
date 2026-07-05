package com.e_rickshawbatteryunlock.developerasaad.ui.navigation

/**
 * Type-safe navigation destinations for the app.
 *
 * Each object represents one screen. Arguments are embedded in the route string
 * using brace syntax following Navigation Compose conventions.
 */
sealed class Screen(val route: String) {

    /** BLE scan screen — initial destination. */
    data object Scan : Screen("scan")

    /** Live battery dashboard — shown after successful connection. */
    data object Dashboard : Screen("dashboard")

    /** Recovery operation screen. */
    data object Recovery : Screen("recovery")

    /** Saved batteries list screen. */
    data object SavedBatteries : Screen("saved_batteries")

    /** Password management screen. */
    data object Password : Screen("password")
}
