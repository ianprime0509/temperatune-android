package com.ianprime0509.temperatune.model

import com.ianprime0509.temperatune.json.moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class TemperamentTest {
    @Test
    fun testNoteNames_withEqualTemperament_returnsNoteNamesInOrder() {
        val input = Temperament::class.java.getResource("equalTemperament.json")!!.readText()
        val temperament = moshi.adapter(Temperament::class.java).fromJson(input)!!

        assertEquals(
            listOf(
                "C",
                "C{sharp}",
                "D",
                "E{flat}",
                "E",
                "F",
                "F{sharp}",
                "G",
                "G{sharp}",
                "A",
                "B{flat}",
                "B"
            ),
            temperament.noteNames
        )
    }
}
