package ua.com.radiokot.camerapp

interface StampRepository {

    suspend fun getStamps(
        asc: Boolean,
    ): List<Stamp>

    suspend fun getStamp(
        id: String,
    ): Stamp?
}
