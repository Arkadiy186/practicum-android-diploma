package ru.practicum.android.diploma.ui.searchfilters.recycleview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.practicum.android.diploma.databinding.ItemWorkplaceSelectableBinding
import ru.practicum.android.diploma.domain.models.filters.Country

class CountryAdapter(private val clickListener: OnClickListener) : ListAdapter<Country, CountryItemViewHolder>(
    object : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryItemViewHolder {
        val layoutInspector = LayoutInflater.from(parent.context)
        return CountryItemViewHolder(ItemWorkplaceSelectableBinding.inflate(layoutInspector, parent, false))
    }

    override fun onBindViewHolder(holder: CountryItemViewHolder, position: Int) {
        val country = getItem(position)
        holder.bind(country)
        holder.itemView.setOnClickListener {
            clickListener.onClick(country)
        }
    }

    interface OnClickListener {
        fun onClick(country: Country)
    }
}
