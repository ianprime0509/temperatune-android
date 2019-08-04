package com.ianprime0509.temperatune

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ianprime0509.temperatune.json.moshi
import com.ianprime0509.temperatune.model.Temperament
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private val temperament = moshi.adapter(Temperament::class.java)
        .fromJson(Temperament::class.java.getResource("equalTemperament.json")!!.readText())!!
    private var noteTrack: AudioTrack = createSineWave(temperament.referencePitch)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        ArrayAdapter(this, android.R.layout.simple_spinner_item, temperament.noteNames).let {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            note_spinner.adapter = it
        }
        note_spinner.setSelection(temperament.noteNames.indexOf(temperament.referenceName))

        note_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) =
                note_spinner.setSelection(temperament.noteNames.indexOf(temperament.referenceName))

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, selection: Int, id: Long) {
                noteTrack.stop()
                noteTrack = createSineWave(temperament.getPitch(temperament.noteNames[selection], 4))
            }

        }

        play_button.setOnClickListener {
            when (noteTrack.playState) {
                AudioTrack.PLAYSTATE_PLAYING -> noteTrack.pause()
                else -> noteTrack.play()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createSineWave(pitch: Double): AudioTrack {
        val sampleRate = 41000
        val numFrames = (sampleRate / pitch).roundToInt()

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
}
