// Top-level build file — plugin declarations only, no configuration here.
//
// AGP 9+ notes:
//  - kotlin.android is NOT declared here because AGP 9 provides built-in Kotlin support.
//    Applying kotlin.android alongside AGP 9 causes: "Cannot add extension with name 'kotlin'"
//  - kotlin.plugin.compose IS declared because the Compose Compiler plugin is separate
//    from the base Kotlin/Android integration.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}