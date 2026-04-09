package com.zhuowei.polling.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zhuowei.polling.R
import com.zhuowei.polling.bean.TreeNode
import com.zhuowei.polling.databinding.ItemTreeNodeBinding

/**
 * 树形RecyclerView适配器
 * 支持展开/折叠、子节点缩进、连接线绘制
 */
class TreeNodeAdapter(
    private var allNodes: MutableList<TreeNode> = mutableListOf()
) : RecyclerView.Adapter<TreeNodeAdapter.ViewHolder>() {

    private var onNodeClickListener: ((TreeNode) -> Unit)? = null

    fun setOnNodeClickListener(listener: (TreeNode) -> Unit) {
        this.onNodeClickListener = listener
    }

    fun setData(rootNodes: List<TreeNode>) {
        allNodes.clear()

        rootNodes.forEach { node ->
            allNodes.addAll(node.getAllVisibleNodes())
        }
        notifyDataSetChanged()
    }

    fun collapseNode(node: TreeNode) {
        if (node.isLeaf) return

        node.isExpand = false
        val position = allNodes.indexOf(node)
        if (position < 0) return

        val startIndex = position + 1
        val removeCount = collectDescendants(node, startIndex)
        if (removeCount > 0) {
            allNodes.subList(startIndex, startIndex + removeCount).clear()
            notifyItemRangeRemoved(startIndex, removeCount)
        }
        notifyItemChanged(position)
    }

    fun expandNode(node: TreeNode) {
        if (node.isLeaf) return

        node.isExpand = true
        val position = allNodes.indexOf(node)
        if (position < 0) return

        val startIndex = position + 1
        val newNodes = node.getAllVisibleNodes().drop(1)
        allNodes.addAll(startIndex, newNodes)
        notifyItemRangeInserted(startIndex, newNodes.size)
        notifyItemChanged(position)
    }

    private fun collectDescendants(node: TreeNode, startIndex: Int): Int {
        var count = 0
        for (i in startIndex until allNodes.size) {
            val currentNode = allNodes[i]
            if (currentNode.level <= node.level) break
            count++
        }
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTreeNodeBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(allNodes[position])
    }

    override fun getItemCount(): Int = allNodes.size

    inner class ViewHolder(private val binding: ItemTreeNodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(node: TreeNode) {
            binding.tvNodeName.text = node.name

            // 计算左边距: level * 缩进宽度
            val indentWidth = binding.root.context.resources
                .getDimensionPixelSize(R.dimen.tree_node_indent)
            val marginStart = node.level * indentWidth
            binding.viewIndent.layoutParams = (binding.viewIndent.layoutParams).apply {
                width = marginStart
            }

            // 处理展开/折叠图标
            if (node.isLeaf) {
                binding.ivExpand.visibility = View.INVISIBLE
            } else {
                binding.ivExpand.visibility = View.VISIBLE
                binding.ivExpand.setImageResource(
                    if (node.isExpand) R.drawable.ic_collapse else R.drawable.ic_expand
                )
            }

            // 连接线控制
            binding.viewLine.visibility = if (node.level > 0) View.VISIBLE else View.GONE
            binding.viewFirstLine.visibility = if (node.level > 0 && node.isLastChild) {
                View.GONE
            } else if (node.level > 0) View.VISIBLE else View.GONE

            // 点击事件
            binding.root.setOnClickListener {
                onNodeClickListener?.invoke(node)
                if (node.isLeaf) return@setOnClickListener

                if (node.isExpand) {
                    collapseNode(node)
                } else {
                    expandNode(node)
                }
            }
        }
    }
}
