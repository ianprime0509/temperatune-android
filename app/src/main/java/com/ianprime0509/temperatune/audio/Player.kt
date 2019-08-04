package com.ianprime0509.temperatune.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.roundToInt
import kotlin.math.sin

class SineWavePlayer(pitch: Double) {
    var pitch = pitch
        set(value) {
            if (value != field) {
                val playing = isPlaying
                track.stop()
                track = createAudioTrack(value)
                if (playing) track.play()
            }
            field = value
        }

    val isPlaying get() = track.playState == AudioTrack.PLAYSTATE_PLAYING

    private var track = createAudioTrack(pitch)

    fun togglePlay() {
        when (isPlaying) {
            true -> track.pause()
            false -> track.play()
        }
    }

    private fun createAudioTrack(pitch: Double): AudioTrack {
        val sampleRate = 41000
        val numFrames = chooseBufferSize(pitch, sampleRate)

        @Suppress("DEPRECATION") val track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            numFrames * 2,
            AudioTrack.MODE_STATIC
        )
        track.setLoopPoints(0, numFrames, -1)

        val data = ShortArray(numFrames)
        val delta = 2 * Math.PI * pitch / sampleRate
        var angle = 0.0
        for (i in data.indices) {
            data[i] = (sin(angle) * Short.MAX_VALUE).toShort()
            angle += delta
        }
        track.write(data, 0, data.size)

        return track
    }

    private fun chooseBufferSize(pitch: Double, sampleRate: Int): Int {
        val period = sampleRate / pitch
        return (1..10).map { it * period }
            .minBy { it % 1 }!!
            .roundToInt()
    }
}
