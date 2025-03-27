package de.suitepad.linbridge.helper

import android.content.Context
import android.media.AudioManager
import android.os.Build
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.manager.getEnabledCodecs
import org.linphone.core.Core
import timber.log.Timber

object AudioOptimizer {
    fun tuneAudioStack(core: Core, context: Context) {
        core.audioJittcomp = 60
        core.isAudioAdaptiveJittcompEnabled = true
        core.isEchoLimiterEnabled = true

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.isMicrophoneMute = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(
                { /* no-op */ },
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }

        Timber.d("Audio stack tuned for communication")
    }
    fun optimizeForLoudAndCleanAudio(core: Core, micBoostDb: Float = 4.0f, speakerBoostDb: Float = 4.0f) {
        core.micGainDb = micBoostDb
        core.playbackGainDb = speakerBoostDb
        core.isEchoCancellationEnabled = true
        core.isEchoLimiterEnabled = true

        core.config.apply {
            setInt("sound", "agc", 1)
            setInt("sound", "noise_suppressor", 1)
            setInt("sound", "noisegate", 1)

            setFloat("sound", "ng_thres", 0.05f)
            setFloat("sound", "ng_floorgain", 0.02f)

            setFloat("sound", "el_thres", 0.12f)
            setInt("sound", "el_sustain", 200)
            setInt("sound", "el_force", 5)
            setFloat("sound", "el_transmit_threshold", 0.1f)

            setInt("sound", "ec_tail_len", 300)
        }

        Timber.d("Optimized for loud & clean audio: mic +${micBoostDb}dB, speaker +${speakerBoostDb}dB")
    }

    fun Core.getConfiguration(): AudioConfiguration {
        return AudioConfiguration().also {
            it.microphoneGain = micGainDb.toInt()
            it.speakerGain = playbackGainDb.toInt()
            it.echoCancellation = isEchoCancellationEnabled
            it.echoLimiter = isEchoLimiterEnabled
            it.echoLimiterSustain = config.getInt("sound", "el_sustain", 0)
            it.echoLimiterSpeakerThreshold = config.getFloat("sound", "el_thres", 0f)
            it.echoLimiterMicrophoneDecrease = config.getInt("sound", "el_force", 0)
            it.echoLimiterDoubleTalkDetection = config.getFloat("sound", "el_transmit_threshold", 0f)
            it.enabledCodecs = getEnabledCodecs()
        }
    }
}