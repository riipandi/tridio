/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
@file:Suppress("unused")

package dev.altaris.tridio

import android.app.Application
import dev.altaris.tridio.BuildConfig.DEBUG
import dev.altaris.tridio.db.roomModule
import dev.altaris.tridio.logging.FabricTree
import dev.altaris.tridio.network.lastFmModule
import dev.altaris.tridio.network.lyricsModule
import dev.altaris.tridio.network.networkModule
import dev.altaris.tridio.notifications.notificationModule
import dev.altaris.tridio.permissions.permissionsModule
import dev.altaris.tridio.playback.mediaModule
import dev.altaris.tridio.repository.repositoriesModule
import dev.altaris.tridio.ui.viewmodels.viewModelsModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class TridioApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(FabricTree())
        }

        val modules = listOf(
                mainModule,
                permissionsModule,
                mediaModule,
                prefsModule,
                networkModule,
                roomModule,
                notificationModule,
                repositoriesModule,
                viewModelsModule,
                lyricsModule,
                lastFmModule
        )
        startKoin(
                androidContext = this,
                modules = modules
        )
    }
}
