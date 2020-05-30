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
package dev.altaris.tridio.ui.fragments.base

import android.os.Bundle
import dev.altaris.tridio.playback.TridioMusicService.Companion.MEDIA_CALLER
import dev.altaris.tridio.playback.TridioMusicService.Companion.MEDIA_ID_ARG
import dev.altaris.tridio.playback.TridioMusicService.Companion.MEDIA_TYPE_ARG
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALBUM
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALL_ALBUMS
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALL_ARTISTS
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALL_FOLDERS
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALL_GENRES
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALL_PLAYLISTS
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ALL_SONGS
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_ARTIST
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_GENRE
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_PLAYLIST
import dev.altaris.tridio.constants.Constants.ACTION_REMOVED_FROM_PLAYLIST
import dev.altaris.tridio.constants.Constants.ACTION_SONG_DELETED
import dev.altaris.tridio.constants.Constants.ALBUM
import dev.altaris.tridio.constants.Constants.ARTIST
import dev.altaris.tridio.constants.Constants.CATEGORY_SONG_DATA
import dev.altaris.tridio.extensions.argumentOrEmpty
import dev.altaris.tridio.extensions.map
import dev.altaris.tridio.extensions.observe
import dev.altaris.tridio.models.CategorySongData
import dev.altaris.tridio.models.Genre
import dev.altaris.tridio.models.MediaID
import dev.altaris.tridio.models.Playlist
import dev.altaris.tridio.ui.fragments.FolderFragment
import dev.altaris.tridio.ui.fragments.GenreFragment
import dev.altaris.tridio.ui.fragments.PlaylistFragment
import dev.altaris.tridio.ui.fragments.album.AlbumDetailFragment
import dev.altaris.tridio.ui.fragments.album.AlbumsFragment
import dev.altaris.tridio.ui.fragments.artist.ArtistDetailFragment
import dev.altaris.tridio.ui.fragments.artist.ArtistFragment
import dev.altaris.tridio.ui.fragments.songs.CategorySongsFragment
import dev.altaris.tridio.ui.fragments.songs.SongsFragment
import dev.altaris.tridio.ui.viewmodels.MediaItemFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

open class MediaItemFragment : BaseNowPlayingFragment() {

    protected lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private lateinit var mediaType: String
    private var mediaId: String? = null
    private var caller: String? = null

    companion object {
        fun newInstance(mediaId: MediaID): MediaItemFragment {
            val args = Bundle().apply {
                putString(MEDIA_TYPE_ARG, mediaId.type)
                putString(MEDIA_ID_ARG, mediaId.mediaId)
                putString(MEDIA_CALLER, mediaId.caller)
            }
            return when (mediaId.type?.toInt()) {
                TYPE_ALL_SONGS -> SongsFragment().apply { arguments = args }
                TYPE_ALL_ALBUMS -> AlbumsFragment().apply { arguments = args }
                TYPE_ALL_PLAYLISTS -> PlaylistFragment().apply { arguments = args }
                TYPE_ALL_ARTISTS -> ArtistFragment().apply { arguments = args }
                TYPE_ALL_FOLDERS -> FolderFragment().apply { arguments = args }
                TYPE_ALL_GENRES -> GenreFragment().apply { arguments = args }
                TYPE_ALBUM -> AlbumDetailFragment().apply {
                    arguments = args.apply { putParcelable(ALBUM, mediaId.mediaItem) }
                }
                TYPE_ARTIST -> ArtistDetailFragment().apply {
                    arguments = args.apply { putParcelable(ARTIST, mediaId.mediaItem) }
                }
                TYPE_PLAYLIST -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Playlist).apply {
                            val data = CategorySongData(name, songCount, TYPE_PLAYLIST, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                TYPE_GENRE -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Genre).apply {
                            val data = CategorySongData(name, songCount, TYPE_GENRE, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                else -> SongsFragment().apply {
                    arguments = args
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaType = argumentOrEmpty(MEDIA_TYPE_ARG)
        mediaId = argumentOrEmpty(MEDIA_ID_ARG)
        caller = argumentOrEmpty(MEDIA_CALLER)

        val mediaId = MediaID(mediaType, mediaId, caller)
        mediaItemFragmentViewModel = getViewModel { parametersOf(mediaId) }

        mainViewModel.customAction
                .map { it.getContentIfNotHandled() }
                .observe(this) {
                    when (it) {
                        ACTION_SONG_DELETED -> mediaItemFragmentViewModel.reloadMediaItems()
                        ACTION_REMOVED_FROM_PLAYLIST -> mediaItemFragmentViewModel.reloadMediaItems()
                    }
                }
    }
}
