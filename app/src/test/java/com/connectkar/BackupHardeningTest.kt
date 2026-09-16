package com.connectkar

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupHardeningTest {

    @Test
    fun `verify allowBackup is disabled in ApplicationInfo`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )

        val isAllowBackupEnabled = (appInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
        assertFalse("FLAG_ALLOW_BACKUP should be disabled in manifest", isAllowBackupEnabled)
    }

    @Test
    fun `verify data_extraction_rules excludes Room database and sensitive user PII files`() {
        val rulesFile = File("src/main/res/xml/data_extraction_rules.xml")
        assertTrue("data_extraction_rules.xml must exist", rulesFile.exists())
        val content = rulesFile.readText()

        assertTrue("Must have <cloud-backup> element", content.contains("<cloud-backup>"))
        assertTrue("Must have <device-transfer> element", content.contains("<device-transfer>"))
        assertTrue("Must explicitly exclude connectkar_db", content.contains("path=\"connectkar_db\""))
        assertTrue("Must explicitly exclude all databases", content.contains("domain=\"database\" path=\".\""))
        assertTrue("Must explicitly exclude shared preferences", content.contains("domain=\"sharedpref\""))
        assertTrue("Must explicitly exclude app internal files", content.contains("domain=\"file\""))
    }

    @Test
    fun `verify backup_rules excludes Room database and sensitive user PII files`() {
        val rulesFile = File("src/main/res/xml/backup_rules.xml")
        assertTrue("backup_rules.xml must exist", rulesFile.exists())
        val content = rulesFile.readText()

        assertTrue("Must have <full-backup-content> root element", content.contains("<full-backup-content>"))
        assertTrue("Must explicitly exclude connectkar_db", content.contains("path=\"connectkar_db\""))
        assertTrue("Must explicitly exclude all databases", content.contains("domain=\"database\" path=\".\""))
        assertTrue("Must explicitly exclude shared preferences", content.contains("domain=\"sharedpref\""))
        assertTrue("Must explicitly exclude app internal files", content.contains("domain=\"file\""))
    }
}
