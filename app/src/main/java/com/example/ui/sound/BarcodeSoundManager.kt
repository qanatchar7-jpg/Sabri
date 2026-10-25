package com.example.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object BarcodeSoundManager {
    var isSoundEnabled: Boolean = true

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays the authentic, crisp retail barcode scanner beep (~2400 Hz for 120ms).
     */
    fun playScanBeep() {
        if (!isSoundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (_: Exception) {
            playSynthesizedBeep(frequency = 2400.0, durationMs = 120)
        }
    }

    /**
     * Plays error sound when an unknown barcode is encountered.
     */
    fun playErrorBeep() {
        if (!isSoundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (_: Exception) {
            playSynthesizedBeep(frequency = 600.0, durationMs = 250)
        }
    }

    /**
     * Plays a pleasant chime upon successful checkout/payment.
     */
    fun playSuccessPaymentChime() {
        if (!isSoundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
        } catch (_: Exception) {
            playSynthesizedBeep(frequency = 1800.0, durationMs = 200)
        }
    }

    /**
     * High-reliability synthetic PCM beep fallback using AudioTrack.
     */
    private fun playSynthesizedBeep(frequency: Double, durationMs: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                    // Apply fade-out envelope to avoid audio clicks
                    val envelope = if (i > numSamples - 1000) (numSamples - i).toDouble() / 1000.0 else 1.0
                    buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
            } catch (_: Exception) {
                // Ignore audio errors on emulators with disabled audio device
            }
        }
    }
}
