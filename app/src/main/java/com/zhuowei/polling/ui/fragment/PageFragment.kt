package com.zhuowei.polling.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chenming.common.base.BaseFragment
import com.chenming.common.base.empty.EmptyViewModel
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.FragmentPageBinding

/**
 * ViewPager中的子页面Fragment
 * 用于展示各Tab页的内容
 */
class PageFragment : BaseFragment<EmptyViewModel, FragmentPageBinding>() {

    private var pageTitle: String = ""
    private var pageContent: String = ""

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"
        private const val ARG_PAGE_NUM = "arg_page_num"

        fun newInstance(title: String, content: String, pageNum: Int): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_CONTENT, content)
            args.putInt(ARG_PAGE_NUM, pageNum)
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_page
    }

    override fun initData() {
        pageTitle = arguments?.getString(ARG_TITLE) ?: ""
        pageContent = arguments?.getString(ARG_CONTENT) ?: ""
        val pageNum = arguments?.getInt(ARG_PAGE_NUM, 0) ?: 0

        mBinding?.let { binding ->
            binding.tvPageTitle.text = pageTitle

            // 初始化模拟数据列表
            val itemList = generateItemList(pageNum)
            binding.rvContent.layoutManager = LinearLayoutManager(requireContext())
            binding.rvContent.adapter = PageItemAdapter(itemList)
        }
    }

    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
    }

    override fun setListener() {
    }

    /**
     * 生成模拟数据列表
     */
    private fun generateItemList(pageNum: Int): List<PageItem> {
        val titles = arrayOf("信息", "动态", "设置", "关于")
        val contents = arrayOf(
            "个人信息页面内容，可以展示头像、昵称、联系方式等信息",
            "最新动态页面内容，可以展示用户的最新活动记录",
            "系统设置页面内容，可以修改应用偏好设置",
            "关于页面内容，展示应用版本和版权信息"
        )

        val items = mutableListOf<PageItem>()
        for (i in 1..15) {
            items.add(
                PageItem(
                    title = "${titles[pageNum]}列表项 $i",
                    content = "${contents[pageNum]} - 第 $i 条记录"
                )
            )
        }
        return items
    }
}

/**
 * 页面数据项
 */
data class PageItem(
    val title: String,
    val content: String
)

/**
 * 页面列表适配器
 */
class PageItemAdapter(
    private val items: List<PageItem>
) : RecyclerView.Adapter<PageItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: android.widget.TextView = itemView.findViewById(R.id.tv_item_title)
        val tvContent: android.widget.TextView = itemView.findViewById(R.id.tv_item_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_page_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content
    }

    override fun getItemCount(): Int = items.size
}
