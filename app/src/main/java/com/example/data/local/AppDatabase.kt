package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ListingEntity::class,
        ChefProfileEntity::class,
        MenuItemEntity::class,
        MealOrderEntity::class,
        MealSubscriptionEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE listings ADD COLUMN authorUid TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE listings ADD COLUMN detailsJson TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN floor TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN residentType TEXT NOT NULL DEFAULT 'OWNER'")
                db.execSQL("ALTER TABLE users ADD COLUMN moveInDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN proofDocumentUri TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_uid` ON `users` (`uid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_users_society` ON `users` (`society`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_firestoreId` ON `listings` (`firestoreId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_society` ON `listings` (`society`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_type` ON `listings` (`type`)")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE listings ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE listings ADD COLUMN isDraft INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE listings ADD COLUMN isPublic INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `chef_profiles` (`uid` TEXT NOT NULL, `isChef` INTEGER NOT NULL, `chefStory` TEXT NOT NULL, `mealsServedCount` INTEGER NOT NULL, `regularsCount` INTEGER NOT NULL, `ratingAvg` REAL NOT NULL, `isSocietyVouched` INTEGER NOT NULL, `speciality` TEXT NOT NULL, `society` TEXT NOT NULL, `pendingSync` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`uid`))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_chef_profiles_uid` ON `chef_profiles` (`uid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_chef_profiles_society` ON `chef_profiles` (`society`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `menu_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firestoreId` TEXT NOT NULL, `chefUid` TEXT NOT NULL, `chefName` TEXT NOT NULL, `chefFlat` TEXT NOT NULL, `dishName` TEXT NOT NULL, `description` TEXT NOT NULL, `price` REAL NOT NULL, `portionsAvailable` INTEGER NOT NULL, `portionsBooked` INTEGER NOT NULL, `isVeg` INTEGER NOT NULL, `photoUrl` TEXT NOT NULL, `cuisineTags` TEXT NOT NULL, `mealType` TEXT NOT NULL, `deliveryWindow` TEXT NOT NULL, `society` TEXT NOT NULL, `isSoldOut` INTEGER NOT NULL, `pendingSync` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_items_firestoreId` ON `menu_items` (`firestoreId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_items_chefUid` ON `menu_items` (`chefUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_items_society` ON `menu_items` (`society`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `meal_orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firestoreId` TEXT NOT NULL, `buyerUid` TEXT NOT NULL, `buyerName` TEXT NOT NULL, `buyerFlat` TEXT NOT NULL, `chefUid` TEXT NOT NULL, `chefName` TEXT NOT NULL, `menuItemId` INTEGER NOT NULL, `dishName` TEXT NOT NULL, `servingSize` INTEGER NOT NULL, `deliveryWindow` TEXT NOT NULL, `dietaryNotes` TEXT NOT NULL, `deliveryMethod` TEXT NOT NULL, `addOns` TEXT NOT NULL, `itemTotal` REAL NOT NULL, `addOnsTotal` REAL NOT NULL, `deliveryFee` REAL NOT NULL, `grandTotal` REAL NOT NULL, `status` TEXT NOT NULL, `society` TEXT NOT NULL, `pendingSync` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_orders_firestoreId` ON `meal_orders` (`firestoreId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_orders_buyerUid` ON `meal_orders` (`buyerUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_orders_chefUid` ON `meal_orders` (`chefUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_orders_society` ON `meal_orders` (`society`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `meal_subscriptions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firestoreId` TEXT NOT NULL, `buyerUid` TEXT NOT NULL, `chefUid` TEXT NOT NULL, `chefName` TEXT NOT NULL, `planType` TEXT NOT NULL, `mealsPerCycle` INTEGER NOT NULL, `discountPercent` INTEGER NOT NULL, `daysRemaining` INTEGER NOT NULL, `renewalDate` TEXT NOT NULL, `status` TEXT NOT NULL, `pricePerMeal` REAL NOT NULL, `society` TEXT NOT NULL, `pendingSync` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_subscriptions_firestoreId` ON `meal_subscriptions` (`firestoreId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_subscriptions_buyerUid` ON `meal_subscriptions` (`buyerUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_subscriptions_chefUid` ON `meal_subscriptions` (`chefUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_subscriptions_society` ON `meal_subscriptions` (`society`)")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN timestamp INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_authorUid` ON `listings` (`authorUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_isBookmarked_type` ON `listings` (`isBookmarked`, `type`)")
            }
        }
    }
}
