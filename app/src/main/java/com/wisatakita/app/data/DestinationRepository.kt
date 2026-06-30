package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.BuildConfig
import com.wisatakita.app.data.db.AppDatabase
import com.wisatakita.app.data.db.DestinationCacheEntity
import com.wisatakita.app.data.remote.ApiHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class DestinationLoadResult(
    val destinations: List<Destination>,
    val sourceLabel: String,
    val validationIssues: List<DestinationValidationIssue> = emptyList()
)

class DestinationRepository(private val context: Context) {
    private val cacheDao by lazy { AppDatabase.getInstance(context).destinationCacheDao() }

    suspend fun getDestinations(): List<Destination> = getDestinationsWithSource().destinations

    suspend fun getAllDestinations(): List<Destination> = getDestinations()

    suspend fun getDestinationsWithSource(): DestinationLoadResult = withContext(Dispatchers.IO) {
        val local = loadAssetDestinations()
        val online = runCatching {
            DestinationJsonMapper.parseRoot(ApiHttpClient.get(BuildConfig.DESTINATION_API_URL))
        }.getOrNull()

        if (!online.isNullOrEmpty() && isCompleteWebServiceData(online, local)) {
            cacheDestinations(online, "Data dari Web Service")
            return@withContext online.localize().toLoadResult(localizedSourceLabel("Data dari Web Service"))
        }

        val cached = loadCachedDestinations()
        if (cached.isNotEmpty()) {
            val label = if (online.isNullOrEmpty()) {
                "Mode offline: cache Web Service"
            } else {
                "Cache Web Service: data online belum valid"
            }
            return@withContext cached.localize().toLoadResult(localizedSourceLabel(label))
        }

        if (local.isNotEmpty()) {
            val label = if (online.isNullOrEmpty()) {
                "Mode offline: data lokal"
            } else {
                "Data lokal terbaru: Web Service belum sinkron"
            }
            return@withContext local.localize().toLoadResult(localizedSourceLabel(label))
        }

        DestinationData.list.localize().toLoadResult(localizedSourceLabel("Mode offline: data bawaan"))
    }

    suspend fun getDestinationById(id: String): Destination? {
        return getDestinations().find { it.id == id }
    }

    private fun loadAssetDestinations(): List<Destination> {
        return runCatching {
            val json = context.assets.open("destinations.json")
                .bufferedReader().use { it.readText() }
            DestinationJsonMapper.parseRoot(json)
        }.getOrDefault(emptyList())
    }

    private fun isCompleteWebServiceData(online: List<Destination>, local: List<Destination>): Boolean {
        val hasEnoughItems = local.isEmpty() || online.size >= local.size
        val hasCoordinates = online.all(LocationTools::hasValidCoordinates)
        val hasRichMetadata = online.all { it.reviewCount > 0 && it.category != "Wisata" }
        return hasEnoughItems && hasCoordinates && hasRichMetadata
    }

    private suspend fun cacheDestinations(destinations: List<Destination>, sourceLabel: String) {
        val now = System.currentTimeMillis()
        cacheDao.clear()
        cacheDao.upsertAll(
            destinations.map {
                DestinationCacheEntity(
                    destinationId = it.id,
                    name = it.name,
                    payloadJson = DestinationJsonMapper.toJson(it),
                    cachedAt = now,
                    sourceLabel = sourceLabel
                )
            }
        )
    }

    private suspend fun loadCachedDestinations(): List<Destination> {
        return cacheDao.getAll().mapNotNull { cached ->
            runCatching {
                DestinationJsonMapper.parseObject(JSONObject(cached.payloadJson))
            }.getOrNull()
        }
    }

    private fun List<Destination>.toLoadResult(sourceLabel: String): DestinationLoadResult {
        return DestinationLoadResult(
            destinations = this,
            sourceLabel = sourceLabel,
            validationIssues = DestinationValidator.validate(this)
        )
    }

    private fun List<Destination>.localize(): List<Destination> =
        DestinationLocalizer.localize(context, this)

    private fun localizedSourceLabel(label: String): String {
        return when (label) {
            "Data dari Web Service" -> "Web Service data"
            "Mode offline: cache Web Service" -> "Offline mode: Web Service cache"
            "Cache Web Service: data online belum valid" -> "Web Service cache: online data is not valid yet"
            "Mode offline: data lokal" -> "Offline mode: local data"
            "Data lokal terbaru: Web Service belum sinkron" -> "Latest local data: Web Service is not synchronized yet"
            "Mode offline: data bawaan" -> "Offline mode: bundled data"
            else -> label
        }.takeIf { com.wisatakita.app.LanguageUtil.currentLanguage(context) == com.wisatakita.app.LanguageUtil.ENGLISH } ?: label
    }
}
