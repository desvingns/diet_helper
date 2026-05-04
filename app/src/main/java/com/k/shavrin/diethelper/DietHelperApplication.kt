package com.k.shavrin.diethelper

import android.app.Application
import com.k.shavrin.diethelper.data.local.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DietHelperApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            databaseSeeder.seedIfNeeded()
        }
    }
}
