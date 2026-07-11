package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.example.data.local.UserEntity
import com.example.data.local.ListingEntity
import com.example.data.local.withSerializedDetails
import com.google.firebase.firestore.DocumentSnapshot

object FirebaseManager {
    val isAvailable: Boolean by lazy {
        try {
            FirebaseAuth.getInstance()
            FirebaseFirestore.getInstance()
            true
        } catch (e: Exception) {
            android.util.Log.i("FirebaseManager", "Firebase is not available (using local simulation fallback): ${e.message}")
            false
        }
    }

    init {
        if (isAvailable) {
            try {
                val appCheckClass = Class.forName("com.google.firebase.appcheck.FirebaseAppCheck")
                val getInstanceMethod = appCheckClass.getMethod("getInstance")
                val appCheckInstance = getInstanceMethod.invoke(null)
                
                val providerFactoryClass = if (com.example.BuildConfig.DEBUG) {
                    try {
                        Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    try {
                        Class.forName("com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory")
                    } catch (e1: Exception) {
                        try {
                            Class.forName("com.google.firebase.appcheck.recaptcha.ReCaptchaEnterpriseAppCheckProviderFactory")
                        } catch (e2: Exception) {
                            null
                        }
                    }
                }

                if (providerFactoryClass != null) {
                    val getFactoryInstanceMethod = providerFactoryClass.getMethod("getInstance")
                    val factoryInstance = getFactoryInstanceMethod.invoke(null)
                    
                    val appCheckProviderFactoryClass = Class.forName("com.google.firebase.appcheck.AppCheckProviderFactory")
                    val installMethod = appCheckClass.getMethod("installAppCheckProviderFactory", appCheckProviderFactoryClass)
                    installMethod.invoke(appCheckInstance, factoryInstance)
                    android.util.Log.i("AppCheck", "Successfully installed App Check provider factory: ${providerFactoryClass.name}")
                } else {
                    android.util.Log.w("AppCheck", "No supported App Check provider factory class found on classpath.")
                }
            } catch (e: Exception) {
                android.util.Log.e("FirebaseManager", "Failed to initialize App Check dynamically: ${e.message}")
            }
        }
    }

    val auth: FirebaseAuth?
        get() = if (isAvailable) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isAvailable) FirebaseFirestore.getInstance() else null

    val functions: FirebaseFunctions?
        get() = if (isAvailable) FirebaseFunctions.getInstance() else null
}

fun DocumentSnapshot.toUserEntity(): UserEntity? {
    return try {
        val uid = getString("uid") ?: id
        val fullName = getString("fullName") ?: return null
        val phoneNumber = getString("phoneNumber") ?: ""
        val society = getString("society") ?: ""
        val blockTower = getString("blockTower") ?: ""
        val flatNumber = getString("flatNumber") ?: ""
        val avatarIndex = getLong("avatarIndex")?.toInt() ?: 0
        val isVerified = getBoolean("isVerified") ?: false
        val isPending = getBoolean("isPending") ?: true
        val isCurrent = getBoolean("isCurrent") ?: false
        val role = getString("role") ?: "RESIDENT"
        val floor = getString("floor") ?: ""
        val residentType = getString("residentType") ?: "OWNER"
        val moveInDate = getString("moveInDate") ?: ""
        val proofDocumentUri = getString("proofDocumentUri") ?: ""
        
        UserEntity(
            uid = uid,
            fullName = fullName,
            phoneNumber = phoneNumber,
            society = society,
            blockTower = blockTower,
            flatNumber = flatNumber,
            avatarIndex = avatarIndex,
            isVerified = isVerified,
            isPending = isPending,
            isCurrent = isCurrent,
            role = role,
            floor = floor,
            residentType = residentType,
            moveInDate = moveInDate,
            proofDocumentUri = proofDocumentUri
        )
    } catch (e: Exception) {
        null
    }
}

fun DocumentSnapshot.toListingEntity(): ListingEntity? {
    return try {
        val idVal = getLong("localId")?.toInt() ?: 0
        val firestoreId = id
        val type = getString("type") ?: ""
        val title = getString("title") ?: ""
        val description = getString("description") ?: ""
        val price = getDouble("price") ?: 0.0
        val contact = getString("contact") ?: ""
        val society = getString("society") ?: ""
        val authorName = getString("authorName") ?: ""
        val authorFlat = getString("authorFlat") ?: ""
        val authorPhone = getString("authorPhone") ?: ""
        val authorUid = getString("authorUid") ?: ""
        val timestamp = getLong("timestamp") ?: System.currentTimeMillis()
        val likesCount = getLong("likesCount")?.toInt() ?: 0
        val isLikedByMe = getBoolean("isLikedByMe") ?: false
        val isBookmarked = getBoolean("isBookmarked") ?: false
        val category = getString("category") ?: ""
        val extra1 = getString("extra1") ?: ""
        val extra2 = getString("extra2") ?: ""
        val extra3 = getString("extra3") ?: ""
        val extra4 = getString("extra4") ?: ""
        val detailsJson = getString("detailsJson") ?: ""
        
        val entity = ListingEntity(
            id = idVal,
            firestoreId = firestoreId,
            type = type,
            title = title,
            description = description,
            price = price,
            contact = contact,
            society = society,
            authorName = authorName,
            authorFlat = authorFlat,
            authorPhone = authorPhone,
            authorUid = authorUid,
            timestamp = timestamp,
            likesCount = likesCount,
            isLikedByMe = isLikedByMe,
            isBookmarked = isBookmarked,
            category = category,
            extra1 = extra1,
            extra2 = extra2,
            extra3 = extra3,
            extra4 = extra4,
            detailsJson = detailsJson
        )
        if (detailsJson.isEmpty()) {
            entity.withSerializedDetails()
        } else {
            entity
        }
    } catch (e: Exception) {
        null
    }
}

// Suspend extension to await Google Task completion
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result, null)
        } else {
            continuation.resumeWith(Result.failure(task.exception ?: RuntimeException("Task failed")))
        }
    }
}

