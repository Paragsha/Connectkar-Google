package com.connectkar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiHelper {
    val moshi: Moshi by lazy {
        try {
            Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        } catch (t: Throwable) {
            Moshi.Builder().build()
        }
    }
    
    inline fun <reified T> toJson(value: T): String {
        return try {
            moshi.adapter(T::class.java).toJson(value)
        } catch (e: Exception) {
            ""
        }
    }

    inline fun <reified T> fromJson(json: String): T? {
        return try {
            if (json.isEmpty()) null else moshi.adapter(T::class.java).fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun toJsonStringList(list: List<String>): String {
        return serializePhotoUrls(list)
    }

    fun serializePhotoUrls(list: List<String>): String {
        if (list.isEmpty()) return ""
        return try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
            moshi.adapter<List<String>>(listType).toJson(list)
        } catch (e: Exception) {
            toJson(ListingPhotosJson(list))
        }
    }

    fun deserializePhotoUrls(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val trimmed = json.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                val parsed = moshi.adapter<List<String>>(listType).fromJson(trimmed)
                if (parsed != null) return parsed.filter { it.isNotBlank() }
            } catch (_: Exception) {
            }
        }
        if (trimmed.startsWith("{") && trimmed.contains("\"urls\"")) {
            try {
                val parsed = fromJson<ListingPhotosJson>(trimmed)
                if (parsed != null && parsed.urls.isNotEmpty()) return parsed.urls.filter { it.isNotBlank() }
            } catch (_: Exception) {
            }
        }
        return trimmed.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ListingPhotosJson(val urls: List<String> = emptyList())

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MarketplaceDetailsJson(val category: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ServiceDetailsJson(val rating: String, val baseRate: Double)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class CarpoolDetailsJson(val origin: String, val destination: String, val seats: String, val departureTime: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class PropertyDetailsJson(val bhk: String, val rent: Double, val status: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MealDetailsJson(val deliveryInfo: String, val mealPrice: Double)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class VehicleDetailsJson(val plateNumber: String, val vehicleModel: String, val locationSpot: String, val securityTag: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class EventDetailsJson(val eventLocation: String, val timing: String)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ExtendedMarketplaceDetails(
    val brand: String = "",
    val model: String = "",
    val itemAge: String = "",
    val quantity: Int = 1,
    val meetupLocation: String = "",
    val preferredDays: List<String> = emptyList(),
    val timePreference: String = "",
    val isNegotiable: Boolean = false,
    val paymentMethods: List<String> = emptyList(),
    // Property specific fields
    val wingFlatNumber: String = "",
    val bhkType: String = "",
    val furnishedStatus: String = "",
    val propertyType: String = "",
    val beds: Int = 0,
    val baths: Int = 0,
    val sqft: Int = 0,
    val amenities: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val verificationRequested: Boolean = false
)

fun ListingEntity.propertyDetails(): ExtendedMarketplaceDetails {
    if (detailsJson.isBlank()) return ExtendedMarketplaceDetails()
    return MoshiHelper.fromJson<ExtendedMarketplaceDetails>(detailsJson) ?: ExtendedMarketplaceDetails()
}

fun ListingEntity.photoUrls(): List<String> {
    return MoshiHelper.deserializePhotoUrls(extra1)
}

val ListingEntity.primaryPhotoUrl: String
    get() = photoUrls().firstOrNull() ?: ""

fun ListingEntity.primaryPhotoUrl(fallback: String): String {
    val first = photoUrls().firstOrNull()
    return if (!first.isNullOrBlank()) first else fallback
}

@Entity(
    tableName = "listings",
    indices = [
        androidx.room.Index(value = ["firestoreId"]),
        androidx.room.Index(value = ["society"]),
        androidx.room.Index(value = ["type"]),
        androidx.room.Index(value = ["authorUid"]),
        androidx.room.Index(value = ["isBookmarked", "type"])
    ]
)
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firestoreId: String = "", // Firestore document reference
    val type: String, // "MARKETPLACE", "SERVICE", "EVENT", "CARPOOL", "PROPERTY", "MEAL", "FEED", "VEHICLE"
    val title: String,
    val description: String,
    val price: Double = 0.0,
    val contact: String = "",
    val society: String = "", // e.g. "Sylvan County", "Aqualily Estate"
    val authorName: String = "Resident",
    val authorFlat: String = "",
    val authorPhone: String = "",
    val authorUid: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isBookmarked: Boolean = false,
    val category: String = "", // e.g. "Electronics", "Plumbing", "Veg", "Rent", "Sell"
    val extra1: String = "", // origin, area, veg/non-veg, vehicle plate
    val extra2: String = "", // destination, BHK, timing, vehicle model
    val extra3: String = "", // seats, delivery, vehicle status
    val extra4: String = "",
    val detailsJson: String = "", // Polymorphic serialized details
    val pendingSync: Boolean = false,
    val isDraft: Boolean = false,
    val isPublic: Boolean = false
) {
    @androidx.room.Ignore
    @kotlin.jvm.Transient
    @Volatile
    private var cachedDetails: ListingDetails? = null

    val details: ListingDetails
        get() {
            var current = cachedDetails
            if (current == null) {
                current = computeDetails()
                cachedDetails = current
            }
            return current
        }

    private fun computeDetails(): ListingDetails {
        if (detailsJson.isNotEmpty()) {
            try {
                return when (type) {
                    "MARKETPLACE" -> {
                        val obj = MoshiHelper.fromJson<MarketplaceDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Marketplace(obj.category) else ListingDetails.Marketplace(category)
                    }
                    "SERVICE" -> {
                        val obj = MoshiHelper.fromJson<ServiceDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Service(obj.rating, obj.baseRate) else ListingDetails.Service(extra4.ifEmpty { "4.5" }, price)
                    }
                    "CARPOOL" -> {
                        val obj = MoshiHelper.fromJson<CarpoolDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Carpool(obj.origin, obj.destination, obj.seats, obj.departureTime) else ListingDetails.Carpool(extra1, extra2, extra3, extra4)
                    }
                    "PROPERTY" -> {
                        val obj = MoshiHelper.fromJson<PropertyDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Property(obj.bhk, obj.rent, obj.status) else ListingDetails.Property(extra2, price, extra3)
                    }
                    "MEAL" -> {
                        val obj = MoshiHelper.fromJson<MealDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Meal(obj.deliveryInfo, obj.mealPrice) else ListingDetails.Meal(extra3, price)
                    }
                    "VEHICLE" -> {
                        val obj = MoshiHelper.fromJson<VehicleDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Vehicle(obj.plateNumber, obj.vehicleModel, obj.locationSpot, obj.securityTag) else ListingDetails.Vehicle(extra1, extra2, extra3, extra4)
                    }
                    "EVENT" -> {
                        val obj = MoshiHelper.fromJson<EventDetailsJson>(detailsJson)
                        if (obj != null) ListingDetails.Event(obj.eventLocation, obj.timing) else ListingDetails.Event(extra1, extra2)
                    }
                    else -> ListingDetails.GeneralFeed
                }
            } catch (e: Exception) {
                // Fallback below
            }
        }
        return when (type) {
            "MARKETPLACE" -> ListingDetails.Marketplace(category)
            "SERVICE" -> ListingDetails.Service(extra4.ifEmpty { "4.5" }, price)
            "CARPOOL" -> ListingDetails.Carpool(extra1, extra2, extra3, extra4)
            "PROPERTY" -> ListingDetails.Property(extra2, price, extra3)
            "MEAL" -> ListingDetails.Meal(extra3, price)
            "VEHICLE" -> ListingDetails.Vehicle(extra1, extra2, extra3, extra4)
            "EVENT" -> ListingDetails.Event(extra1, extra2)
            else -> ListingDetails.GeneralFeed
        }
    }
}

fun ListingEntity.withSerializedDetails(): ListingEntity {
    val jsonStr = when (type) {
        "MARKETPLACE" -> MoshiHelper.toJson(MarketplaceDetailsJson(category))
        "SERVICE" -> MoshiHelper.toJson(ServiceDetailsJson(extra4.ifEmpty { "4.5" }, price))
        "CARPOOL" -> MoshiHelper.toJson(CarpoolDetailsJson(extra1, extra2, extra3, extra4))
        "PROPERTY" -> MoshiHelper.toJson(PropertyDetailsJson(extra2, price, extra3))
        "MEAL" -> MoshiHelper.toJson(MealDetailsJson(extra3, price))
        "VEHICLE" -> MoshiHelper.toJson(VehicleDetailsJson(extra1, extra2, extra3, extra4))
        "EVENT" -> MoshiHelper.toJson(EventDetailsJson(extra1, extra2))
        else -> ""
    }
    return this.copy(detailsJson = jsonStr)
}

fun ListingEntity.toFirestoreMap(): HashMap<String, Any?> {
    val map = hashMapOf<String, Any?>(
        "localId" to id,
        "type" to type,
        "title" to title,
        "description" to description,
        "price" to price,
        "contact" to contact,
        "society" to society,
        "authorName" to authorName,
        "authorFlat" to authorFlat,
        "authorPhone" to authorPhone,
        "authorUid" to authorUid,
        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "clientTimestamp" to timestamp,
        "likesCount" to likesCount,
        "category" to category,
        "extra1" to extra1,
        "extra2" to extra2,
        "extra3" to extra3,
        "extra4" to extra4,
        "detailsJson" to detailsJson,
        "isDraft" to isDraft,
        "isPublic" to isPublic
    )
    val photosList = photoUrls()
    map["photos"] = photosList
    map["photoUrls"] = photosList
    return map
}

sealed class ListingDetails {
    data class Marketplace(val category: String) : ListingDetails()
    data class Service(val rating: String, val baseRate: Double) : ListingDetails()
    data class Carpool(val origin: String, val destination: String, val seats: String, val departureTime: String) : ListingDetails()
    data class Property(val bhk: String, val rent: Double, val status: String) : ListingDetails()
    data class Meal(val deliveryInfo: String, val mealPrice: Double) : ListingDetails()
    data class Vehicle(val plateNumber: String, val vehicleModel: String, val locationSpot: String, val securityTag: String) : ListingDetails()
    data class Event(val eventLocation: String, val timing: String) : ListingDetails()
    object GeneralFeed : ListingDetails()
}
