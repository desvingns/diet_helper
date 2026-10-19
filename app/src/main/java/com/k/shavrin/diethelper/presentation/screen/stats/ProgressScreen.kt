package com.k.shavrin.diethelper.presentation.screen.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.k.shavrin.diethelper.presentation.screen.weight.WeightScreen

@Composable
fun ProgressScreen() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Питание") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Вес") })
        }
        if (tab == 0) StatsScreen() else WeightScreen()
    }
}
