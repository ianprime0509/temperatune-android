package com.ianprime0509.temperatune.model

import com.ianprime0509.temperatune.util.positiveRem
import kotlin.math.absoluteValue
import kotlin.math.log2
import kotlin.math.pow

const val CENTS_IN_OCTAVE: Int = 1200

class InvalidTemperamentException(message: String) : Exception(message)

data class NoteOffset(val relativeTo: String, val cents: Double)

class Temperament(
    val name: String,
    val octaveBaseName: String,
    val referencePitch: Double,
    val referenceName: String,
    val referenceOctave: Int,
    notes: Map<String, NoteOffset>,
    val description: String = "",
    val source: String = ""
) {
    val noteOffsets: Map<String, Double> = computeOffsets(notes)
    val noteNames: List<String> = noteOffsets.keys.sortedBy { noteOffsets.getValue(it) }

    fun getPitch(note: String, octave: Int): Double {
        var offset = noteOffsets[note] ?: throw IllegalArgumentException("$note is not defined as a note")
        offset += CENTS_IN_OCTAVE * (octave - referenceOctave)
        return referencePitch * 2.0.pow(offset / CENTS_IN_OCTAVE)
    }

    fun getNearestNote(pitch: Double): NoteOffset {
        val baseOffset = noteOffsets.getValue(octaveBaseName)
        val offset = normalizeOffset(CENTS_IN_OCTAVE * log2(pitch / referencePitch), baseOffset)

        val index = noteNames.binarySearchBy(offset) { noteOffsets.getValue(it) }
        if (index >= 0) return NoteOffset(noteNames[index], 0.0)

        // There are two notes neighboring this pitch and we need to see which is closer
        val firstIndex = (-index - 1) % noteNames.size // -index - 1 is always positive
        val firstNote = noteNames[firstIndex]
        val firstOffset = NoteOffset(firstNote, offset - noteOffsets.getValue(firstNote))
        val secondIndex = (firstIndex - 1) positiveRem noteNames.size // Handle wrap-around at start of list
        val secondNote = noteNames[secondIndex]
        val secondOffset = NoteOffset(secondNote, offset - noteOffsets.getValue(secondNote))

        return if (firstOffset.cents.absoluteValue < secondOffset.cents.absoluteValue) firstOffset else secondOffset
    }

    private fun computeOffsets(notes: Map<String, NoteOffset>): Map<String, Double> {
        val offsets = mutableMapOf(Pair(referenceName, 0.0))
        val todo = mutableListOf(referenceName)

        while (todo.isNotEmpty()) {
            val currentName = todo.removeAt(todo.lastIndex)
            val currentOffset = offsets.getValue(currentName)

            // Check for the current note on the left hand side of an offset assignment
            // currentNote: [other, offset]
            if (currentName in notes) {
                val definition = notes.getValue(currentName)
                if (definition.relativeTo !in offsets) todo += definition.relativeTo
                defineOffset(offsets, definition.relativeTo, currentOffset - definition.cents)
            }

            // Check for the current note on the right hand side of an offset assignment
            // other: [currentNote, offset]
            notes.filter { it.value.relativeTo == currentName }.forEach { (other, definition) ->
                if (other !in offsets) todo += other
                defineOffset(offsets, other, currentOffset + definition.cents)
            }
        }

        normalizeOffsets(offsets)

        // Ensure that all notes in definitions actually got offsets
        val missingOffset = notes.keys - offsets.keys
        if (missingOffset.isNotEmpty()) {
            throw InvalidTemperamentException("Could not determine offsets for $missingOffset")
        }

        return offsets
    }

    private fun defineOffset(offsets: MutableMap<String, Double>, note: String, offset: Double) {
        if (note in offsets && (offsets.getValue(note) - offset) % CENTS_IN_OCTAVE != 0.0) {
            throw InvalidTemperamentException(
                "Conflicting definitions for $note: ${offsets.getValue(note)} and $offset"
            )
        }
        offsets[note] = offset
    }

    private fun normalizeOffset(offset: Double, baseOffset: Double): Double {
        val difference = (offset - baseOffset) positiveRem CENTS_IN_OCTAVE
        return baseOffset + difference
    }

    private fun normalizeOffsets(offsets: MutableMap<String, Double>) {
        if (octaveBaseName !in offsets) {
            throw InvalidTemperamentException("Octave base $octaveBaseName not defined as note")
        }

        val baseOffset = offsets.getValue(octaveBaseName)
        for ((note, offset) in offsets) {
            offsets[note] = normalizeOffset(offset, baseOffset)
        }
    }
}
