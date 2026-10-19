package app.nighttrain.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/** Every trip, moment and note in one JSON file. Nothing leaves the phone unless you export it. */
object Backup {
    private const val VERSION = 1

    fun encode(all: Everything): String = JSONObject()
        .put("app", "nighttrain")
        .put("version", VERSION)
        .put("trips", JSONArray(all.trips.map {
            JSONObject().put("id", it.id).put("from", it.from).put("to", it.to).put("seed", it.seed).put("length", it.length)
                .put("plan", it.plan).put("pos", it.pos).put("startedAt", it.startedAt).put("endedAt", it.endedAt)
                .put("tunnel", it.tunnel).put("sleeper", it.sleeper)
        }))
        .put("moments", JSONArray(all.moments.map {
            JSONObject().put("id", it.id).put("tripId", it.tripId).put("cardId", it.cardId).put("at", it.at).put("to", it.to)
                .put("result", it.result).put("starred", it.starred).put("note", it.note)
        }))
        .put("notes", JSONArray(all.notes.map {
            JSONObject().put("id", it.id).put("from", it.from).put("text", it.text).put("createdAt", it.createdAt).put("readAt", it.readAt)
        }))
        .toString(2)

    private fun JSONObject.list(key: String) = optJSONArray(key)?.let { a -> (0 until a.length()).map { a.getJSONObject(it) } }.orEmpty()

    /** Throws [IllegalArgumentException] when the text is not a Night Train backup. */
    fun decode(text: String): Everything {
        val root = runCatching { JSONObject(text) }.getOrNull()
        require(root != null && root.optString("app") == "nighttrain") { "Not a Night Train backup" }
        return Everything(
            trips = root.list("trips").map {
                Trip(
                    id = it.getLong("id"), from = it.getString("from"), to = it.getString("to"), seed = it.optLong("seed"),
                    length = it.optString("length", Length.EXPRESS.name), plan = it.optString("plan"), pos = it.optInt("pos"),
                    startedAt = it.optLong("startedAt"), endedAt = it.optLong("endedAt"),
                    tunnel = it.optBoolean("tunnel"), sleeper = it.optBoolean("sleeper"),
                )
            },
            moments = root.list("moments").map {
                Moment(
                    id = it.getLong("id"), tripId = it.getLong("tripId"), cardId = it.getString("cardId"), at = it.optLong("at"),
                    to = it.optInt("to"), result = it.optString("result", Result.DONE.name), starred = it.optBoolean("starred"),
                    note = it.optString("note"),
                )
            },
            notes = root.list("notes").map {
                Note(id = it.getLong("id"), from = it.optInt("from"), text = it.optString("text"), createdAt = it.optLong("createdAt"), readAt = it.optLong("readAt"))
            },
        )
    }

    fun write(context: Context, uri: Uri, all: Everything) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(encode(all).toByteArray()) } ?: error("Can't write there")
    }

    fun read(context: Context, uri: Uri): Everything {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } ?: error("Can't read that")
        return decode(text)
    }
}
