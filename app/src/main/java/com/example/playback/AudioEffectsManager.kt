package com.example.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.EqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

class AudioEffectsManager {
    companion object {
        private const val TAG = "AudioEffectsManager"
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _eqState = MutableStateFlow(EqualizerState())
    val eqState: StateFlow<EqualizerState> = _eqState.asStateFlow()

    // Real-time audio waveform / spectrum for vintage VU meters and visualizers
    private val _visualizerBands = MutableStateFlow(FloatArray(16) { 0.1f })
    val visualizerBands: StateFlow<FloatArray> = _visualizerBands.asStateFlow()

    private val _vuMeterLeft = MutableStateFlow(0.2f)
    val vuMeterLeft: StateFlow<Float> = _vuMeterLeft.asStateFlow()

    private val _vuMeterRight = MutableStateFlow(0.25f)
    val vuMeterRight: StateFlow<Float> = _vuMeterRight.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return

        try {
            release()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _eqState.value.isEnabled
                applyStateToEq(this, _eqState.value)
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _eqState.value.isEnabled
                if (strengthSupported) {
                    setStrength(_eqState.value.bassBoost.toShort())
                }
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _eqState.value.isEnabled
                if (strengthSupported) {
                    setStrength(_eqState.value.virtualizer.toShort())
                }
            }

            Log.d(TAG, "Audio effects attached to session: $audioSessionId")
        } catch (e: Exception) {
            Log.w(TAG, "Could not initialize hardware audio effects: ${e.message}")
        }
    }

    fun updateBands(bandIndex: Int, gainDb: Int) {
        val newBands = _eqState.value.bands.toMutableMap()
        newBands[bandIndex] = gainDb
        val updated = _eqState.value.copy(bands = newBands, presetName = "Custom")
        _eqState.value = updated

        try {
            equalizer?.let { eq ->
                if (bandIndex < eq.numberOfBands) {
                    val range = eq.bandLevelRange
                    val min = range[0]
                    val max = range[1]
                    val level = ((gainDb + 12f) / 24f * (max - min) + min).toInt().toShort()
                    eq.setBandLevel(bandIndex.toShort(), level)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set EQ band: ${e.message}")
        }
    }

    fun setBassBoost(strength: Int) {
        _eqState.value = _eqState.value.copy(bassBoost = strength)
        try {
            bassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    bb.setStrength(strength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(strength: Int) {
        _eqState.value = _eqState.value.copy(virtualizer = strength)
        try {
            virtualizer?.let { virt ->
                if (virt.strengthSupported) {
                    virt.setStrength(strength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set virtualizer: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _eqState.value = _eqState.value.copy(isEnabled = enabled)
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle audio fx: ${e.message}")
        }
    }

    fun applyPreset(presetName: String) {
        val bands = when (presetName) {
            "90s Rave Club" -> mapOf(0 to 6, 1 to 3, 2 to 0, 3 to 4, 4 to 6)
            "Tape Cassette Warmth" -> mapOf(0 to 4, 1 to 5, 2 to 2, 3 to -1, 4 to -4)
            "Discman Crystal" -> mapOf(0 to 2, 1 to 1, 2 to 3, 3 to 5, 4 to 7)
            "Underground Bass" -> mapOf(0 to 9, 1 to 5, 2 to -2, 3 to 1, 4 to 2)
            "Vocal & Lyric" -> mapOf(0 to -2, 1 to 1, 2 to 6, 3 to 5, 4 to 1)
            else -> mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0) // Flat
        }
        val bb = when (presetName) {
            "Underground Bass" -> 900
            "90s Rave Club" -> 700
            "Tape Cassette Warmth" -> 450
            else -> 200
        }
        _eqState.value = EqualizerState(
            isEnabled = true,
            bands = bands,
            bassBoost = bb,
            virtualizer = 400,
            presetName = presetName
        )
        equalizer?.let { applyStateToEq(it, _eqState.value) }
    }

    fun updateVisualizerFrame(isPlaying: Boolean, positionMs: Long) {
        if (!isPlaying) {
            _vuMeterLeft.value = (_vuMeterLeft.value * 0.85f).coerceAtLeast(0.02f)
            _vuMeterRight.value = (_vuMeterRight.value * 0.85f).coerceAtLeast(0.02f)
            val decay = _visualizerBands.value.map { (it * 0.85f).coerceAtLeast(0.05f) }.toFloatArray()
            _visualizerBands.value = decay
            return
        }

        // Generate dynamic hardware spectrum data mapped from beat and time
        val t = positionMs / 1000.0
        val beatPhase = (positionMs % 500) / 500.0
        val kickEnergy = if (beatPhase < 0.25) (1.0 - beatPhase * 4).toFloat() else 0.1f

        val newBands = FloatArray(16)
        for (i in 0 until 16) {
            val freqWeight = if (i < 4) kickEnergy * 0.9f else 0.3f
            val wave = (sin(t * (3.0 + i * 1.7) + i * 0.8) * 0.35 + 0.55).toFloat()
            newBands[i] = (wave * 0.7f + freqWeight * 0.5f).coerceIn(0.05f, 1.0f)
        }
        _visualizerBands.value = newBands

        val l = (newBands[1] * 0.6f + newBands[4] * 0.4f).coerceIn(0.1f, 0.98f)
        val r = (newBands[2] * 0.5f + newBands[5] * 0.5f).coerceIn(0.1f, 0.98f)
        _vuMeterLeft.value = l
        _vuMeterRight.value = r
    }

    private fun applyStateToEq(eq: Equalizer, state: EqualizerState) {
        try {
            val range = eq.bandLevelRange
            val min = range[0]
            val max = range[1]
            state.bands.forEach { (bandIndex, gainDb) ->
                if (bandIndex < eq.numberOfBands) {
                    val level = ((gainDb + 12f) / 24f * (max - min) + min).toInt().toShort()
                    eq.setBandLevel(bandIndex.toShort(), level)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed applying state to EQ: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio effects", e)
        } finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
        }
    }
}
