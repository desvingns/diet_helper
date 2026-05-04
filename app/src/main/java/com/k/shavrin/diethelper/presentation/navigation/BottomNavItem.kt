package com.k.shavrin.diethelper.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Today : BottomNavItem(Routes.TODAY, Icons.Filled.Today, "Сегодня")
    object History : BottomNavItem(Routes.HISTORY, Icons.Filled.History, "История")
    object Weight : BottomNavItem(Routes.WEIGHT, Icons.Filled.MonitorWeight, "Вес")
    object Settings : BottomNavItem(Routes.SETTINGS, Icons.Filled.Settings, "Настройки")
}

val BottomNavItems = listOf(
    BottomNavItem.Today,
    BottomNavItem.History,
    BottomNavItem.Weight,
    BottomNavItem.Settings
)
