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
import dev.altaris.tridio.R
import dev.altaris.tridio.playback.TridioMusicService.Companion.TYPE_PLAYLIST
import dev.altaris.tridio.constants.Constants.CATEGORY_SONG_DATA
import dev.altaris.tridio.databinding.FragmentCategorySongsBinding
import dev.altaris.tridio.extensions.addOnItemClick
import dev.altaris.tridio.extensions.argument
import dev.altaris.tridio.extensions.filter
import dev.altaris.tridio.extensions.getExtraBundle
import dev.altaris.tridio.extensions.inflateWithBinding
import dev.altaris.tridio.extensions.observe
import dev.altaris.tridio.extensions.safeActivity
import dev.altaris.tridio.extensions.toSongIds
import dev.altaris.tridio.models.CategorySongData
import dev.altaris.tridio.models.Song
import dev.altaris.tridio.ui.adapters.SongsAdapter
import dev.altaris.tridio.ui.fragments.base.MediaItemFragment
import dev.altaris.tridio.util.AutoClearedValue
import kotlinx.android.synthetic.main.fragment_album_detail.recyclerView

class CategorySongsFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    private lateinit var categorySongData: CategorySongData
    var binding by AutoClearedValue<FragmentCategorySongsBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categorySongData = argument(CATEGORY_SONG_DATA)
        binding = inflater.inflateWithBinding(R.layout.fragment_category_songs, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.categorySongData = categorySongData

        songsAdapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
            if (categorySongData.type == TYPE_PLAYLIST) {
                playlistId = categorySongData.id
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                val extras = getExtraBundle(songsAdapter.songs.toSongIds(), categorySongData.title)
                mainViewModel.mediaItemClicked(songsAdapter.songs[position], extras)
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    songsAdapter.updateData(list as List<Song>)
                }
    }
}
