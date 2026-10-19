package com.k.shavrin.diethelper.presentation.screen.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.k.shavrin.diethelper.R
import com.k.shavrin.diethelper.presentation.components.PillTabs
import com.k.shavrin.diethelper.presentation.components.ScreenHeading
import com.k.shavrin.diethelper.presentation.screen.weight.WeightScreen

@Composable
fun ProgressScreen() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeading("ВАША ДИНАМИКА", "Прогресс", "Маленькие шаги складываются в результат",
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp), artwork = R.drawable.art_journey)
        PillTabs(listOf("Питание", "Вес"), tab, { tab = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        if (tab == 0) StatsScreen() else WeightScreen()
    }
}
