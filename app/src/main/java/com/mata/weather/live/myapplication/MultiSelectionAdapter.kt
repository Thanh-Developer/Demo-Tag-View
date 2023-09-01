package com.mata.weather.live.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mata.weather.live.myapplication.databinding.ItemSelectionBinding
import java.util.stream.Collectors.toList

class MultiSelectionAdapter(
    private val onItemClick: (Int, Boolean) -> Unit,
) : ListAdapter<ItemModel, MultiSelectionAdapter.ItemViewHolder>(DiffUtilItem()) {
    private val itemFilter = ArrayList<ItemModel>()
    private val items = ArrayList<ItemModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemSelectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return itemFilter.size
    }

    override fun submitList(list: List<ItemModel>?) {
        super.submitList(ArrayList<ItemModel>(list ?: listOf()))
    }

    fun addList(list: List<ItemModel>) {
        itemFilter.addAll(list)
        items.addAll(list)
        submitList(itemFilter)
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val charString: String = constraint.toString()
                return if (charString.isEmpty()) {
                    results.also {
                        it.values = items
                    }
                } else {
                    results.also {
                        it.values = items.stream().filter { item ->
                            item.content.lowercase().contains(charString.lowercase())
                        }.collect(toList())
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.let {
                    itemFilter.clear()
                    itemFilter.addAll(it.values as List<ItemModel>)
                    submitList(itemFilter)
                }
            }
        }
    }

    inner class ItemViewHolder(val binding: ItemSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            binding.apply {

                cbSelect.isChecked = itemFilter[position].isSelected

                tvContent.text = itemFilter[position].content

                cbSelect.setOnClickListener {

                    val index = items.indexOf(itemFilter[layoutPosition])

                    items[index].isSelected = cbSelect.isChecked

                    itemFilter[layoutPosition].isSelected = cbSelect.isChecked

                    onItemClick.invoke(index, cbSelect.isChecked)
                }
            }
        }
    }

    class DiffUtilItem : DiffUtil.ItemCallback<ItemModel>() {
        override fun areItemsTheSame(
            oldItem: ItemModel, newItem: ItemModel
        ): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(
            oldItem: ItemModel, newItem: ItemModel
        ): Boolean {
            return newItem == oldItem
        }
    }
}
