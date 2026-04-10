package ua.com.radiokot.camerapp

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Stamp(
    val id: String,
    val thumbnailUrl: String,
    val takenAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stamp) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Stamp(id='$id', thumbnailUrl='$thumbnailUrl', takenAt=$takenAt)"
    }
}
