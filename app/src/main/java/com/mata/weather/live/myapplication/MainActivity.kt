package com.mata.weather.live.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mata.weather.live.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MultiSelectionAdapter
    private lateinit var binding: ActivityMainBinding
    private var items: ArrayList<ItemModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTagView()
        setupAdapter()
    }

    private fun setupAdapter() {
        for (i in 0 until 100) {
            items.add(ItemModel(id = i.toString(), content = "content $i", isSelected = false))
        }

        binding.apply {
            adapter = MultiSelectionAdapter(onItemClick = { id, isChecked ->
                handleItemSelected(id, isChecked)
            })

            rcvDemo.adapter = adapter
            rcvDemo.layoutManager =
                LinearLayoutManagerWrapper(this@MainActivity, LinearLayoutManager.VERTICAL, false)

            adapter.addList(items)

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    binding.rcvDemo.scrollToPosition(0)
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    binding.rcvDemo.scrollToPosition(0)
                }
            })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun initTagView() {
        val layoutInflater = layoutInflater
        val tagView = layoutInflater.inflate(R.layout.item_tag_search, binding.root, false)
        val edtSearch = tagView.findViewById<AppCompatEditText>(R.id.edtSearch)
        binding.tagView.addView(tagView)

        edtSearch.textChanges()
            .debounce(500)
            .distinctUntilChanged()
            .mapLatest { searchKey ->
            adapter.getFilter().filter(searchKey)
        }.launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun handleItemSelected(index: Int, isChecked: Boolean) {
        val tagView = layoutInflater.inflate(R.layout.item_tag, binding.root, false)
        tagView.tag = index

        if (isChecked) {
            val tagTextView = tagView.findViewById<View>(R.id.tagTextView) as TextView
            tagTextView.text = items[index].content
            binding.tagView.addView(tagView, binding.tagView.childCount - 1)

            tagView.setOnClickListener {
                handleRemoveTag(tagView.tag as Int)
                notifyItemChanged(tagView.tag as Int)
            }
        } else {
            handleRemoveTag(tagView.tag as Int)
        }

        items[index].isSelected = isChecked
    }

    private fun handleRemoveTag(tag: Int) {
        for (i in 0 until binding.tagView.childCount - 1) {
            val view = binding.tagView.getChildAt(i)
            if (tag == view.tag) {
                binding.tagView.removeViewAt(i)
                items[tag].isSelected = false
            }
        }
    }

    private fun notifyItemChanged(tag: Int) {
        val index = adapter.itemFilter.indexOf(items[tag])
        adapter.notifyItemChanged(index)
    }
}

@ExperimentalCoroutinesApi
@CheckResult
fun AppCompatEditText.textChanges(): Flow<String> = callbackFlow {

    val listener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            trySend(s.toString())
        }
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}