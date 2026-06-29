package com.chenming.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ObservableList.OnListChangedCallback
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.chenming.common.R
import com.chenming.common.databinding.AdapterEmptyBindingBinding
import com.chenming.common.databinding.AdapterEmptyBindingBindingImpl
import com.chenming.common.listener.OnItemClickListener
import com.chenming.common.listener.OnItemLongClickListener
import com.chenming.httprequest.XLog
import java.lang.ref.WeakReference

/**
 * 监听数据变化和有空布局的adapter
 */
open abstract class AutoNotifyAdapter
<T : BindingViewHolder<ViewDataBinding>, K, BIND : ViewDataBinding>() :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val ITEM_TYPE_EMPTY = 9999
    private var mIsNoData: Boolean = false
    protected lateinit var mDatas: ObservableList<K>
    protected lateinit var mContext: Context
    private lateinit var mCallback: WeakReferenceOnListChangedCallback<T, K, BIND>
    private var mOnDataRefreshFinishListener: OnDataRefreshFinishListener? = null
    protected var mOnItemClickListener: OnItemClickListener? = null
    private var mResId: Int? = null
    private var mEmptyImgRes: Int? = null
    protected var mOnItemLongClickListener: OnItemLongClickListener? = null
    private var mNeedStopFlash: Boolean = false
    private var mShowEmptyIcon: Boolean = true

    fun setEmptyImageResId(resId: Int) {
        mEmptyImgRes = resId
    }

    fun setShowEmptyIcon(showEmptyIcon: Boolean) {
        this.mShowEmptyIcon = showEmptyIcon
    }

    fun setNeedStopFlash(needStopFlash: Boolean) {
        this.mNeedStopFlash = needStopFlash
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.mOnItemLongClickListener = listener
    }


    public fun setOnDataRefreshFinishListener(onDataRefreshFinishListener: OnDataRefreshFinishListener) {
        this.mOnDataRefreshFinishListener = onDataRefreshFinishListener
    }


    public fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

    constructor(context: Context, resId: Int, datas: ObservableList<K>) : this() {
        this.mContext = context
        this.mResId = resId
        mDatas = datas
        //设置监听
        mCallback = WeakReferenceOnListChangedCallback(
            this
        )
        if (mDatas.size == 0) {
            mIsNoData = true
        }
        mDatas.addOnListChangedCallback(mCallback)
    }


    abstract fun onBindOtherViewHolder(holder: BIND, position: Int, adapterPosition: Int)

    open fun getOtherItemCount(): Int {
        return mDatas.size
    }

    fun setIsNoData(isNoData: Boolean) {
        mDatas?.let {
            it.clear()
        }
        this.mIsNoData = isNoData
    }


    override fun getItemViewType(position: Int): Int {
        if (mIsNoData)
            return ITEM_TYPE_EMPTY
        else {
            return getOtherItemViewType(position)
        }

    }

    open fun getOtherItemViewType(position: Int): Int {
        return 0
    }

    open fun <B : AdapterEmptyBindingBinding> getEmptyBinding(parent: ViewGroup): B {
        return DataBindingUtil.inflate<AdapterEmptyBindingBindingImpl>(
            LayoutInflater.from(mContext),
            R.layout.adapter_empty_binding, parent, false
        ) as B
    }

    fun onCreateOtherViewHolder(parent: ViewGroup, viewType: Int): T {
        val inflate = DataBindingUtil.inflate<BIND>(
            LayoutInflater.from(mContext),
            mResId!!, parent, false
        )
        return BindingViewHolder(inflate) as T
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_EMPTY) {
            val binding = getEmptyBinding<AdapterEmptyBindingBinding>(parent)
            if (mShowEmptyIcon) {
                if (mEmptyImgRes != null)
                    binding.ivEmpty.setImageResource(mEmptyImgRes!!)
            } else {
                binding.ivEmpty.visibility = ViewGroup.GONE
            }
            return BindingEmptyViewHolder(binding)
        }

        return onCreateOtherViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BindingEmptyViewHolder<*>) {
            XLog.e("BindingEmptyViewHolder")
        } else {
            onBindOtherViewHolder((holder as T).binding as BIND, position, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        if (mIsNoData) {
            return 1
        }
        return getOtherItemCount()
    }


    class BindingEmptyViewHolder<T : ViewDataBinding>(binding: T) :
        BindingViewHolder<ViewDataBinding>(
            binding
        )

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mCallback?.let {
            mDatas?.removeOnListChangedCallback(it)
        }
    }


    abstract class OnDataRefreshFinishListener {
        open fun onDataRefreshFinish() {

        }
    }


    private class WeakReferenceOnListChangedCallback<K : BindingViewHolder<ViewDataBinding>,
            T, BIND : ViewDataBinding> constructor(
        adapter: AutoNotifyAdapter<K, T, BIND>
    ) : OnListChangedCallback<ObservableList<T>>() {


        var adapterRef: WeakReference<AutoNotifyAdapter<K, T, BIND>> =
            WeakReference<AutoNotifyAdapter<K, T, BIND>>(adapter)

        override fun onChanged(sender: ObservableList<T>) {
            val adapter = adapterRef.get() ?: return

            if (!adapter.mNeedStopFlash) {
                adapter.notifyDataSetChanged()

                adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
            }


        }

        override fun onItemRangeChanged(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return
            if (!adapter.mNeedStopFlash) {
                adapter.notifyItemRangeChanged(positionStart, itemCount)

                adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
            }


        }

        override fun onItemRangeInserted(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return
            if (!adapter.mNeedStopFlash) {
                if (adapter.mIsNoData) {
                    adapter.mIsNoData = false
                    adapter.notifyDataSetChanged()
                }
                adapter.notifyItemRangeInserted(positionStart, itemCount)
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
                adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
            }

        }

        override fun onItemRangeMoved(
            sender: ObservableList<T>,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return

            if (!adapter.mNeedStopFlash) {
                for (i in 0 until itemCount) {
                    adapter.notifyItemMoved(fromPosition + i, toPosition + i)
                }
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
                adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
            }

        }

        override fun onItemRangeRemoved(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {

            val adapter = adapterRef.get() ?: return

            if (!adapter.mNeedStopFlash) {
                if (sender.size == 0) {
                    adapter.mIsNoData = true
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyItemRangeRemoved(positionStart, itemCount)
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                    adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
                }

            }


        }

    }


}