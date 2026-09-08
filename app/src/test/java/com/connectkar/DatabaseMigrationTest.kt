package com.connectkar
 
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.connectkar.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    @Test
    fun migration_5_to_6_preservesExistingDataAndAddsNewColumns() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbName = "migration_test_db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create v5 schema
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `users` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `uid` TEXT NOT NULL,
                            `fullName` TEXT NOT NULL,
                            `phoneNumber` TEXT NOT NULL,
                            `society` TEXT NOT NULL,
                            `blockTower` TEXT NOT NULL,
                            `flatNumber` TEXT NOT NULL,
                            `avatarIndex` INTEGER NOT NULL,
                            `isVerified` INTEGER NOT NULL,
                            `isPending` INTEGER NOT NULL,
                            `isCurrent` INTEGER NOT NULL,
                            `role` TEXT NOT NULL,
                            `floor` TEXT NOT NULL,
                            `residentType` TEXT NOT NULL,
                            `moveInDate` TEXT NOT NULL,
                            `proofDocumentUri` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_uid` ON `users` (`uid`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_users_society` ON `users` (`society`)")

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `listings` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `firestoreId` TEXT NOT NULL,
                            `type` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `description` TEXT NOT NULL,
                            `price` REAL NOT NULL,
                            `contact` TEXT NOT NULL,
                            `society` TEXT NOT NULL,
                            `authorName` TEXT NOT NULL,
                            `authorFlat` TEXT NOT NULL,
                            `authorPhone` TEXT NOT NULL,
                            `authorUid` TEXT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `likesCount` INTEGER NOT NULL,
                            `isLikedByMe` INTEGER NOT NULL,
                            `isBookmarked` INTEGER NOT NULL,
                            `category` TEXT NOT NULL,
                            `extra1` TEXT NOT NULL,
                            `extra2` TEXT NOT NULL,
                            `extra3` TEXT NOT NULL,
                            `extra4` TEXT NOT NULL,
                            `detailsJson` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_firestoreId` ON `listings` (`firestoreId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_society` ON `listings` (`society`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_type` ON `listings` (`type`)")

                    // Insert sample v5 data
                    db.execSQL(
                        """
                        INSERT INTO `users` (
                            `uid`, `fullName`, `phoneNumber`, `society`, `blockTower`, `flatNumber`,
                            `avatarIndex`, `isVerified`, `isPending`, `isCurrent`, `role`, `floor`,
                            `residentType`, `moveInDate`, `proofDocumentUri`
                        ) VALUES (
                            'user_abc', 'Rahul Sharma', '9999999999', 'Palm Grove', 'A', '101',
                            1, 1, 0, 1, 'RESIDENT', '1', 'OWNER', '2023-01-01', ''
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        INSERT INTO `listings` (
                            `firestoreId`, `type`, `title`, `description`, `price`, `contact`, `society`,
                            `authorName`, `authorFlat`, `authorPhone`, `authorUid`, `timestamp`,
                            `likesCount`, `isLikedByMe`, `isBookmarked`, `category`, `extra1`, `extra2`,
                            `extra3`, `extra4`, `detailsJson`
                        ) VALUES (
                            'fs_123', 'MARKETPLACE', 'Sofa', 'Comfortable sofa', 5000.0, '9999999999', 'Palm Grove',
                            'Rahul', '101', '9999999999', 'user_abc', 1700000000000, 2, 0, 1, 'Furniture',
                            '', '', '', '', '{"category":"Furniture"}'
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    // Not invoked on initial create
                }
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase

        // Execute MIGRATION_5_6
        AppDatabase.MIGRATION_5_6.migrate(db)

        // Verify users table has new pendingSync column and existing record is preserved
        val userCursor = db.query("SELECT * FROM users WHERE uid = 'user_abc'")
        assertTrue("User record should exist after migration", userCursor.moveToFirst())
        val userPendingSyncCol = userCursor.getColumnIndex("pendingSync")
        assertTrue("pendingSync column must exist in users", userPendingSyncCol != -1)
        assertEquals(0, userCursor.getInt(userPendingSyncCol)) // Default value 0 (false)
        assertEquals("Rahul Sharma", userCursor.getString(userCursor.getColumnIndexOrThrow("fullName")))
        userCursor.close()

        // Verify listings table has new columns (pendingSync, isDraft, isPublic) and existing record is preserved
        val listingCursor = db.query("SELECT * FROM listings WHERE firestoreId = 'fs_123'")
        assertTrue("Listing record should exist after migration", listingCursor.moveToFirst())
        val listingPendingSyncCol = listingCursor.getColumnIndex("pendingSync")
        val isDraftCol = listingCursor.getColumnIndex("isDraft")
        val isPublicCol = listingCursor.getColumnIndex("isPublic")

        assertTrue("pendingSync column must exist in listings", listingPendingSyncCol != -1)
        assertTrue("isDraft column must exist in listings", isDraftCol != -1)
        assertTrue("isPublic column must exist in listings", isPublicCol != -1)

        assertEquals(0, listingCursor.getInt(listingPendingSyncCol))
        assertEquals(0, listingCursor.getInt(isDraftCol))
        assertEquals(0, listingCursor.getInt(isPublicCol))
        assertEquals("Sofa", listingCursor.getString(listingCursor.getColumnIndexOrThrow("title")))
        assertEquals(5000.0, listingCursor.getDouble(listingCursor.getColumnIndexOrThrow("price")), 0.001)
        listingCursor.close()

        db.close()
        context.deleteDatabase(dbName)
    }
}
