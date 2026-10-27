package ink.c0d4.seenema.data.model

import ink.c0d4.seenema.domain.model.IN
import ink.c0d4.seenema.domain.model.Provider
import ink.c0d4.seenema.domain.model.Results
import ink.c0d4.seenema.domain.model.WatchProviders
import com.google.gson.annotations.SerializedName

data class WhereToWatchProviderResponse(
    val id: Int?=null,
    val results: ResultsResponse?=null
){
    fun toWatchProviders():WatchProviders{
        return WatchProviders(id = id, results = results?.toResults())
    }
}

data class ResultsResponse(
    var IN:INResponse?=null
){
    fun toResults():Results{
        return Results(IN = IN?.toIN())
    }
}

data class INResponse(
    var link:String?=null,
    var flatrate: List<ProviderResponse>?=null,
    var buy: List<ProviderResponse>?=null,
    var rent: List<ProviderResponse>?=null
){
    fun toIN():IN{
        return IN(
            link = link,
            flatrate = flatrate?.map { it.toProvider() },
            buy = buy?.map { it.toProvider() },
            rent = rent?.map { it.toProvider() }
        )
    }
}

data class ProviderResponse(
    @SerializedName("logo_path")
    val logoPath: String?=null,
    @SerializedName("provider_id")
    val providerId: Int?=null,
    @SerializedName("provider_name")
    val providerName: String?=null
){
    fun toProvider(): Provider {
        return Provider(
            logoPath = logoPath,
            providerId = providerId,
            providerName = providerName
        )
    }
}