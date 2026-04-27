package ua.com.radiokot.camerapp.stamps.domain

class StampCollection(
    val id: String,
    val name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StampCollection) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "StampCollection(id='$id')"
    }
}
