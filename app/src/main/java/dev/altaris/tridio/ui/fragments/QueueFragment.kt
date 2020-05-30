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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import dev.altaris.tridio.R
import dev.altaris.tridio.constants.Constants.ACTION_QUEUE_REORDER
import dev.altaris.tridio.constants.Constants.QUEUE_FROM
import dev.altaris.tridio.constants.Constants.QUEUE_TO
import dev.altaris.tridio.extensions.addOnItemClick
import dev.altaris.tridio.extensions.getExtraBundle
import dev.altaris.tridio.extensions.inflateTo
import dev.altaris.tridio.extensions.keepInOrder
import dev.altaris.tridio.extensions.observe
import dev.altaris.tridio.extensions.toSongIds
import dev.altaris.tridio.models.QueueData
import dev.altaris.tridio.repository.SongsRepository
import dev.altaris.tridio.ui.adapters.SongsAdapter
import dev.altaris.tridio.ui.fragments.base.BaseNowPlayingFragment
import dev.altaris.tridio.ui.widgets.DragSortRecycler
import kotlinx.android.synthetic.main.fragment_queue.recyclerView
import kotlinx.android.synthetic.main.fragment_queue.tvQueueTitle
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class QueueFragment : BaseNowPlayingFragment() {
    lateinit var adapter: SongsAdapter
    private lateinit var queueData: QueueData
    private var isReorderFromUser = false

    private val songsRepository by inject<SongsRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.fragment_queue, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongsAdapter().apply {
            isQueue = true
            popupMenuListener = mainViewModel.popupMenuListener
        }
        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@QueueFragment.adapter
        }

        nowPlayingViewModel.queueData.observe(this) { data ->
            this.queueData = data
            tvQueueTitle.text = data.queueTitle
            if (data.queue.isNotEmpty()) {
                fetchQueueSongs(data.queue)
            }
        }

        recyclerView.addOnItemClick { position, _ ->
            adapter.getSongForPosition(position)?.let { song ->
                val extras = getExtraBundle(adapter.songs.toSongIds(), queueData.queueTitle)
                mainViewModel.mediaItemClicked(song, extras)
            }
        }
    }

    private fun fetchQueueSongs(queue: LongArray) {
        //to avoid lag when reordering queue, we don't re-fetch queue if we know the reorder was from user
        if (isReorderFromUser) {
            isReorderFromUser = false
            return
        }

        // TODO should this logic be in a view model?
        launch {
            val songs = withContext(IO) {
                songsRepository.getSongsForIds(queue).keepInOrder(queue)
            } ?: return@launch
            adapter.updateData(songs)

            val dragSortRecycler = DragSortRecycler().apply {
                setViewHandleId(R.id.ivReorder)
                setOnItemMovedListener { from, to ->
                    isReorderFromUser = true
                    adapter.reorderSong(from, to)

                    val extras = Bundle().apply {
                        putInt(QUEUE_FROM, from)
                        putInt(QUEUE_TO, to)
                    }
                    mainViewModel.transportControls().sendCustomAction(ACTION_QUEUE_REORDER, extras)
                }
            }

            recyclerView.run {
                addItemDecoration(dragSortRecycler)
                addOnItemTouchListener(dragSortRecycler)
                addOnScrollListener(dragSortRecycler.scrollListener)
            }
        }
    }
}
