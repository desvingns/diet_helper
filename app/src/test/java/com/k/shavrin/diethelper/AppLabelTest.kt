package com.k.shavrin.diethelper

import android.content.ComponentName
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class AppLabelTest {

    @Test
    fun `application and launcher activity use DietHelper label`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager

        assertEquals("DietHelper", context.getString(R.string.app_name))
        assertEquals("DietHelper", packageManager.getApplicationLabel(context.applicationInfo).toString())

        val launcherActivity = packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0
        )
        assertEquals("DietHelper", launcherActivity.loadLabel(packageManager).toString())
    }
}
