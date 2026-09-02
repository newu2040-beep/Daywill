package com.example.ui.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class AmbientSound(
    val id: String,
    val title: String,
    val icon: String, // Emoji or icon descriptor
    val description: String
)

object SoundPresets {
    val RAINFALL = AmbientSound("rain", "Rainfall", "🌧️", "Soothing gentle rain pattern")
    val OCEAN_WAVES = AmbientSound("ocean", "Ocean Waves", "🌊", "Rhythmic ocean tide surging")
    val FOREST_BIRDS = AmbientSound("forest", "Forest Birds", "🌲", "Calm woodland breeze & birds")
    val ZEN_BOWL = AmbientSound("zen", "Tibetan Bowl", "🧘", "432Hz harmonic meditation bowl")
    val WHITE_NOISE = AmbientSound("white_noise", "White Noise", "📻", "Smooth continuous focus background")
    val COFFEE_SHOP = AmbientSound("coffee", "Coffee Shop", "☕", "Warm ambient cafe atmosphere")

    val ALL_SOUNDS = listOf(RAINFALL, OCEAN_WAVES, FOREST_BIRDS, ZEN_BOWL, WHITE_NOISE, COFFEE_SHOP)
}

class AmbientSoundManager private constructor() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSound = MutableStateFlow<AmbientSound?>(SoundPresets.RAINFALL)
    val currentSound: StateFlow<AmbientSound?> = _currentSound.asStateFlow()

    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    companion object {
        @Volatile
        private var instance: AmbientSoundManager? = null

        fun getInstance(): AmbientSoundManager {
            return instance ?: synchronized(this) {
                instance ?: AmbientSoundManager().also { instance = it }
            }
        }
    }

    fun selectSound(sound: AmbientSound) {
        _currentSound.value = sound
        if (_isPlaying.value) {
            playSound(sound)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        audioTrack?.setVolume(clamped)
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            stopSound()
        } else {
            val sound = _currentSound.value ?: SoundPresets.RAINFALL
            playSound(sound)
        }
    }

    fun playSound(sound: AmbientSound) {
        stopSound()
        _currentSound.value = sound
        _isPlaying.value = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(44100)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        track.setVolume(_volume.value)
        track.play()
        audioTrack = track

        synthJob = scope.launch {
            val buffer = ShortArray(2048)
            var phase = 0.0
            var wavePhase = 0.0
            var birdTimer = 0
            var prevSample = 0.0

            while (isActive && _isPlaying.value) {
                val soundId = _currentSound.value?.id ?: "rain"
                for (i in buffer.indices) {
                    var sample = 0.0

                    when (soundId) {
                        "rain" -> {
                            val white = (Math.random() * 2.0 - 1.0)
                            // Low pass filter for rain soft rumble
                            prevSample = prevSample * 0.85 + white * 0.15
                            sample = prevSample * 0.7
                            // Random droplet clicks
                            if (Math.random() < 0.0008) {
                                sample += (Math.random() * 0.8)
                            }
                        }
                        "ocean" -> {
                            wavePhase += 2.0 * PI / (sampleRate * 6.0) // 6 second cycle
                            val waveMod = (sin(wavePhase) + 1.0) * 0.5
                            val white = (Math.random() * 2.0 - 1.0)
                            prevSample = prevSample * 0.92 + white * 0.08
                            sample = prevSample * (0.2 + 0.7 * waveMod)
                        }
                        "forest" -> {
                            val breeze = (Math.random() * 2.0 - 1.0) * 0.25
                            prevSample = prevSample * 0.90 + breeze * 0.10
                            sample = prevSample

                            birdTimer++
                            if (birdTimer > sampleRate * 3) {
                                if (birdTimer < sampleRate * 3 + 2000) {
                                    val birdFreq = 2200.0 + sin((birdTimer % 2000) * 0.02) * 400.0
                                    phase += 2.0 * PI * birdFreq / sampleRate
                                    sample += sin(phase) * 0.15
                                } else if (birdTimer > sampleRate * 3 + 5000) {
                                    birdTimer = 0
                                }
                            }
                        }
                        "zen" -> {
                            val baseFreq = 432.0
                            phase += 2.0 * PI * baseFreq / sampleRate
                            wavePhase += 2.0 * PI * (baseFreq * 2.01) / sampleRate
                            val bowlSine = sin(phase) * 0.5 + sin(wavePhase) * 0.25
                            sample = bowlSine * 0.6
                        }
                        "white_noise" -> {
                            val white = (Math.random() * 2.0 - 1.0)
                            prevSample = prevSample * 0.70 + white * 0.30
                            sample = prevSample * 0.4
                        }
                        "coffee" -> {
                            val hum = (Math.random() * 2.0 - 1.0)
                            prevSample = prevSample * 0.94 + hum * 0.06
                            sample = prevSample * 0.5
                        }
                        else -> {
                            val white = (Math.random() * 2.0 - 1.0)
                            sample = white * 0.3
                        }
                    }

                    buffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }

                track.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSound() {
        _isPlaying.value = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
