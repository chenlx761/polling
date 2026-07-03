package com.zhuowei.polling.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chenming.common.utils.ToastUtil
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.TreeNodeAdapter
import com.zhuowei.polling.base.MyBaseDialog
import com.zhuowei.polling.bean.TreeNode

class AreaTreePickerDialog(
    context: Context,
    private val rootNodes: List<TreeNode>,
    currentSelectedAreaId: Int?,
    private val onConfirm: (TreeNode) -> Unit
) : MyBaseDialog(context) {

    private lateinit var tvSelectedArea: TextView
    private lateinit var tvCancel: TextView
    private lateinit var tvConfirm: TextView
    private lateinit var rvAreaTree: RecyclerView
    private val treeAdapter = TreeNodeAdapter()
    private var pendingSelectedNode: TreeNode? =
        findAndExpandSelectedNode(rootNodes, currentSelectedAreaId?.toString())

    override fun initEvent() {
        treeAdapter.setOnNodeClickListener { node ->
            if (!node.isLeaf) {
                return@setOnNodeClickListener
            }
            pendingSelectedNode = node
            treeAdapter.setSelectedNode(node.id)
            updateAreaSelectionText(node)
        }
        tvCancel.setOnClickListener {
            dismiss()
        }
        tvConfirm.setOnClickListener {
            val selectedNode = pendingSelectedNode
            if (selectedNode == null || !selectedNode.isLeaf) {
                ToastUtil.showShortToast(context.getString(R.string.area_required_hint))
                return@setOnClickListener
            }
            dismiss()
            onConfirm(selectedNode)
        }
    }

    override fun setData() {
        treeAdapter.setShowNodeIcon(false)
        val leafSelectedNode = pendingSelectedNode?.takeIf { it.isLeaf }
        pendingSelectedNode = leafSelectedNode
        treeAdapter.setSelectedNode(leafSelectedNode?.id)
        treeAdapter.setData(rootNodes)
        updateAreaSelectionText(leafSelectedNode)
    }

    override fun initView() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tvSelectedArea = findViewById(R.id.tv_selected_area)
        tvCancel = findViewById(R.id.tv_cancel)
        tvConfirm = findViewById(R.id.tv_confirm)
        rvAreaTree = findViewById(R.id.rv_area_tree)
        rvAreaTree.layoutManager = LinearLayoutManager(context)
        rvAreaTree.adapter = treeAdapter
    }

    override fun setDialogLayout(): Int {
        return R.layout.dialog_area_tree_picker
    }

    override fun setDialogWidth(): Int {
        return (context.resources.displayMetrics.widthPixels * 0.92f).toInt()
    }

    override fun setDialogHeight(): Int {
        return (context.resources.displayMetrics.heightPixels * 0.75f).toInt()
    }

    private fun updateAreaSelectionText(selectedNode: TreeNode?) {
        tvSelectedArea.text = selectedNode?.name ?: context.getString(R.string.area_select_none)
    }

    private fun findAndExpandSelectedNode(
        nodes: List<TreeNode>,
        targetNodeId: String?
    ): TreeNode? {
        if (targetNodeId.isNullOrEmpty()) {
            return null
        }
        nodes.forEach { node ->
            val matchedNode = findAndExpandSelectedNode(node, targetNodeId)
            if (matchedNode != null) {
                return matchedNode
            }
        }
        return null
    }

    private fun findAndExpandSelectedNode(
        node: TreeNode,
        targetNodeId: String
    ): TreeNode? {
        if (node.id == targetNodeId) {
            return node
        }
        node.children.forEach { child ->
            val matchedNode = findAndExpandSelectedNode(child, targetNodeId)
            if (matchedNode != null) {
                node.isExpand = true
                return matchedNode
            }
        }
        return null
    }
}
