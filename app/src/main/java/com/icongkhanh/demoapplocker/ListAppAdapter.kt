package com.icongkhanh.demoapplocker

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.icongkhanh.demoapplocker.databinding.ItemAppBinding

class ListAppAdapter(val context: Context): ListAdapter<AppInfo, ListAppAdapter.AppHolder>(AppDiff) {

    private var onToggleLockedListener : OnToggleLockedListener? = null

    inner class AppHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppInfo) {

            binding.name.text = item.name
            binding.packageName.text = item.packageName

            binding.toggle.isChecked = item.isLocked

            binding.toggle.setOnCheckedChangeListener { buttonView, isChecked ->
                onToggleLockedListener?.onToggle(isChecked, item)
            }

            Glide.with(context)
                .load(Utils.getIconApp(item.packageName, context.packageManager))
                .into(binding.icon)
        }
    }

    object AppDiff: DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(context), parent, false)
        return AppHolder(binding)
    }

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnToggleLockedListener(l: OnToggleLockedListener) {
        onToggleLockedListener = l
    }

    interface OnToggleLockedListener {
        fun onToggle(isLocked: Boolean, appInfo: AppInfo)
    }
}