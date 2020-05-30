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
package dev.altaris.tridio.ui.listeners

import android.content.Context
import dev.altaris.tridio.models.Song

interface PopupMenuListener {

    fun play(song: Song)

    fun goToAlbum(song: Song)

    fun goToArtist(song: Song)

    fun addToPlaylist(context: Context, song: Song)

    fun deleteSong(context: Context, song: Song)

    fun removeFromPlaylist(song: Song, playlistId: Long)

    fun playNext(song: Song)
}
