package com.zhuowei.polling.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    private var rootNodes: List<TreeNode> = emptyList()
    private var onNodeClickListener: ((TreeNode) -> Unit)? = null
    private var selectedNodeId: String? = null
    private var showNodeIcon: Boolean = true

    fun setOnNodeClickListener(listener: (TreeNode) -> Unit) {
        this.onNodeClickListener = listener
    }

    fun setSelectedNode(nodeId: String?) {
        selectedNodeId = nodeId
        notifyDataSetChanged()
    }

    fun setShowNodeIcon(show: Boolean) {
        showNodeIcon = show
    }

    fun setData(rootNodes: List<TreeNode>) {
        this.rootNodes = rootNodes
        refreshVisibleNodes()
    }

    fun collapseNode(node: TreeNode) {
        if (node.isLeaf) return

        node.isExpand = false
        refreshVisibleNodes()
    }

    fun expandNode(node: TreeNode) {
        if (node.isLeaf) return

        node.isExpand = true
        refreshVisibleNodes()
    }

    private fun refreshVisibleNodes() {
        allNodes.clear()
        rootNodes.forEach { node ->
            allNodes.addAll(node.getAllVisibleNodes())
        }
        notifyDataSetChanged()
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
                    if (node.isExpand) R.mipmap.shrink else R.mipmap.spread
                )
            }

            if (showNodeIcon) {
                binding.ivNodeIcon.visibility = View.VISIBLE
                binding.ivNodeIcon.setImageResource(node.icon ?: R.drawable.ic_user)
            } else {
                binding.ivNodeIcon.visibility = View.GONE
            }

            // 连接线控制
            binding.viewLine.visibility = if (node.level > 0) View.VISIBLE else View.GONE
            binding.viewFirstLine.visibility = if (node.level > 0 && node.isLastChild) {
                View.GONE
            } else if (node.level > 0) View.VISIBLE else View.GONE

            val context = binding.root.context
            val isSelected = selectedNodeId == node.id
            binding.root.setBackgroundResource(
                if (isSelected) R.drawable.bg_f5f9ff_radius_5 else android.R.color.transparent
            )
            binding.tvNodeName.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.primary else R.color.black
                )
            )

            // 选中逻辑交给外部决定，当前适配器只负责把点击事件透出。
            binding.root.setOnClickListener {
                onNodeClickListener?.invoke(node)
            }

            // 展开/收起单独交给箭头按钮，避免父节点点击时被展开逻辑打断选中状态。
            binding.ivExpand.setOnClickListener {
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
