package app.suhasdissa.memerize.backend.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelegramModel(
    @SerialName("messages") var messages: ArrayList<Messages> = arrayListOf()

)

@Serializable
data class Messages(

    @SerialName("id") var id: Int,
    @SerialName("media") var media: Media? = Media()

)

@Serializable
data class Media(
    @SerialName("_") var type: String? = null
)

//messageMediaDocument //messageMediaPhoto
// https://tg.i-c-a.su/media/eplussl/6057
//  https://tg.i-c-a.su/json/eplussl?limit=100
