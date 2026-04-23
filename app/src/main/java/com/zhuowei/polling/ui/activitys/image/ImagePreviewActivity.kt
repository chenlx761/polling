package com.zhuowei.polling.ui.activitys.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.chenming.common.base.empty.EmptyViewModel
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.ImagePreviewAdapter
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.databinding.ActivityImagePreviewBinding

class ImagePreviewActivity : MyBaseActivity<EmptyViewModel, ActivityImagePreviewBinding>() {

    companion object {
        private const val EXTRA_IMAGE_PATHS = "extra_image_paths"
        private const val EXTRA_CURRENT_POSITION = "extra_current_position"

        fun newInstance(context: Context, imagePaths: List<String>, currentPosition: Int = 0) {
            val intent = Intent(context, ImagePreviewActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_IMAGE_PATHS, ArrayList(imagePaths))
                putExtra(EXTRA_CURRENT_POSITION, currentPosition)
            }
            context.startActivity(intent)
        }
    }

    private var imagePaths: MutableList<String> = mutableListOf()
    private var currentPosition: Int = 0
    private var adapter: ImagePreviewAdapter? = null

    override fun getLayoutId(): Int = R.layout.activity_image_preview

    override fun initData() {
        imagePaths = intent.getStringArrayListExtra(EXTRA_IMAGE_PATHS) ?: mutableListOf()
        currentPosition = intent.getIntExtra(EXTRA_CURRENT_POSITION, 0)
    }

    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setObserveListener() {}

    override fun setListener() {
        mBinding!!.myTitleBar.setLeftLayoutClickListener {
            finish()
        }

        mBinding!!.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                updateIndicator()
            }
        })


    }

    override fun setData() {
        if (imagePaths.isEmpty()) {
            finish()
            return
        }

        mBinding!!.tvTitle.text = "${currentPosition + 1}/${imagePaths.size}"

        adapter = ImagePreviewAdapter(imagePaths)
        mBinding!!.viewPager.adapter = adapter

        if (currentPosition in imagePaths.indices) {
            mBinding!!.viewPager.setCurrentItem(currentPosition, false)
        }

        updateIndicator()
    }

    private fun updateIndicator() {
        mBinding!!.tvTitle.text = "${currentPosition + 1}/${imagePaths.size}"
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}
