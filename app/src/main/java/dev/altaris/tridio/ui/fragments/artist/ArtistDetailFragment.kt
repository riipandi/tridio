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
package dev.altaris.tridio.ui.fragments.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import dev.altaris.tridio.R
import dev.altaris.tridio.constants.Constants.ARTIST
import dev.altaris.tridio.databinding.FragmentArtistDetailBinding
import dev.altaris.tridio.extensions.addOnItemClick
import dev.altaris.tridio.extensions.argument
import dev.altaris.tridio.extensions.filter
import dev.altaris.tridio.extensions.getExtraBundle
import dev.altaris.tridio.extensions.inflateWithBinding
import dev.altaris.tridio.extensions.observe
import dev.altaris.tridio.extensions.safeActivity
import dev.altaris.tridio.extensions.toSongIds
import dev.altaris.tridio.models.Artist
import dev.altaris.tridio.models.Song
import dev.altaris.tridio.repository.AlbumRepository
import dev.altaris.tridio.ui.adapters.AlbumAdapter
import dev.altaris.tridio.ui.adapters.SongsAdapter
import dev.altaris.tridio.ui.fragments.base.MediaItemFragment
import dev.altaris.tridio.util.AutoClearedValue
import kotlinx.android.synthetic.main.fragment_artist_detail.recyclerView
import kotlinx.android.synthetic.main.fragment_artist_detail.rvArtistAlbums
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class ArtistDetailFragment : MediaItemFragment() {
    lateinit var artist: Artist
    var binding by AutoClearedValue<FragmentArtistDetailBinding>(this)

    private val albumRepository by inject<AlbumRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        artist = argument(ARTIST)
        binding = inflater.inflateWithBinding(R.layout.fragment_artist_detail, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.artist = artist

        val adapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        recyclerView.layoutManager = LinearLayoutManager(safeActivity)
        recyclerView.adapter = adapter

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    adapter.updateData(list as List<Song>)
                }

        recyclerView.addOnItemClick { position: Int, _: View ->
            val extras = getExtraBundle(adapter.songs.toSongIds(), artist.name)
            mainViewModel.mediaItemClicked(adapter.songs[position], extras)
        }

        setupArtistAlbums()
    }

    private fun setupArtistAlbums() {
        val albumsAdapter = AlbumAdapter(true)
        rvArtistAlbums.apply {
            layoutManager = LinearLayoutManager(safeActivity, HORIZONTAL, false)
            adapter = albumsAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(albumsAdapter.albums[position], null)
            }
        }

        // TODO should this be in a view model?
        launch {
            val albums = withContext(IO) {
                albumRepository.getAlbumsForArtist(artist.id)
            }
            albumsAdapter.updateData(albums)
        }
    }
}
