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
package dev.altaris.tridio.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dev.altaris.tridio.R
import dev.altaris.tridio.extensions.addOnItemClick
import dev.altaris.tridio.extensions.getExtraBundle
import dev.altaris.tridio.extensions.inflateWithBinding
import dev.altaris.tridio.databinding.FragmentSearchBinding
import dev.altaris.tridio.extensions.observe
import dev.altaris.tridio.extensions.safeActivity
import dev.altaris.tridio.extensions.toSongIds
import dev.altaris.tridio.ui.adapters.AlbumAdapter
import dev.altaris.tridio.ui.adapters.ArtistAdapter
import dev.altaris.tridio.ui.adapters.SongsAdapter
import dev.altaris.tridio.ui.fragments.base.BaseNowPlayingFragment
import dev.altaris.tridio.ui.viewmodels.SearchViewModel
import dev.altaris.tridio.util.AutoClearedValue
import kotlinx.android.synthetic.main.fragment_search.btnBack
import kotlinx.android.synthetic.main.fragment_search.etSearch
import kotlinx.android.synthetic.main.fragment_search.rvAlbums
import kotlinx.android.synthetic.main.fragment_search.rvArtist
import kotlinx.android.synthetic.main.fragment_search.rvSongs
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchFragment : BaseNowPlayingFragment() {

    private val searchViewModel by sharedViewModel<SearchViewModel>()

    private lateinit var songAdapter: SongsAdapter
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var artistAdapter: ArtistAdapter

    var binding by AutoClearedValue<FragmentSearchBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_search, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        songAdapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }

        albumAdapter = AlbumAdapter()
        rvAlbums.apply {
            layoutManager = GridLayoutManager(safeActivity, 3)
            adapter = albumAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(albumAdapter.albums[position], null)
            }
        }

        artistAdapter = ArtistAdapter()
        rvArtist.apply {
            layoutManager = GridLayoutManager(safeActivity, 3)
            adapter = artistAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(artistAdapter.artists[position], null)
            }
        }

        rvSongs.addOnItemClick { position: Int, _: View ->
            songAdapter.getSongForPosition(position)?.let { song ->
                val extras = getExtraBundle(songAdapter.songs.toSongIds(), "All songs")
                mainViewModel.mediaItemClicked(song, extras)
            }
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchViewModel.search(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                songAdapter.updateData(emptyList())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        })
        btnBack.setOnClickListener { safeActivity.onBackPressed() }

        searchViewModel.searchLiveData.observe(this) { searchData ->
            songAdapter.updateData(searchData.songs)
            albumAdapter.updateData(searchData.albums)
            artistAdapter.updateData(searchData.artists)
        }

        binding.let {
            it.viewModel = searchViewModel
            it.setLifecycleOwner(this)
        }
    }
}
