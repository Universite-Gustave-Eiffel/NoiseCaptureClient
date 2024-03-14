package org.noise_planet.noisecapture

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

const val DATABASE_VERSION = 1

interface DatabaseDriverFactory {
    suspend fun provideDbDriver(): SqlDriver
}