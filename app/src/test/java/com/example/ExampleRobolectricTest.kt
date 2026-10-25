package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.HardwareNavScreen
import com.example.model.TapeSide
import com.example.playback.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `verify app name resource matches REPLAY 99`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("REPLAY 99", appName)
  }

  @Test
  fun `verify lrc parser parses timestamps and words`() {
    val sampleLrc = """
      [00:01.50]<00:01.50>Acid <00:02.00>Techno
      [00:04.00]Underground Machine
    """.trimIndent()

    val parsed = LrcParser.parse(sampleLrc)
    assertEquals(2, parsed.size)
    assertEquals(1500L, parsed[0].timestampMs)
    assertEquals(4000L, parsed[1].timestampMs)
    assertNotNull(parsed[0].wordTimestamps)
    assertEquals(2, parsed[0].wordTimestamps?.size)
  }

  @Test
  fun `verify hardware navigation screens enum contains 14 destinations`() {
    assertEquals(14, HardwareNavScreen.values().size)
    assertEquals(TapeSide.SIDE_A, TapeSide.valueOf("SIDE_A"))
  }
}
