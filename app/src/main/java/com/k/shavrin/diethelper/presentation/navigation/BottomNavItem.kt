package com.k.shavrin.diethelper.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Today : BottomNavItem(Routes.TODAY, Icons.Rounded.RestaurantMenu, "Сегодня")
    object Workouts : BottomNavItem(Routes.WORKOUTS, Icons.Rounded.FitnessCenter, "Тренинг")
    object History : BottomNavItem(Routes.HISTORY, Icons.Rounded.History, "История")
    object Statistics : BottomNavItem(Routes.STATISTICS, Icons.Rounded.Insights, "Прогресс")
    object Weight : BottomNavItem(Routes.WEIGHT, Icons.Rounded.MonitorWeight, "Вес")
    object Settings : BottomNavItem(Routes.SETTINGS, Icons.Rounded.TrackChanges, "Цели")
}

val BottomNavItems = listOf(
    BottomNavItem.Today,
    BottomNavItem.Workouts,
    BottomNavItem.History,
    BottomNavItem.Statistics,
    BottomNavItem.Settings
)
