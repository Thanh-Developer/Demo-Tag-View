package com.mata.weather.live.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
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

            rvTextValue.adapter = adapter

            adapter.addList(items)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun initTagView() {
        val layoutInflater = layoutInflater
        val tagView = layoutInflater.inflate(R.layout.item_tag_search, binding.root, false)
        val edtSearch = tagView.findViewById<AppCompatEditText>(R.id.edtSearch)
        binding.tagLayout.addView(tagView)

        edtSearch.textChanges()
            .debounce(500)
            .distinctUntilChanged()
            .mapLatest { searchKey ->
                adapter.getFilter().filter(searchKey)
            }
            .launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun handleItemSelected(index: Int, isChecked: Boolean) {
        val layoutInflater = layoutInflater
        val tagView = layoutInflater.inflate(R.layout.item_tag, binding.root, false)
        tagView.tag = index

        if (isChecked) {
            val tagTextView = tagView.findViewById<View>(R.id.tagTextView) as TextView
            tagTextView.text = items[index].content
            binding.tagLayout.addView(tagView, binding.tagLayout.childCount - 1)

            tagView.setOnClickListener {
                handleRemoveTag(tagView.tag as Int)
            }
        } else {
            handleRemoveTag(tagView.tag as Int)
        }

        items[index].isSelected = isChecked
    }

    private fun handleRemoveTag(tag: Int) {
        for (i in 0 until binding.tagLayout.childCount - 1) {
            val view = binding.tagLayout.getChildAt(i)
            if (tag == view.tag) {
                binding.tagLayout.removeViewAt(i)
                items[tag].isSelected = false
                adapter.submitList(items.toList())
                // Have item header so we need + 1 when update ui
                adapter.notifyItemChanged(tag + 1)
            }
        }
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