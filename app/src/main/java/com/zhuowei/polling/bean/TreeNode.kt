package com.zhuowei.polling.bean

import java.util.UUID

/**
 * 树节点数据模型
 * 支持无限层级的树状结构
 */
data class TreeNode(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var icon: Int? = null,
    var isExpand: Boolean = false,
    var level: Int = 0,
    var isLastChild: Boolean = false,
    var isFirstChild: Boolean = false,
    var parent: TreeNode? = null,
    val children: MutableList<TreeNode> = mutableListOf()
) {
    val isLeaf: Boolean
        get() = children.isEmpty()

    val hasVisibleChildren: Boolean
        get() = children.isNotEmpty() && isExpand

    fun addChild(node: TreeNode) {
        node.parent = this
        node.level = this.level + 1
        children.add(node)
    }

    fun setChildrenExpand(expand: Boolean) {
        if (!isLeaf) {
            isExpand = expand
            children.forEach { it.setChildrenExpand(expand) }
        }
    }

    fun getAllVisibleNodes(): List<TreeNode> {
        val result = mutableListOf<TreeNode>()
        result.add(this)
        if (isExpand) {
            children.forEach { child ->
                result.addAll(child.getAllVisibleNodes())
            }
        }
        return result
    }

    fun refreshPositionInfo() {
        val childList = parent?.children ?: return
        childList.forEachIndexed { index, node ->
            node.isFirstChild = index == 0
            node.isLastChild = index == childList.size - 1
            node.refreshPositionInfo()
        }
    }
}
