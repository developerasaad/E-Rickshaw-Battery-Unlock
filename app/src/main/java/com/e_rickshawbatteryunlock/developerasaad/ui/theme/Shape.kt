package com.e_rickshawbatteryunlock.developerasaad.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape scale for the app.
 *
 * Large rounded corners align with the premium, modern aesthetic and make
 * touch targets feel softer and more approachable for non-technical users.
 */
val AppShapes = Shapes(
    /** Used for small components: chips, badges, buttons */
    small = RoundedCornerShape(8.dp),
    /** Used for cards, bottom sheets, dialogs */
    medium = RoundedCornerShape(16.dp),
    /** Used for full-width sheets and large containers */
    large = RoundedCornerShape(24.dp),
    /** Used for floating surfaces */
    extraLarge = RoundedCornerShape(32.dp),
)
