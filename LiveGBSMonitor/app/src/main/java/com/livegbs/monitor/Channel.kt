data class ChannelResponse(
    val code: Int,
    val msg: String,
    val data: List<Channel>
)

data class Channel(
    val id: String,
    val name: String,
    val deviceName: String,
    val streamUrl: String
)