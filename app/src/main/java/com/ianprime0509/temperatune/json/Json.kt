package com.ianprime0509.temperatune.json

import com.ianprime0509.temperatune.model.NoteOffset
import com.ianprime0509.temperatune.model.Temperament
import com.squareup.moshi.*

val moshi: Moshi = Moshi.Builder()
    .add(NoteOffset::class.java, NoteDefinitionAdapter())
    .add { type, _, moshi ->
        if (type != Temperament::class.java) {
            null
        } else {
            TemperamentAdapter(
                moshi.adapter(
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        NoteOffset::class.java
                    )
                )
            )
        }
    }
    .build()

class NoteDefinitionAdapter : JsonAdapter<NoteOffset>() {
    override fun toJson(writer: JsonWriter, value: NoteOffset?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.beginArray()
            .value(value.relativeTo)
            .value(value.cents)
            .endArray()
    }

    override fun fromJson(reader: JsonReader): NoteOffset {
        reader.beginArray()
        val relativeTo = reader.nextString()
        val cents = reader.nextDouble()
        reader.endArray()

        return NoteOffset(relativeTo, cents)
    }
}

class TemperamentAdapter(private val notesAdapter: JsonAdapter<Map<String, NoteOffset>>) :
    JsonAdapter<Temperament>() {
    override fun toJson(writer: JsonWriter, value: Temperament?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.beginObject()
            .name("name").value(value.name)
            .name("description").value(value.description)
            .name("source").value(value.source)
            .name("octaveBaseName").value(value.octaveBaseName)
            .name("referencePitch").value(value.referencePitch)
            .name("referenceName").value(value.referenceName)
            .name("referenceOctave").value(value.referenceOctave)
        writer.name("notes")
        notesAdapter.toJson(writer, value.noteOffsets.mapValues { (_, offset) ->
            NoteOffset(value.referenceName, offset)
        })
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): Temperament {
        var name: String? = null
        var octaveBaseName: String? = null
        var referenceName: String? = null
        var referencePitch: Double? = null
        var referenceOctave: Int? = null
        var notes: Map<String, NoteOffset>? = null
        var description = ""
        var source = ""

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "name" -> name = reader.nextString()
                "octaveBaseName" -> octaveBaseName = reader.nextString()
                "referenceName" -> referenceName = reader.nextString()
                "referencePitch" -> referencePitch = reader.nextDouble()
                "referenceOctave" -> referenceOctave = reader.nextInt()
                "notes" -> notes = notesAdapter.fromJson(reader)
                "description" -> description = reader.nextString()
                "source" -> source = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return Temperament(
            name = name ?: throw JsonDataException("No name provided"),
            octaveBaseName = octaveBaseName ?: throw JsonDataException("No octave base name provided"),
            referenceName = referenceName ?: throw JsonDataException("No reference name provided"),
            referencePitch = referencePitch ?: throw JsonDataException("No reference pitch provided"),
            referenceOctave = referenceOctave ?: throw JsonDataException("No reference octave provided"),
            notes = notes ?: throw JsonDataException("No notes provided"),
            description = description,
            source = source
        )
    }
}
