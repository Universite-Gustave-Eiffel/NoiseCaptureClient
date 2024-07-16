package org.noiseplanet.noisecapture

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabase(private val context: Context) : DatabaseDriverFactory {

    override suspend fun provideDbDriver(): SqlDriver {
        val callback = object : Callback(DATABASE_VERSION) {
            override fun onCreate(db: SupportSQLiteDatabase) {
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            }
        }
        return AndroidSqliteDriver(
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(context)
                    .callback(callback)
                    .name("Storage")
                    .noBackupDirectory(false)
                    .build()
            )
        )
    }
}
