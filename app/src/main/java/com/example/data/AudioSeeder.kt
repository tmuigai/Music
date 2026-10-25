package com.example.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object AudioSeeder {
    private const val TAG = "AudioSeeder"

    data class SeedTrackInfo(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val genre: String,
        val bpm: Double,
        val style: SynthStyle,
        val lyrics: String
    )

    enum class SynthStyle {
        ACID_TECHNO, JUNGLE_DNB, BOOM_BAP, CYBER_RAVE, LOFI_DUB
    }

    val SEED_TRACKS = listOf(
        SeedTrackInfo(
            id = 9901L,
            title = "Acid Machine 1999",
            artist = "The 303 Syndicate",
            album = "Underground Frequency EP",
            genre = "Acid Techno",
            bpm = 138.0,
            style = SynthStyle.ACID_TECHNO,
            lyrics = """
                [00:00.00]<00:00.00>Power <00:00.80>up <00:01.20>the <00:01.60>analog <00:02.20>circuits
                [00:03.00]<00:03.00>Feel <00:03.60>the <00:04.10>thirty-three <00:04.90>voltage
                [00:06.00]<00:06.00>Nineteen <00:06.60>ninety <00:07.10>nine <00:07.60>warehouse <00:08.50>energy
                [00:09.50]<00:09.50>Matte <00:10.00>black <00:10.50>deck <00:11.00>spinning <00:11.80>free
                [00:13.00]<00:13.00>Acid <00:13.60>resonance <00:14.50>cutting <00:15.20>through
                [00:16.00]<00:16.00>Your <00:16.50>machine <00:17.20>your <00:17.80>rules
            """.trimIndent()
        ),
        SeedTrackInfo(
            id = 9902L,
            title = "Concrete Jungle '95",
            artist = "Amen Protocol",
            album = "Sub-Bass Pressure",
            genre = "Drum & Bass",
            bpm = 165.0,
            style = SynthStyle.JUNGLE_DNB,
            lyrics = """
                [00:00.00]<00:00.00>One <00:00.60>hundred <00:01.10>and <00:01.50>sixty-five <00:02.30>BPM
                [00:03.00]<00:03.00>Rolling <00:03.80>breaks <00:04.40>across <00:05.10>the <00:05.50>room
                [00:06.20]<00:06.20>Low <00:06.80>frequency <00:07.70>sub-bass <00:08.50>boom
                [00:09.20]<00:09.20>Cassette <00:10.00>reels <00:10.60>turning <00:11.30>fast
                [00:12.50]<00:12.50>Underground <00:13.40>sounds <00:14.00>built <00:14.60>to <00:15.00>last
            """.trimIndent()
        ),
        SeedTrackInfo(
            id = 9903L,
            title = "Brooklyn Boom Bap",
            artist = "Crate Diggers '94",
            album = "Dusty Vinyl Sessions",
            genre = "90s Hip Hop",
            bpm = 92.0,
            style = SynthStyle.BOOM_BAP,
            lyrics = """
                [00:00.00]<00:00.00>Dust <00:00.50>on <00:01.00>the <00:01.30>groove <00:01.80>needle <00:02.40>drops
                [00:03.00]<00:03.00>Boom <00:03.60>bap <00:04.20>snare <00:04.80>never <00:05.40>stops
                [00:06.20]<00:06.20>Walkman <00:07.00>clipped <00:07.60>to <00:08.10>the <00:08.50>belt
                [00:09.50]<00:09.50>Warmest <00:10.20>tape <00:10.90>sound <00:11.50>ever <00:12.10>felt
                [00:13.00]<00:13.00>Side <00:13.50>A <00:13.90>running <00:14.70>till <00:15.20>the <00:15.80>end
            """.trimIndent()
        ),
        SeedTrackInfo(
            id = 9904L,
            title = "Cyber Rave Odyssey",
            artist = "Future Shock",
            album = "Laserdisc Transmission",
            genre = "Eurodance",
            bpm = 140.0,
            style = SynthStyle.CYBER_RAVE,
            lyrics = """
                [00:00.00]<00:00.00>Discman <00:00.70>spinning <00:01.40>in <00:01.90>the <00:02.40>dark
                [00:03.10]<00:03.10>Anti-skip <00:04.00>active <00:04.70>forty <00:05.40>seconds
                [00:06.20]<00:06.20>Laser <00:06.90>reads <00:07.40>the <00:07.90>spiral <00:08.70>track
                [00:09.50]<00:09.50>Digital <00:10.30>sound <00:11.00>no <00:11.50>turning <00:12.30>back
            """.trimIndent()
        ),
        SeedTrackInfo(
            id = 9905L,
            title = "Midnight Cassette Dub",
            artist = "Tape Echo Collective",
            album = "Chrome Type II",
            genre = "Lo-Fi Dub",
            bpm = 85.0,
            style = SynthStyle.LOFI_DUB,
            lyrics = """
                [00:00.00]<00:00.00>Slow <00:00.90>motion <00:01.80>magnetic <00:02.80>tape
                [00:03.80]<00:03.80>Analog <00:04.70>hiss <00:05.50>pure <00:06.30>escape
                [00:07.20]<00:07.20>VU <00:07.90>meters <00:08.80>dancing <00:09.70>green
                [00:10.50]<00:10.50>Best <00:11.20>sound <00:12.00>in <00:12.60>the <00:13.20>machine
            """.trimIndent()
        )
    )

    fun seedTracksIfNecessary(context: Context): List<TrackEntity> {
        val audioDir = File(context.filesDir, "replay_audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val entities = mutableListOf<TrackEntity>()

        for (info in SEED_TRACKS) {
            val audioFile = File(audioDir, "track_${info.id}.wav")
            if (!audioFile.exists() || audioFile.length() < 1000) {
                try {
                    generateWavFile(audioFile, info.bpm, info.style, durationSeconds = 30)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to generate audio for ${info.title}", e)
                }
            }

            entities.add(
                TrackEntity(
                    id = info.id,
                    title = info.title,
                    artist = info.artist,
                    album = info.album,
                    durationMs = 30000L,
                    contentUri = audioFile.absolutePath,
                    albumArtUri = null,
                    path = audioFile.absolutePath,
                    genre = info.genre,
                    year = 1999,
                    dateAdded = System.currentTimeMillis() - (info.id * 1000),
                    replayCount = (info.id % 7).toInt() + 1,
                    lastPlayedMs = System.currentTimeMillis() - (info.id * 50000),
                    isFavorite = info.id == 9901L || info.id == 9903L,
                    customLyrics = info.lyrics
                )
            )
        }

        return entities
    }

    private fun generateWavFile(
        file: File,
        bpm: Double,
        style: SynthStyle,
        durationSeconds: Int = 30
    ) {
        val sampleRate = 22050
        val totalSamples = sampleRate * durationSeconds
        val bytesPerSample = 2 // 16-bit
        val dataSize = totalSamples * bytesPerSample

        val buffer = ByteBuffer.allocate(44 + dataSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))

        // fmt chunk
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16) // Subchunk1Size
        buffer.putShort(1.toShort()) // AudioFormat (1 = PCM)
        buffer.putShort(1.toShort()) // NumChannels (1 = Mono)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * bytesPerSample) // ByteRate
        buffer.putShort(bytesPerSample.toShort()) // BlockAlign
        buffer.putShort(16.toShort()) // BitsPerSample

        // data chunk
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)

        val beatDurationSamples = (60.0 / bpm * sampleRate).toInt()
        val sixteenthNoteSamples = beatDurationSamples / 4

        val rootFreq = when (style) {
            SynthStyle.ACID_TECHNO -> 65.41 // C2
            SynthStyle.JUNGLE_DNB -> 55.00 // A1
            SynthStyle.BOOM_BAP -> 73.42 // D2
            SynthStyle.CYBER_RAVE -> 65.41 // C2
            SynthStyle.LOFI_DUB -> 48.99 // G1
        }

        // Bass scale notes
        val scaleIntervals = intArrayOf(0, 3, 7, 10, 12, 10, 7, 3)

        for (i in 0 until totalSamples) {
            val step = (i / sixteenthNoteSamples) % 16
            val stepSample = i % sixteenthNoteSamples
            val beatSample = i % beatDurationSamples

            var sampleVal = 0.0

            // 1. Kick drum (every quarter note beat or 90s breakbeat pattern)
            val isKick = when (style) {
                SynthStyle.ACID_TECHNO, SynthStyle.CYBER_RAVE -> beatSample < (sampleRate * 0.18)
                SynthStyle.JUNGLE_DNB -> (step == 0 || step == 10) && stepSample < (sampleRate * 0.12)
                SynthStyle.BOOM_BAP -> (step == 0 || step == 6 || step == 10) && stepSample < (sampleRate * 0.15)
                SynthStyle.LOFI_DUB -> (step == 0 || step == 8) && stepSample < (sampleRate * 0.22)
            }
            if (isKick) {
                val decay = 1.0 - (stepSample.toDouble() / (sampleRate * 0.18)).coerceIn(0.0, 1.0)
                val kickPitch = 50.0 + 110.0 * decay * decay
                val phase = 2.0 * PI * kickPitch * (stepSample.toDouble() / sampleRate)
                sampleVal += sin(phase) * decay * 0.55
            }

            // 2. Snare / Clack (backbeats on steps 4 and 12)
            val isSnare = (step == 4 || step == 12) && stepSample < (sampleRate * 0.14)
            if (isSnare) {
                val decay = 1.0 - (stepSample.toDouble() / (sampleRate * 0.14)).coerceIn(0.0, 1.0)
                val noise = ((i * 1103515245 + 12345) and 0x7FFFFFFF) / 1073741824.0 - 1.0
                val tone = sin(2.0 * PI * 180.0 * (stepSample.toDouble() / sampleRate))
                sampleVal += (noise * 0.6 + tone * 0.4) * decay * 0.35
            }

            // 3. Hi-Hat / Shaker (16th notes)
            val hatDecay = 1.0 - (stepSample.toDouble() / (sampleRate * 0.04)).coerceIn(0.0, 1.0)
            if (hatDecay > 0) {
                val noise = ((i * 314159265 + 13579) and 0x7FFFFFFF) / 1073741824.0 - 1.0
                sampleVal += noise * hatDecay * 0.12
            }

            // 4. Synth / Bassline
            val noteIndex = scaleIntervals[step % scaleIntervals.size]
            val freq = rootFreq * Math.pow(2.0, noteIndex / 12.0)
            val bassEnv = 1.0 - (stepSample.toDouble() / sixteenthNoteSamples).coerceIn(0.0, 1.0)
            val t = i.toDouble() / sampleRate
            
            // Rich multi-harmonic tone
            val sawApprox = sin(2.0 * PI * freq * t) +
                    0.5 * sin(2.0 * PI * freq * 2.0 * t) +
                    0.25 * sin(2.0 * PI * freq * 3.0 * t)
            
            sampleVal += sawApprox * bassEnv * 0.28

            // Clamp and convert to 16-bit PCM
            val clamped = sampleVal.coerceIn(-1.0, 1.0)
            val pcm = (clamped * 32767.0).toInt().toShort()
            buffer.putShort(pcm)
        }

        FileOutputStream(file).use { fos ->
            fos.write(buffer.array())
        }
    }
}
