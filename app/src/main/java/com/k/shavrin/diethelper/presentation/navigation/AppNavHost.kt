package com.k.shavrin.diethelper.presentation.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.k.shavrin.diethelper.presentation.screen.export.ExportScreen
import com.k.shavrin.diethelper.presentation.screen.history.HistoryDayScreen
import com.k.shavrin.diethelper.presentation.screen.history.HistoryScreen
import com.k.shavrin.diethelper.presentation.screen.product.AddProductScreen
import com.k.shavrin.diethelper.presentation.screen.product.ProductSearchScreen
import com.k.shavrin.diethelper.presentation.screen.settings.SettingsScreen
import com.k.shavrin.diethelper.presentation.screen.stats.ProgressScreen
import com.k.shavrin.diethelper.presentation.screen.today.TodayScreen
import com.k.shavrin.diethelper.presentation.screen.weight.WeightScreen
import com.k.shavrin.diethelper.presentation.screen.workout.WorkoutScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    val entry by navController.currentBackStackEntryAsState()
    val arrival = remember { Animatable(1f) }
    LaunchedEffect(entry?.id) {
        arrival.snapTo(0f)
        arrival.animateTo(1f, tween(220))
    }
    NavHost(
        navController = navController,
        startDestination = Routes.TODAY,
        modifier = Modifier.padding(contentPadding).consumeWindowInsets(contentPadding).graphicsLayer {
            alpha = arrival.value
            translationX = (1f - arrival.value) * 6.dp.toPx()
        }
    ) {
        composable(Routes.WORKOUTS) { WorkoutScreen() }
        composable(Routes.TODAY) {
            TodayScreen(
                onNavigateToProductSearch = { date, mealType ->
                    navController.navigate(Routes.productSearch(date, mealType))
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateToDay = { date ->
                    navController.navigate(Routes.historyDay(date))
                }
            )
        }

        composable(Routes.STATISTICS) {
            ProgressScreen()
        }

        composable(Routes.WEIGHT) {
            WeightScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToExport = { navController.navigate(Routes.EXPORT) }
            )
        }

        composable(Routes.EXPORT) {
            ExportScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.PRODUCT_SEARCH,
            arguments = listOf(
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString(Routes.ARG_DATE).orEmpty()
            val mealType = backStackEntry.arguments?.getString(Routes.ARG_MEAL_TYPE).orEmpty()
            ProductSearchScreen(
                date = date,
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { name ->
                    navController.navigate(Routes.addProduct(name))
                }
            )
        }

        composable(
            route = Routes.ADD_PRODUCT,
            arguments = listOf(
                navArgument(Routes.ARG_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString(Routes.ARG_NAME).orEmpty()
            AddProductScreen(
                initialName = name,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.HISTORY_DAY,
            arguments = listOf(
                navArgument(Routes.ARG_DATE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString(Routes.ARG_DATE).orEmpty()
            HistoryDayScreen(
                date = date,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
