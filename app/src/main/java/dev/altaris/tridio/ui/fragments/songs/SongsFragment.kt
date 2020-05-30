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
package dev.altaris.tridio.ui.fragments.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.rxkprefs.Pref
import com.google.android.material.snackbar.Snackbar
import dev.altaris.tridio.PREF_SONG_SORT_ORDER
import dev.altaris.tridio.R
import dev.altaris.tridio.constants.SongSortOrder
import dev.altaris.tridio.constants.SongSortOrder.SONG_A_Z
import dev.altaris.tridio.constants.SongSortOrder.SONG_DURATION
import dev.altaris.tridio.constants.SongSortOrder.SONG_YEAR
import dev.altaris.tridio.constants.SongSortOrder.SONG_Z_A
import dev.altaris.tridio.extensions.addOnItemClick
import dev.altaris.tridio.extensions.filter
import dev.altaris.tridio.extensions.getExtraBundle
import dev.altaris.tridio.extensions.inflateTo
import dev.altaris.tridio.extensions.observe
import dev.altaris.tridio.extensions.disposeOnDetach
import dev.altaris.tridio.extensions.ioToMain
import dev.altaris.tridio.extensions.safeActivity
import dev.altaris.tridio.extensions.toSongIds
import dev.altaris.tridio.models.Song
import dev.altaris.tridio.ui.adapters.SongsAdapter
import dev.altaris.tridio.ui.fragments.base.MediaItemFragment
import dev.altaris.tridio.ui.listeners.SortMenuListener
import kotlinx.android.synthetic.main.layout_recyclerview.recyclerView
import org.koin.android.ext.android.inject

class SongsFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    private val sortOrderPref by inject<Pref<SongSortOrder>>(name = PREF_SONG_SORT_ORDER)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.layout_recyclerview, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        songsAdapter = SongsAdapter().apply {
            showHeader = true
            popupMenuListener = mainViewModel.popupMenuListener
            sortMenuListener = sortListener
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                songsAdapter.getSongForPosition(position)?.let { song ->
                    val extras = getExtraBundle(songsAdapter.songs.toSongIds(), getString(R.string.all_songs))
                    mainViewModel.mediaItemClicked(song, extras)
                }
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    songsAdapter.updateData(list as List<Song>)
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Auto trigger a reload when the sort order pref changes
        sortOrderPref.observe()
                .ioToMain()
                .subscribe { mediaItemFragmentViewModel.reloadMediaItems() }
                .disposeOnDetach(view)
    }

    private val sortListener = object : SortMenuListener {
        override fun shuffleAll() {
            songsAdapter.songs.shuffled().apply {
                val extras = getExtraBundle(toSongIds(), getString(R.string.all_songs))

                if (this.isEmpty()) {
                    Snackbar.make(recyclerView, R.string.shuffle_no_songs_error, Snackbar.LENGTH_SHORT)
                            .show()
                } else {
                    mainViewModel.mediaItemClicked(this[0], extras)
                }
            }
        }

        override fun sortAZ() = sortOrderPref.set(SONG_A_Z)

        override fun sortDuration() = sortOrderPref.set(SONG_DURATION)

        override fun sortYear() = sortOrderPref.set(SONG_YEAR)

        override fun numOfSongs() {}

        override fun sortZA() = sortOrderPref.set(SONG_Z_A)
    }
}