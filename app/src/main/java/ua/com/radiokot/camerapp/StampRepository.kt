package ua.com.radiokot.camerapp

interface StampRepository {

    suspend fun getStamps(
        asc: Boolean,
    ): List<Stamp>
}
