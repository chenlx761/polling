package com.chenming.common.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

open class BindingViewHolder<T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(
    binding.root
)