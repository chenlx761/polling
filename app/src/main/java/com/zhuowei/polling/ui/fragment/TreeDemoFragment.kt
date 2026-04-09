package com.zhuowei.polling.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.chenming.common.base.BaseFragment
import com.chenming.httprequest.XLog
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.TreeNodeAdapter
import com.zhuowei.polling.bean.TreeNode
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.FragmentTreeDemoBinding
import com.zhuowei.polling.util.TreeDataGenerator

/**
 * 树形结构演示Fragment
 * 展示如何使用RecyclerView实现无限层级的树状图
 */
class TreeDemoFragment : BaseFragment<MainVm, FragmentTreeDemoBinding>() {

    private lateinit var treeAdapter: TreeNodeAdapter
    private var rootNodes: MutableList<TreeNode> = mutableListOf()

    companion object {
        fun newInstance(): TreeDemoFragment {
            return TreeDemoFragment()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_tree_demo
    }

    override fun initData() {
        // 初始化适配器
        treeAdapter = TreeNodeAdapter()

        // 设置RecyclerView
        mBinding!!.rvTree.layoutManager = LinearLayoutManager(requireContext())
        mBinding!!.rvTree.adapter = treeAdapter

        // 加载初始数据
        loadTreeData()
    }

    override fun initViewModel(): MainVm {
        return createViewModel(MainVm::class.java)
    }

    override fun setData() {
        updateStats()
    }

    override fun setListener() {
        // 节点点击监听
        treeAdapter.setOnNodeClickListener { node ->
            XLog.e("节点点击", "点击了: ${node.name}, 层级: ${node.level}")
        }

        // 全部展开按钮
        mBinding!!.btnExpandAll.setOnClickListener {
            expandAll()
        }

        // 全部折叠按钮
        mBinding!!.btnCollapseAll.setOnClickListener {
            collapseAll()
        }

        // 生成新数据按钮
        mBinding!!.btnGenerate.setOnClickListener {
            loadTreeData()
        }

        // 测试折叠页面按钮
        mBinding!!.btnTestCollapsible.setOnClickListener {
            com.zhuowei.polling.ui.activitys.CollapsiblePagerActivity.newInstance(requireContext())
        }
    }

    /**
     * 加载树形数据
     */
    private fun loadTreeData() {
        // 方式1: 生成随机树形数据
        rootNodes = TreeDataGenerator.generateTree(maxDepth = 5).toMutableList()

        // 方式2: 使用固定的组织架构树 (如需可切换)
        // rootNodes = TreeDataGenerator.generateSimpleOrgTree().toMutableList()

        // 设置数据到适配器
        treeAdapter.setData(rootNodes)

        // 更新统计信息
        updateStats()
    }

    /**
     * 更新统计信息
     */
    private fun updateStats() {
        val stats = TreeDataGenerator.getTreeStats(rootNodes)
        mBinding!!.tvTotalNodes.text = "总节点: ${stats.totalNodes}"
        mBinding!!.tvMaxDepth.text = "最大深度: ${stats.maxDepth}"
        mBinding!!.tvLeafNodes.text = "叶子节点: ${stats.leafNodes}"
    }

    /**
     * 全部展开
     */
    private fun expandAll() {
        rootNodes.forEach { it.setChildrenExpand(true) }
        treeAdapter.setData(rootNodes)
    }

    /**
     * 全部折叠
     */
    private fun collapseAll() {
        rootNodes.forEach {
            it.setChildrenExpand(false)
        }
        treeAdapter.setData(rootNodes)
    }
}