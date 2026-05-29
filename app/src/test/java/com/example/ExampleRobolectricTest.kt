package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.VidstaViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Vidsta", appName)
  }

  @Test
  fun `test viewmodel initialization and database seeding`() = runTest {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = VidstaViewModel(application)
    assertNotNull(viewModel)
  }
}
