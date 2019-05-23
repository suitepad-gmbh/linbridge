package de.suitepad.linbridge

import android.os.Bundle
import de.suitepad.linbridge.api.core.AudioCodec

const val EXTRA_SIP_SERVER = "SERVER"
const val EXTRA_SIP_USERNAME = "USERNAME"
const val EXTRA_SIP_PASSWORD = "PASSWORD"
const val EXTRA_SIP_PORT = "PORT"
const val EXTRA_SIP_PROXY = "PROXY"

const val EXTRA_MICROPHONE_GAIN = "MICROPHONE_GAIN"
const val EXTRA_SPEAKER_GAIN = "SPEAKER_GAIN"
const val EXTRA_AEC_ENABLED = "AEC_ENABLED"
const val EXTRA_EL_ENABLED = "EL_ENABLED"
const val EXTRA_EL_MIC_REDUCTION = "EL_MICROPHONE_REDUCTION"
const val EXTRA_EL_SPEAKER_THRESHOLD = "EL_SPEAKER_THRESHOLD"
const val EXTRA_EL_SUSTAIN = "EL_SUSTAIN"
const val EXTRA_EL_DOUBLETALK_THRESHOLD = "EL_DOUBLETALK_THRESHOLD"
const val EXTRA_LIST_CODEC_ENABLED = "CODECS"

const val EXTRA_DTMF_CHAR = "NUMBER"

const val EXTRA_DESTINATION = "DESTINATION"

enum class IntentAction(val routine: (bridge: IBridgeService, bundle: Bundle?) -> Unit) {
    START({ bridge, _ ->
        bridge.startService()
    }),
    STOP({ bridge, _ ->
        bridge.stopService()
    }),
    AUTHENTICATE({ bridge, bundle ->
        bridge.authenticate(de.suitepad.linbridge.api.core.Credentials(
                bundle?.getString(EXTRA_SIP_SERVER),
                bundle?.getInt(EXTRA_SIP_PORT, 5060) ?: 5060,
                bundle?.getString(EXTRA_SIP_USERNAME),
                bundle?.getString(EXTRA_SIP_PASSWORD),
                bundle?.getString(EXTRA_SIP_PROXY)
        ))
    }),
    CALL({ bridge, bundle ->
        bridge.call(bundle?.getString(EXTRA_DESTINATION))
    }),
    ANSWER({ bridge, _ ->
        bridge.answerCall()
    }),
    REJECT({ bridge, _ ->
        bridge.rejectCall()
    }),
    CONFIG({ bridge, bundle ->
        bridge.updateConfig(de.suitepad.linbridge.api.AudioConfiguration().apply {
            echoCancellation = bundle?.getBoolean(EXTRA_AEC_ENABLED, false) ?: false
            echoLimiter = bundle?.getBoolean(EXTRA_EL_ENABLED, false) ?: false
            echoLimiterDoubleTalkDetection = bundle?.getFloat(EXTRA_EL_DOUBLETALK_THRESHOLD, 1.0f) ?: 1.0f
            echoLimiterMicrophoneDecrease = bundle?.getInt(EXTRA_EL_MIC_REDUCTION, 0) ?: 0
            echoLimiterSpeakerThreshold = bundle?.getFloat(EXTRA_EL_SPEAKER_THRESHOLD, 1.0f) ?: 1.0f
            echoLimiterSustain = bundle?.getInt(EXTRA_EL_SUSTAIN, 0) ?: 0
            enabledCodecs = bundle?.getStringArray(EXTRA_LIST_CODEC_ENABLED)?.map { AudioCodec.valueOf(it) }?.toTypedArray()
            microphoneGain = bundle?.getInt(EXTRA_MICROPHONE_GAIN, 0) ?: 0
            speakerGain = bundle?.getInt(EXTRA_SPEAKER_GAIN, 0) ?: 0
        })
    }),
    DTMFPLAY({ bridge, bundle ->
        bridge.sendDtmf(bundle?.getChar(EXTRA_DTMF_CHAR, '0') ?: '0')
    }),
    DTMFSTOP({ bridge, bundle ->
        bridge.stopDtmf()
    })
}