package com.chenming.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableList
import androidx.databinding.ObservableList.OnListChangedCallback
import androidx.recyclerview.widget.RecyclerView
import com.chenming.common.R
import com.chenming.common.listener.ClickListener
import java.lang.ref.WeakReference

/**
 * 监听数据变化和有空布局的adapter
 */
open abstract class AutoNotifyMultipleAdapter<T : RecyclerView.ViewHolder, K>() :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    protected  var mClickListener: ClickListener? = null
    private val ITEM_TYPE_EMPTY = 9999
    private var mIsNoData: Boolean = false
    protected lateinit var mDatas: ObservableList<K>
    protected lateinit var mContext: Context
    private lateinit var mCallback: WeakReferenceOnListChangedCallback<T, K>
    private var mOnDataRefreshFinishListener: OnDataRefreshFinishListener? = null


    public fun setOnDataRefreshFinishListener(onDataRefreshFinishListener: OnDataRefreshFinishListener) {
        this.mOnDataRefreshFinishListener = onDataRefreshFinishListener
    }

    protected fun getClickListener(): ClickListener? {
        return mClickListener
    }

    open fun setClickListener(listener: ClickListener) {
        this.mClickListener = listener
    }

    constructor(context: Context, datas: ObservableList<K>) : this() {
        this.mContext = context
        mDatas = datas
        //设置监听
        mCallback = WeakReferenceOnListChangedCallback(
            this
        )
        mDatas.addOnListChangedCallback(mCallback)
    }

    abstract fun onCreateOtherViewHolder(parent: ViewGroup, viewType: Int): T

    abstract fun onBindOtherViewHolder(holder: T, position: Int)

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

    open fun getEmptyView(): Int {
        return R.layout.adapter_empty
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_EMPTY) {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(getEmptyView(), parent, false)
            return EmptyViewHolder(view)
        }

        return onCreateOtherViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmptyViewHolder) {

        } else {
            onBindOtherViewHolder(holder as T, position)
        }
    }

    override fun getItemCount(): Int {
        if (mIsNoData) {
            return 1
        }
        return getOtherItemCount()
    }


    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
        }
    }

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


    private class WeakReferenceOnListChangedCallback<K : RecyclerView.ViewHolder, T> constructor(
        adapter: AutoNotifyMultipleAdapter<K, T>
    ) : OnListChangedCallback<ObservableList<T>>() {


        var adapterRef: WeakReference<AutoNotifyMultipleAdapter<K, T>> =
            WeakReference<AutoNotifyMultipleAdapter<K, T>>(adapter)

        override fun onChanged(sender: ObservableList<T>) {
            val adapter = adapterRef.get() ?: return
            adapter.notifyDataSetChanged()

            adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
        }

        override fun onItemRangeChanged(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return
            adapter.notifyItemRangeChanged(positionStart, itemCount)

            adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
        }

        override fun onItemRangeInserted(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return
            adapter.notifyItemRangeInserted(positionStart, itemCount)

            adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
        }

        override fun onItemRangeMoved(
            sender: ObservableList<T>,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return
            for (i in 0 until itemCount) {
                adapter.notifyItemMoved(fromPosition + i, toPosition + i)
            }
            adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
        }

        override fun onItemRangeRemoved(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            val adapter = adapterRef.get() ?: return
            adapter.notifyItemRangeRemoved(positionStart, itemCount)
            adapter.notifyItemRangeChanged(0,adapter.itemCount)
            adapter.mOnDataRefreshFinishListener?.onDataRefreshFinish()
        }

    }


}