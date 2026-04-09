package com.zhuowei.polling.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.ActivityCollapsiblePagerBinding
import com.zhuowei.polling.ui.fragment.PageFragment

/**
 * 可折叠头部 + ViewPager2 滑动页面
 *
 * 实现功能:
 * 1. CoordinatorLayout + AppBarLayout 实现顶部可折叠效果
 * 2. CollapsingToolbarLayout 实现渐变折叠效果
 * 3. TabLayout + ViewPager2 实现多页面滑动切换
 * 4. ViewPager2与AppBarLayout联动滚动
 */
class CollapsiblePagerActivity : MyBaseActivity<MainVm, ActivityCollapsiblePagerBinding>() {

    private val tabTitles = arrayOf("信息", "动态", "设置", "关于")
    private val fragments = mutableListOf<Fragment>()

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, CollapsiblePagerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_collapsible_pager
    }

    override fun setListener() {
    }

    override fun initViewModel(): MainVm {
        return createViewModel(MainVm::class.java)
    }

    override fun initData() {
    }

    override fun initView(savedInstanceState: Bundle?) {
        setupToolbar()
        setupAppBarListener()
        setupViewPager()
        setupTabLayout()
    }

    /**
     * 配置Toolbar
     */
    private fun setupToolbar() {

        // 设置用户信息
        mBinding.ivAvatar.setImageResource(R.mipmap.ic_launcher)
        mBinding.tvUserName.text = "张三"
        mBinding.tvUserDesc.text = "Android 开发工程师"
    }

    /**
     * 监听AppBarLayout滚动状态，只在完全折叠时显示标题
     */
    private fun setupAppBarListener() {
//        mBinding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            val totalScrollRange = appBarLayout.totalScrollRange
//            // 当完全折叠时（垂直偏移等于总滚动范围）
//            if (kotlin.math.abs(verticalOffset) >= totalScrollRange) {
//                mBinding.collapsingToolbar.title = ""
//            } else {
//                mBinding.collapsingToolbar.title = ""
//            }
//        })
    }

    /**
     * 配置ViewPager2
     */
    private fun setupViewPager() {
        // 创建Fragment列表
        tabTitles.forEachIndexed { index, title ->
            fragments.add(PageFragment.newInstance(title, "", index))
        }

        // 设置ViewPager2适配器
        mBinding.viewPager.adapter = CollapsiblePagerAdapter(this, fragments, tabTitles)

        // 禁止ViewPager2预加载
        mBinding.viewPager.offscreenPageLimit = 2

        // 设置页面切换动画
        mBinding.viewPager.setPageTransformer(DepthPageTransformer())
    }

    /**
     * 配置TabLayout
     */
    private fun setupTabLayout() {
        // 使用TabLayoutMediator关联TabLayout和ViewPager2
        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun setObserveListener() {
    }

    override fun setData() {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

/**
 * ViewPager2适配器
 */
class CollapsiblePagerAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>,
    private val titles: Array<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    @NonNull
    override fun getItemId(position: Int): Long {
        return fragments[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragments.any { it.hashCode().toLong() == itemId }
    }
}

/**
 * 深度页面切换动画
 */
class DepthPageTransformer : ViewPager2.PageTransformer {
    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // 页面滑出左侧
                    alpha = 0f
                }
                position <= 0 -> { // 当前页面正在滑动
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // 下一个页面正在进入
                    alpha = 1 - position
                    translationX = pageWidth * -position
                    translationZ = -1f
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - kotlin.math.abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // 页面滑出右侧
                    alpha = 0f
                }
            }
        }
    }
}
