package ua.com.radiokot.camerapp.stamps.domain

class StampCollection(
    val id: String,
    val name: String,
) {
    /**
     * Primary collection is the default one for the stamps.
     * There's always one primary collection and it can't be deleted.
     */
    val isPrimary: Boolean =
        id == PRIMARY_ID

    fun copy(
        newName: String,
    ) = StampCollection(
        id = id,
        name = newName,
    )

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

    companion object {
        const val PRIMARY_ID = "0"
    }
}
