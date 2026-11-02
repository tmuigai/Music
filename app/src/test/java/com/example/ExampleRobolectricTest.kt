package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.HardwareNavScreen
import com.example.model.TapeSide
import com.example.model.audioFormat
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
  fun `verify hardware navigation screens enum contains 21 destinations`() {
    assertEquals(21, HardwareNavScreen.values().size)
    assertEquals("17", HardwareNavScreen.VINYL.code)
    assertEquals("18", HardwareNavScreen.FOUR_TRACK.code)
    assertEquals("19", HardwareNavScreen.ROTARY_PHONE.code)
    assertEquals("20", HardwareNavScreen.PARTY_JUKEBOX.code)
    assertEquals("21", HardwareNavScreen.BUILD_A_MACHINE.code)
    assertEquals(TapeSide.SIDE_A, TapeSide.valueOf("SIDE_A"))
  }

  @Test
  fun `verify listening environments and smart folders exist`() {
    assertEquals(6, com.example.model.ListeningEnvironment.values().size)
    assertEquals(7, com.example.model.SmartFolderType.values().size)
    val env = com.example.model.ListeningEnvironment.PARTY
    assertEquals(160, env.ampBoostPercent)
    assertEquals("90s Rave Club", env.eqPreset)
  }

  @Test
  fun `verify volume state supports 300 percent amplification with limiter and clipping`() {
    val vol = com.example.model.MasterVolumeState(
      amplificationPercent = 250,
      isClipping = true,
      limiterEnabled = true
    )
    assertEquals(250, vol.amplificationPercent)
    assertTrue(vol.isClipping)
    assertTrue(vol.limiterEnabled)
  }

  @Test
  fun `verify five primary navigation categories map all hardware destinations`() {
    assertEquals(5, com.example.model.HardwareNavCategory.values().size)
    assertEquals(com.example.model.HardwareNavCategory.HOME, HardwareNavScreen.CONTROL_DECK.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.CASSETTE.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.DISCMAN.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.DJ_DECK.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.MINIDISC.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.BOOMBOX.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.VINYL.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.FOUR_TRACK.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.ROTARY_PHONE.category)
    assertEquals(com.example.model.HardwareNavCategory.MACHINES, HardwareNavScreen.PARTY_JUKEBOX.category)
    assertEquals(com.example.model.HardwareNavCategory.LIBRARY, HardwareNavScreen.MIXTAPE_LIBRARY.category)
    assertEquals(com.example.model.HardwareNavCategory.SOUND, HardwareNavScreen.SOUND_LAB.category)
    assertEquals(com.example.model.HardwareNavCategory.SOUND, HardwareNavScreen.LYRICS_ROOM.category)
    assertEquals(com.example.model.HardwareNavCategory.WORKSHOP, HardwareNavScreen.WORKSHOP.category)
    assertEquals(com.example.model.HardwareNavCategory.WORKSHOP, HardwareNavScreen.VIRTUAL_ROOM.category)
    assertEquals(com.example.model.HardwareNavCategory.WORKSHOP, HardwareNavScreen.BUILD_A_MACHINE.category)
  }

  @Test
  fun `verify audio format detection identifies all supported hardware formats`() {
    assertEquals("MP3", com.example.model.detectAudioFormat("/music/track.mp3"))
    assertEquals("FLAC", com.example.model.detectAudioFormat("/music/track.flac"))
    assertEquals("WAV", com.example.model.detectAudioFormat("/music/track.wav"))
    assertEquals("AAC", com.example.model.detectAudioFormat("/music/track.aac"))
    assertEquals("M4A", com.example.model.detectAudioFormat("/music/track.m4a"))
    assertEquals("OGG", com.example.model.detectAudioFormat("/music/track.ogg"))
    assertEquals("OPUS", com.example.model.detectAudioFormat("/music/track.opus"))
  }

  @Test
  fun `verify multi-genre parsing extracts individual genres from delimited strings`() {
    val genres = com.example.model.parseGenres("Acid Techno / Breakbeat, Industrial")
    assertEquals(3, genres.size)
    assertTrue(genres.contains("Acid Techno"))
    assertTrue(genres.contains("Breakbeat"))
    assertTrue(genres.contains("Industrial"))

    val fallback = com.example.model.parseGenres("Unknown")
    assertEquals(1, fallback.size)
    assertEquals("Underground", fallback[0])
  }

  @Test
  fun `verify curated categories enum contains all 8 required crates`() {
    val categories = com.example.model.CuratedCategory.values()
    assertEquals(8, categories.size)
    assertTrue(categories.any { it == com.example.model.CuratedCategory.RECENTLY_ADDED })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.RECENTLY_PLAYED })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.MOST_PLAYED })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.FAVOURITES })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.FORGOTTEN_TRACKS })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.LOSSLESS_AUDIO })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.EXTENDED_CUTS })
    assertTrue(categories.any { it == com.example.model.CuratedCategory.INTERLUDES })
  }

  @Test
  fun `verify scan progress defaults to idle ready state`() {
    val progress = com.example.model.ScanProgress()
    org.junit.Assert.assertFalse(progress.isScanning)
    assertEquals("SCANNER READY", progress.statusMessage)
    assertEquals(0, progress.totalFound)
    assertEquals(0, progress.newlyAdded)
  }

  @Test
  fun `verify library health report calculations and duplicate group structures`() {
    val master = com.example.model.Track(
      id = 101L,
      title = "Rez",
      artist = "Underworld",
      album = "Dubnobass",
      durationMs = 360000L,
      contentUri = "content://media/101",
      path = "/music/rez.flac"
    )
    val dup = com.example.model.Track(
      id = 102L,
      title = "Rez",
      artist = "Underworld",
      album = "Unknown",
      durationMs = 360000L,
      contentUri = "content://media/102",
      path = "/music/rez.mp3"
    )

    val group = com.example.model.DuplicateGroup(
      id = "dup_101",
      title = master.title,
      artist = master.artist,
      reason = "Identical Title & Artist (2 copies)",
      recommendedKeep = master,
      duplicateTracks = listOf(dup)
    )

    assertEquals("Rez", group.title)
    assertEquals("FLAC", group.recommendedKeep.audioFormat)
    assertEquals("MP3", group.duplicateTracks[0].audioFormat)

    val report = com.example.model.LibraryHealthReport(
      totalTracks = 2,
      duplicateGroups = listOf(group),
      healthScore = 92
    )
    assertEquals(92, report.healthScore)
    assertEquals(1, report.duplicateGroups.size)
  }
}
