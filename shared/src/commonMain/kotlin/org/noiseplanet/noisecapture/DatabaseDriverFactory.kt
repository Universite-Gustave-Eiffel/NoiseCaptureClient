package org.noiseplanet.noisecapture

import app.cash.sqldelight.db.SqlDriver

const val DATABASE_VERSION = 1

interface DatabaseDriverFactory {

    suspend fun provideDbDriver(): SqlDriver
}
