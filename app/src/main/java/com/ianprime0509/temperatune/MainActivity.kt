package com.ianprime0509.temperatune

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ianprime0509.temperatune.audio.SineWavePlayer
import com.ianprime0509.temperatune.json.moshi
import com.ianprime0509.temperatune.model.Temperament
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private val temperament = moshi.adapter(Temperament::class.java)
        .fromJson(Temperament::class.java.getResource("equalTemperament.json")!!.readText())!!
    private val octaves = listOf(3, 4, 5)
    private val player = SineWavePlayer(temperament.referencePitch)

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
            override fun onNothingSelected(parent: AdapterView<*>?) {
                note_spinner.setSelection(temperament.noteNames.indexOf(temperament.referenceName))
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, selection: Int, id: Long) {
                player.pitch = getSelectedPitch(selection, octave_spinner.selectedItemPosition)
                pitch_label.text = "${player.pitch} Hz"
            }
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_item, octaves).let {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            octave_spinner.adapter = it
        }
        octave_spinner.setSelection(1)

        octave_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                note_spinner.setSelection(1)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, selection: Int, id: Long) {
                player.pitch = getSelectedPitch(note_spinner.selectedItemPosition, selection)
                pitch_label.text = "${player.pitch} Hz"
            }
        }

        play_button.setOnClickListener {
            player.togglePlay()
            play_button.text = when (player.isPlaying) {
                true -> "Pause"
                false -> "Play"
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

    private fun getSelectedPitch(noteIndex: Int, octaveIndex: Int): Double {
        return temperament.getPitch(temperament.noteNames[noteIndex], octaves[octaveIndex])
    }
}
