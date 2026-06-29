package com.chenming.common.dialog

import android.content.Context
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.chenming.common.R


class PickerUtils {

    companion object {

        private var mInstance: PickerUtils? = null
            get() {
                if (field == null) {
                    field = PickerUtils()
                }
                return field
            }

        @JvmStatic
        @Synchronized
        fun getInstance(): PickerUtils {
            return mInstance!!
        }

    }

    fun showTimePicker(context: Context, listener: OnTimeSelectListener) {
        //时间选择器
        val pvTime = TimePickerBuilder(
            context,
            listener
        ).setType(booleanArrayOf(true, true, true, true, true, false)).build()
        pvTime.show()
    }


    interface OptionPickerListener {
        fun onOptionSelect(position: Int)
    }

    interface OptionItemIndexListener {
        fun onOptionSelect(index: Int)
    }


    fun showOptionsPicker(
        context: Context,
        items1: List<String>,
        listener: OptionPickerListener?
    ) {
        //条件选择器
        val pvOptions: OptionsPickerView<String> = OptionsPickerBuilder(
            context
        ) { options1, options2, options3, v ->
            listener?.onOptionSelect(options1)
        }
            .setCancelColor(context.resources.getColor(R.color.text_color666))
            .setSubmitColor(context.resources.getColor(R.color.colorPrimary))
            .build()
        pvOptions.setPicker(items1)
        pvOptions.show()
    }

    fun showOptionsPicker2(
        context: Context,
        items1: List<String>,
        listener: OptionPickerListener?
    ) {
        //条件选择器
        val pvOptions: MyOptionsPickerView<String> = MyOptionsPickerBuilder(
            context
        ) { options1, options2, options3, v ->
            listener?.onOptionSelect(options1)
        }
            .setCancelColor(context.resources.getColor(R.color.text_color666))
            .setSubmitColor(context.resources.getColor(R.color.colorPrimary))
            .build()
        pvOptions.setPicker(items1)
        pvOptions.show()
    }

    /**
     * 这个按确认不会自动关闭的
     */
    fun showIndexOptionsPicker(
        context: Context,
        items1: List<String>,
        listener: OptionPickerListener?
    ): MyOptionsPickerView<String> {
        //条件选择器
        val pvOptions: MyOptionsPickerView<String> = MyOptionsPickerBuilder(
            context
        ) { options1, options2, options3, v ->
            listener?.onOptionSelect(options1)
        }
            .setCancelColor(context.resources.getColor(R.color.text_color666))
            .setSubmitColor(context.resources.getColor(R.color.colorPrimary))
            .build()
        pvOptions.setPicker(items1)
        pvOptions.show()
        return pvOptions
    }


}