package ua.com.radiokot.camerapp.posters.domain

class SendStampPosterOptions(
    val id: String,
    val layers: Collection<StampPosterLayer>,
    val isDark: Boolean,
) {
    override fun toString(): String {
        return "SendStampPosterOptions(id='$id', layers=$layers, isDark=$isDark)"
    }
}
