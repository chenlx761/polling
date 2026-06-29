package com.chenming.common.base.widget
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import com.blankj.utilcode.util.ClickUtils
import com.chenming.common.R


/**
 * Created by wuqx14 on 2021/1/4.
 */
class CommonDialogContent : BaseDialogFragment() {

    private var cdcTitleRl: RelativeLayout? = null
    private var cdcTitleTv: TextView? = null
    private var cdcContentRl: RelativeLayout? = null
    private var cdcContentTv: TextView? = null
    private var cdcBottomRl: RelativeLayout? = null
    private var cdcBottomPositiveTv: TextView? = null
    private var cdcBottomNegativeTv: TextView? = null

    fun init(
        context: Context?, title: CharSequence?, content: CharSequence?,
        positiveBtnAction: Pair<CharSequence?, View.OnClickListener>?,
        negativeBtnAction: Pair<CharSequence?, View.OnClickListener>?
    ): CommonDialogContent? {
        super.init(context!!, object : DialogLayoutCallback {
            override fun bindTheme(): Int {
                return R.style.CommonContentDialogStyle
            }

            override fun bindLayout(): Int {
                return R.layout.common_dialog_content
            }

            override fun initView(dialog: BaseDialogFragment?, contentView: View?) {
                cdcTitleRl = contentView!!.findViewById(R.id.cdcTitleRl)
                cdcTitleTv = contentView.findViewById(R.id.cdcTitleTv)
                cdcContentRl = contentView.findViewById(R.id.cdcContentRl)
                cdcContentTv = contentView.findViewById(R.id.cdcContentTv)
                cdcBottomRl = contentView.findViewById(R.id.cdcBottomRl)
                cdcBottomPositiveTv = contentView.findViewById(R.id.cdcBottomPositiveTv)
                cdcBottomNegativeTv = contentView.findViewById(R.id.cdcBottomNegativeTv)
                if (TextUtils.isEmpty(title)) {
                    cdcTitleRl!!.visibility = View.GONE
                } else {
                    cdcTitleTv!!.text = title
                }
                if (TextUtils.isEmpty(content)) {
                    cdcContentRl!!.visibility = View.GONE
                } else {
                    cdcContentTv!!.text = content
                }
                if (positiveBtnAction == null && negativeBtnAction == null) {
                    cdcBottomRl!!.visibility = View.GONE
                } else {
                    if (positiveBtnAction != null) {
                        ClickUtils.applyPressedBgDark(cdcBottomPositiveTv)
                        cdcBottomPositiveTv!!.text = positiveBtnAction.first
                        cdcBottomPositiveTv!!.setOnClickListener { v ->
                            dismiss()
                            positiveBtnAction.second.onClick(v)
                        }
                    }
                    if (negativeBtnAction != null) {
                        ClickUtils.applyPressedBgDark(cdcBottomNegativeTv)
                        cdcBottomNegativeTv!!.text = negativeBtnAction.first
                        cdcBottomNegativeTv!!.setOnClickListener { v ->
                            dismiss()
                            negativeBtnAction.second.onClick(v)
                        }
                    }
                }
            }

            override fun setWindowStyle(window: Window?) {

            }

            override fun onCancel(dialog: BaseDialogFragment?) {

            }

            override fun onDismiss(dialog: BaseDialogFragment?) {

            }
        })
        return this
    }
}