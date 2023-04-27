///*
// * Copyright (c) 2010-2021 Belledonne Communications SARL.
// *
// * This file is part of linphone-android
// * (see https://www.linphone.org).
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
package de.suitepad.linbridge.manager
//
//import android.content.Context
//import android.media.AudioManager
//import android.os.Build
//import android.telecom.CallAudioState
//import org.linphone.core.AudioDevice
//import org.linphone.core.Call
//import org.linphone.core.Core
//import org.linphone.core.tools.Log
//import timber.log.Timber
//
//object AudioRouteUtils {
//    fun isHeadsetAudioRouteAvailable(core: Core): Boolean {
//        for (audioDevice in core.audioDevices) {
//            if ((audioDevice.type == AudioDevice.Type.Headset || audioDevice.type == AudioDevice.Type.Headphones) &&
//                audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
//            ) {
//                Log.i("[Audio Route Helper] Found headset/headphones audio device [${audioDevice.deviceName} (${audioDevice.driverName})]")
//                return true
//            }
//        }
//        return false
//    }
//
//    fun isBluetoothAudioRouteAvailable(core: Core): Boolean {
//        for (audioDevice in core.audioDevices) {
//            if (audioDevice.type == AudioDevice.Type.Bluetooth &&
//                audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
//            ) {
//                Log.i("[Audio Route Helper] Found bluetooth audio device [${audioDevice.deviceName} (${audioDevice.driverName})]")
//                return true
//            }
//        }
//        return false
//    }
//
//    fun routeAudioToHeadset(call: Call? = null, skipTelecom: Boolean = false, core: Core) {
//        routeAudioTo(call, arrayListOf(AudioDevice.Type.Headphones, AudioDevice.Type.Headset), skipTelecom, core = core)
//    }
//
//    fun routeAudioToBluetooth(call: Call? = null, skipTelecom: Boolean = false, core: Core) {
//        routeAudioTo(call, arrayListOf(AudioDevice.Type.Bluetooth), skipTelecom, core = core)
//    }
//
//    private fun routeAudioTo(
//        call: Call?,
//        types: List<AudioDevice.Type>,
//        skipTelecom: Boolean = false,
//        core: Core,
//    ) {
//        val route = when (types.first()) {
//            AudioDevice.Type.Earpiece -> CallAudioState.ROUTE_EARPIECE
//            AudioDevice.Type.Speaker -> CallAudioState.ROUTE_SPEAKER
//            AudioDevice.Type.Headphones, AudioDevice.Type.Headset -> CallAudioState.ROUTE_WIRED_HEADSET
//            AudioDevice.Type.Bluetooth, AudioDevice.Type.BluetoothA2DP -> CallAudioState.ROUTE_BLUETOOTH
//            else -> CallAudioState.ROUTE_WIRED_OR_EARPIECE
//        }
//
//        applyAudioRouteChange(call, types, core = core)
//        changeCaptureDeviceToMatchAudioRoute(call, types, core = core)
//    }
//
//    private fun applyAudioRouteChange(
//        call: Call?,
//        types: List<AudioDevice.Type>,
//        output: Boolean = true,
//        core: Core,
//    ) {
//        val currentCall = if (core.callsNb > 0) {
//            call ?: core.currentCall ?: core.calls[0]
//        } else {
//            Timber.i("[Audio Route Helper] No call found, setting audio route on Core")
//            Log.w("[Audio Route Helper] No call found, setting audio route on Core")
//            null
//        }
//        val conference = core.conference
//        val capability = if (output)
//            AudioDevice.Capabilities.CapabilityPlay
//        else
//            AudioDevice.Capabilities.CapabilityRecord
//        val preferredDriver = if (output) {
//            core.defaultOutputAudioDevice?.driverName
//        } else {
//            core.defaultInputAudioDevice?.driverName
//        }
//
//        val extendedAudioDevices = core.extendedAudioDevices
//        Log.i("[Audio Route Helper] Looking for an ${if (output) "output" else "input"} audio device with capability [$capability], driver name [$preferredDriver] and type [$types] in extended audio devices list (size ${extendedAudioDevices.size})")
//        val foundAudioDevice = extendedAudioDevices.find {
//            it.driverName == preferredDriver && types.contains(it.type) && it.hasCapability(capability)
//        }
//        val audioDevice = if (foundAudioDevice == null) {
//            Log.w("[Audio Route Helper] Failed to find an audio device with capability [$capability], driver name [$preferredDriver] and type [$types]")
//            extendedAudioDevices.find {
//                types.contains(it.type) && it.hasCapability(capability)
//            }
//        } else {
//            foundAudioDevice
//        }
//
//        if (audioDevice == null) {
//            Log.e("[Audio Route Helper] Couldn't find audio device with capability [$capability] and type [$types]")
//            for (device in extendedAudioDevices) {
//                // TODO: switch to debug?
//                Log.i("[Audio Route Helper] Extended audio device: [${device.deviceName} (${device.driverName}) ${device.type} / ${device.capabilities}]")
//            }
//            return
//        }
//        if (conference != null && conference.isIn) {
//            Log.i("[Audio Route Helper] Found [${audioDevice.type}] ${if (output) "playback" else "recorder"} audio device [${audioDevice.deviceName} (${audioDevice.driverName})], routing conference audio to it")
//            if (output) conference.outputAudioDevice = audioDevice
//            else conference.inputAudioDevice = audioDevice
//        } else if (currentCall != null) {
//            Log.i("[Audio Route Helper] Found [${audioDevice.type}] ${if (output) "playback" else "recorder"} audio device [${audioDevice.deviceName} (${audioDevice.driverName})], routing call audio to it")
//            if (output) currentCall.outputAudioDevice = audioDevice
//            else currentCall.inputAudioDevice = audioDevice
//        } else {
//            Log.i("[Audio Route Helper] Found [${audioDevice.type}] ${if (output) "playback" else "recorder"} audio device [${audioDevice.deviceName} (${audioDevice.driverName})], changing core default audio device")
//            if (output) core.outputAudioDevice = audioDevice
//            else core.inputAudioDevice = audioDevice
//        }
//    }
//
//    private fun changeCaptureDeviceToMatchAudioRoute(call: Call?, types: List<AudioDevice.Type>, core: Core) {
//        when (types.first()) {
//            AudioDevice.Type.Bluetooth -> {
//                if (isBluetoothAudioRecorderAvailable(core = core)) {
//                    Log.i("[Audio Route Helper] Bluetooth device is able to record audio, also change input audio device")
//                    applyAudioRouteChange(call, arrayListOf(AudioDevice.Type.Bluetooth), false, core = core)
//                }
//            }
//            AudioDevice.Type.Headset, AudioDevice.Type.Headphones -> {
//                if (isHeadsetAudioRecorderAvailable(core)) {
//                    Log.i("[Audio Route Helper] Headphones/Headset device is able to record audio, also change input audio device")
//                    applyAudioRouteChange(
//                        call,
//                        (arrayListOf(AudioDevice.Type.Headphones, AudioDevice.Type.Headset)),
//                        false,
//                        core = core
//                    )
//                }
//            }
//            AudioDevice.Type.Earpiece, AudioDevice.Type.Speaker -> {
//                Log.i("[Audio Route Helper] Audio route requested to Earpiece or Speaker, setting input to Microphone")
//                applyAudioRouteChange(call, (arrayListOf(AudioDevice.Type.Microphone)), false, core = core)
//            }
//            else -> {
//                Log.w("[Audio Route Helper] Unexpected audio device type: ${types.first()}")
//            }
//        }
//    }
//
//    private fun isBluetoothAudioRecorderAvailable(core: Core): Boolean {
//        for (audioDevice in core.audioDevices) {
//            if (audioDevice.type == AudioDevice.Type.Bluetooth &&
//                audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityRecord)
//            ) {
//                Log.i("[Audio Route Helper] Found bluetooth audio recorder [${audioDevice.deviceName} (${audioDevice.driverName})]")
//                return true
//            }
//        }
//        return false
//    }
//
//    private fun isHeadsetAudioRecorderAvailable(core: Core): Boolean {
//        for (audioDevice in core.audioDevices) {
//            if ((audioDevice.type == AudioDevice.Type.Headset || audioDevice.type == AudioDevice.Type.Headphones) &&
//                audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityRecord)
//            ) {
//                Log.i("[Audio Route Helper] Found headset/headphones audio recorder [${audioDevice.deviceName} (${audioDevice.driverName})]")
//                return true
//            }
//        }
//        return false
//    }
//}
//
//// backup
//fun routeAudio(core: Core, context: Context, call: Call) {
//    if (core.callsNb == 1) {
//        if (AudioRouteUtils.isHeadsetAudioRouteAvailable(core)) {
//            Timber.i("HOLA: Headset available")
//            AudioRouteUtils.routeAudioToHeadset(call, core = core)
//        } else if (AudioRouteUtils.isBluetoothAudioRouteAvailable(core)) {
//            Timber.i("HOLA: Bluetooth audio route is available")
//            AudioRouteUtils.routeAudioToBluetooth(call, core = core)
//            val audioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
//            audioManager.mode = AudioManager.MODE_IN_CALL
//            Timber.i("HOLA: mode = ${audioManager.mode} |")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                audioManager.activePlaybackConfigurations.forEach {
//                    Timber.i("HOLA: attribute::: ${it.audioAttributes}")
//                }
//            }
//        } else {
//            Timber.i("HOLA: Headset is not available")
//        }
//    }
//}