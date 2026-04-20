package ua.com.radiokot.camerapp

import java.time.LocalDateTime

class Stamp(
    val id: String,
    val imageUri: String,
    val caption: String?,
    val takenAtLocal: LocalDateTime,
    val isReadOnly: Boolean,
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
        return "Stamp(id='$id', caption=$caption)"
    }
}
