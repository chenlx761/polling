package com.zhuowei.polling.util

import com.zhuowei.polling.bean.TreeNode
import kotlin.random.Random

/**
 * 树形数据生成器
 * 用于生成无限层级的模拟树状数据
 */
object TreeDataGenerator {

    private val departmentNames = listOf(
        "总公司", "技术部", "研发部", "产品部", "设计部",
        "市场部", "销售部", "财务部", "人力资源部", "运营部",
        "前端组", "后端组", "移动端组", "测试组", "运维组",
        "iOS开发组", "Android开发组", "Web开发组", "Java组", "Python组",
        "高级工程师", "中级工程师", "初级工程师", "实习生",
        "项目经理", "技术经理", "架构师", "测试工程师",
        "UI设计师", "交互设计师", "视觉设计师"
    )

    private val personNames = listOf(
        "张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十",
        "钱一", "高二", "林三", "黄四", "杨五", "朱六", "秦七", "许八",
        "何九", "吕十", "施一", "张二", "孔三", "曹四", "严五", "华六",
        "金七", "魏八", "陶九", "姜十", "戚一", "谢二", "邹三"
    )

    /**
     * 生成指定结构的树形数据
     * @param maxDepth 最大深度
     * @param nodesPerLevel 每层节点数量范围
     */
    fun generateTree(maxDepth: Int = 5, nodesPerLevel: IntRange = 2..5): List<TreeNode> {
        val rootNodes = mutableListOf<TreeNode>()

        // 生成2-4个顶级节点
        val rootCount = Random.nextInt(2, 5)
        repeat(rootCount) { i ->
            val root = TreeNode(name = "${departmentNames.random()} ${i + 1}")
            root.level = 0
            generateChildren(root, maxDepth, nodesPerLevel, 1)
            rootNodes.add(root)
        }

        // 刷新节点位置信息
        rootNodes.forEach { it.refreshPositionInfo() }
        return rootNodes
    }

    private fun generateChildren(
        parent: TreeNode,
        maxDepth: Int,
        nodesPerLevel: IntRange,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return

        // 随机决定是否继续生成子节点
        if (Random.nextFloat() > 0.7f && currentDepth > 2) return

        val childCount = if (currentDepth == maxDepth) {
            Random.nextInt(1, 4)
        } else {
            Random.nextInt(nodesPerLevel.first, nodesPerLevel.last + 1)
        }

        repeat(childCount) { i ->
            val isLeafLevel = currentDepth == maxDepth || Random.nextFloat() > 0.6f

            val childName = if (isLeafLevel) {
                // 叶子节点使用人名
                "${personNames.random()} (${currentDepth}级)"
            } else {
                // 非叶子节点使用部门名
                "${departmentNames.random()} ${currentDepth}-${i + 1}"
            }

            val child = TreeNode(name = childName)
            child.isExpand = currentDepth < 3  // 前3层默认展开
            parent.addChild(child)

            if (!isLeafLevel) {
                generateChildren(child, maxDepth, nodesPerLevel, currentDepth + 1)
            }
        }
    }

    /**
     * 生成简单的组织架构树
     */
    fun generateSimpleOrgTree(): List<TreeNode> {
        val company = TreeNode(name = "深圳总部")
        company.level = 0

        val tech = TreeNode(name = "技术中心")
        val hr = TreeNode(name = "人力资源")
        val sales = TreeNode(name = "销售部")

        company.addChild(tech)
        company.addChild(hr)
        company.addChild(sales)

        // 技术中心子节点
        val frontend = TreeNode(name = "前端开发部")
        val backend = TreeNode(name = "后端开发部")
        val mobile = TreeNode(name = "移动端开发部")
        tech.addChild(frontend)
        tech.addChild(backend)
        tech.addChild(mobile)

        // 前端子节点
        frontend.addChild(TreeNode(name = "张三 (前端)"))
        frontend.addChild(TreeNode(name = "李四 (前端)"))
        frontend.addChild(TreeNode(name = "王五 (前端)"))

        // 后端子节点
        backend.addChild(TreeNode(name = "Java组"))
        backend.addChild(TreeNode(name = "Python组"))
        backend.addChild(TreeNode(name = "Go组"))

        // Java组子节点
        backend.children.find { it.name == "Java组" }?.apply {
            addChild(TreeNode(name = "赵六 (Java)"))
            addChild(TreeNode(name = "孙七 (Java)"))
        }

        // 移动端子节点
        mobile.addChild(TreeNode(name = "iOS组"))
        mobile.addChild(TreeNode(name = "Android组"))
        mobile.children.find { it.name == "iOS组" }?.apply {
            addChild(TreeNode(name = "周八 (iOS)"))
        }
        mobile.children.find { it.name == "Android组" }?.apply {
            addChild(TreeNode(name = "吴九 (Android)"))
            addChild(TreeNode(name = "郑十 (Android)"))
        }

        // 人力资源子节点
        hr.addChild(TreeNode(name = "招聘组"))
        hr.addChild(TreeNode(name = "培训组"))
        hr.children.find { it.name == "招聘组" }?.apply {
            addChild(TreeNode(name = "钱一 (HR)"))
        }

        // 销售部子节点
        sales.addChild(TreeNode(name = "华南区"))
        sales.addChild(TreeNode(name = "华北区"))
        sales.children.find { it.name == "华南区" }?.apply {
            addChild(TreeNode(name = "高二 (销售)"))
        }

        company.refreshPositionInfo()
        return listOf(company)
    }

    /**
     * 获取树的统计信息
     */
    fun getTreeStats(nodes: List<TreeNode>): TreeStats {
        var totalNodes = 0
        var maxDepth = 0
        var leafNodes = 0

        fun traverse(node: TreeNode) {
            totalNodes++
            if (node.level > maxDepth) maxDepth = node.level
            if (node.isLeaf) leafNodes++
            node.children.forEach { traverse(it) }
        }

        nodes.forEach { traverse(it) }

        return TreeStats(
            totalNodes = totalNodes,
            maxDepth = maxDepth,
            leafNodes = leafNodes,
            branchNodes = totalNodes - leafNodes
        )
    }

    data class TreeStats(
        val totalNodes: Int,
        val maxDepth: Int,
        val leafNodes: Int,
        val branchNodes: Int
    )
}
