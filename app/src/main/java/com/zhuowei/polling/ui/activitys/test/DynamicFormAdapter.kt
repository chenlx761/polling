package com.zhuowei.polling.ui.activitys.test

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.ItemFormDateBinding
import com.zhuowei.polling.databinding.ItemFormFileAttachmentBinding
import com.zhuowei.polling.databinding.ItemFormInputBinding
import com.zhuowei.polling.databinding.ItemFormImageAttachmentBinding
import com.zhuowei.polling.databinding.ItemFormSelectBinding
import com.zhuowei.polling.databinding.ItemFormSubmitBinding
import com.zhuowei.polling.databinding.ItemFormSwitchBinding
import com.zhuowei.polling.databinding.ItemFormTextareaBinding

class DynamicFormAdapter(
    private val items: List<FormItem>,
    private val onAction: (FormAdapterAction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position].key) {
            FormItem.KEY_INPUT -> VIEW_TYPE_INPUT
            FormItem.KEY_SELECT -> VIEW_TYPE_SELECT
            FormItem.KEY_MULTI_SELECT -> VIEW_TYPE_MULTI_SELECT
            FormItem.KEY_LOCATION -> VIEW_TYPE_LOCATION
            FormItem.KEY_DATE -> VIEW_TYPE_DATE
            FormItem.KEY_SWITCH -> VIEW_TYPE_SWITCH
            FormItem.KEY_TEXTAREA -> VIEW_TYPE_TEXTAREA
            FormItem.KEY_IMAGE_PICKER -> VIEW_TYPE_IMAGE_ATTACHMENT
            FormItem.KEY_FILE_PICKER -> VIEW_TYPE_FILE_ATTACHMENT
            FormItem.KEY_SUBMIT -> VIEW_TYPE_SUBMIT
            else -> error("Unsupported key: ${items[position].key}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_INPUT -> InputViewHolder(
                ItemFormInputBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_SELECT -> SelectViewHolder(
                ItemFormSelectBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_MULTI_SELECT -> MultiSelectViewHolder(
                ItemFormSelectBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_LOCATION -> LocationViewHolder(
                ItemFormSelectBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_DATE -> DateViewHolder(
                ItemFormDateBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_SWITCH -> SwitchViewHolder(
                ItemFormSwitchBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_TEXTAREA -> TextAreaViewHolder(
                ItemFormTextareaBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_IMAGE_ATTACHMENT -> ImageAttachmentViewHolder(
                ItemFormImageAttachmentBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_FILE_ATTACHMENT -> FileAttachmentViewHolder(
                ItemFormFileAttachmentBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_SUBMIT -> SubmitViewHolder(
                ItemFormSubmitBinding.inflate(inflater, parent, false)
            )

            else -> error("Unsupported viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (item.key) {
            FormItem.KEY_INPUT -> (holder as InputViewHolder).bind(item)
            FormItem.KEY_SELECT -> (holder as SelectViewHolder).bind(item, onAction)
            FormItem.KEY_MULTI_SELECT -> (holder as MultiSelectViewHolder).bind(item, onAction)
            FormItem.KEY_LOCATION -> (holder as LocationViewHolder).bind(item, onAction)
            FormItem.KEY_DATE -> (holder as DateViewHolder).bind(item, onAction)
            FormItem.KEY_SWITCH -> (holder as SwitchViewHolder).bind(item)
            FormItem.KEY_TEXTAREA -> (holder as TextAreaViewHolder).bind(item)
            FormItem.KEY_IMAGE_PICKER -> (holder as ImageAttachmentViewHolder).bind(item, onAction)
            FormItem.KEY_FILE_PICKER -> (holder as FileAttachmentViewHolder).bind(item, onAction)
            FormItem.KEY_SUBMIT -> (holder as SubmitViewHolder).bind(item, onAction)
        }
    }

    inner class InputViewHolder(
        private val binding: ItemFormInputBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem) {
            binding.tvLabel.text = buildLabel(item.label, item.required)
            binding.etValue.hint = item.hint
            binding.etValue.inputType = item.inputType
            if (binding.etValue.text?.toString().orEmpty() != item.value) {
                binding.etValue.setText(item.value)
                binding.etValue.setSelection(binding.etValue.text?.length ?: 0)
            }

            val oldWatcher = binding.etValue.tag as? TextWatcher
            if (oldWatcher != null) {
                binding.etValue.removeTextChangedListener(oldWatcher)
            }
            val watcher = SimpleTextWatcher { item.value = it }
            binding.etValue.addTextChangedListener(watcher)
            binding.etValue.tag = watcher
        }
    }

    inner class SelectViewHolder(
        private val binding: ItemFormSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            val actionCallback = onAction
            binding.tvLabel.text = buildLabel(item.label, item.required)
            val hasValue = item.value.isNotBlank()
            binding.tvValue.text = if (hasValue) item.value else "请选择"
            binding.tvValue.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (hasValue) R.color.gray_700 else R.color.gray_600
                )
            )
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionCallback(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    inner class DateViewHolder(
        private val binding: ItemFormDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            val actionCallback = onAction
            binding.tvLabel.text = buildLabel(item.label, item.required)
            val hasValue = item.value.isNotBlank()
            binding.tvValue.text = if (hasValue) item.value else "请选择日期"
            binding.tvValue.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (hasValue) R.color.gray_700 else R.color.gray_600
                )
            )
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionCallback(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    inner class LocationViewHolder(
        private val binding: ItemFormSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            val actionCallback = onAction
            binding.tvLabel.text = buildLabel(item.label, item.required)
            val hasValue = item.value.isNotBlank()
            binding.tvValue.text = if (hasValue) item.value else "点击获取定位"
            binding.tvValue.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (hasValue) R.color.gray_700 else R.color.gray_600
                )
            )
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionCallback(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    inner class MultiSelectViewHolder(
        private val binding: ItemFormSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            val actionCallback = onAction
            binding.tvLabel.text = buildLabel(item.label, item.required)
            val hasValue = item.selectedOptions.isNotEmpty()
            binding.tvValue.text = if (hasValue) {
                item.selectedOptions.joinToString("、")
            } else {
                "请选择，可多选"
            }
            binding.tvValue.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (hasValue) R.color.gray_700 else R.color.gray_600
                )
            )
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionCallback(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    inner class SwitchViewHolder(
        private val binding: ItemFormSwitchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem) {
            binding.tvLabel.text = item.label
            binding.tvDescription.text = item.description
            binding.tvDescription.isVisible = item.description.isNotBlank()
            binding.switchValue.setOnCheckedChangeListener(null)
            binding.switchValue.isChecked = item.checked
            binding.switchValue.setOnCheckedChangeListener { _, isChecked ->
                item.checked = isChecked
            }
        }
    }

    inner class TextAreaViewHolder(
        private val binding: ItemFormTextareaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem) {
            binding.tvLabel.text = buildLabel(item.label, item.required)
            binding.etValue.hint = item.hint
            if (binding.etValue.text?.toString().orEmpty() != item.value) {
                binding.etValue.setText(item.value)
                binding.etValue.setSelection(binding.etValue.text?.length ?: 0)
            }

            val oldWatcher = binding.etValue.tag as? TextWatcher
            if (oldWatcher != null) {
                binding.etValue.removeTextChangedListener(oldWatcher)
            }
            val watcher = SimpleTextWatcher { item.value = it }
            binding.etValue.addTextChangedListener(watcher)
            binding.etValue.tag = watcher
        }
    }

    inner class SubmitViewHolder(
        private val binding: ItemFormSubmitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            val actionCallback = onAction
            binding.btnSubmit.text = item.buttonText
            binding.btnSubmit.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionCallback(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    inner class ImageAttachmentViewHolder(
        private val binding: ItemFormImageAttachmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            binding.tvLabel.text = buildLabel(item.label, item.required)
            binding.tvCount.text = "已选择 ${item.selectedAttachments.size}/${item.maxCount}"
            binding.tvEmpty.text = if (item.selectedAttachments.isEmpty()) "暂未选择图片" else ""
            binding.tvEmpty.isVisible = item.selectedAttachments.isEmpty()
            binding.rvAttachmentList.isVisible = item.selectedAttachments.isNotEmpty()
            val canAddMore = item.selectedAttachments.size < item.maxCount
            binding.btnAdd.isEnabled = canAddMore
            binding.btnAdd.alpha = if (canAddMore) 1f else 0.6f

            if (binding.rvAttachmentList.adapter == null) {
                binding.rvAttachmentList.layoutManager = GridLayoutManager(binding.root.context, 3)
                binding.rvAttachmentList.adapter = FormAttachmentAdapter(
                    itemKey = FormItem.KEY_IMAGE_PICKER,
                    onPreviewClick = { attachmentPosition ->
                        val currentPosition = adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            onAction(
                                FormAdapterAction(
                                    item = item,
                                    itemPosition = currentPosition,
                                    type = FormActionType.ATTACHMENT_PREVIEW,
                                    attachmentPosition = attachmentPosition
                                )
                            )
                        }
                    },
                    onDeleteClick = { attachmentPosition ->
                        val currentPosition = adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            onAction(
                                FormAdapterAction(
                                    item = item,
                                    itemPosition = currentPosition,
                                    type = FormActionType.ATTACHMENT_DELETE,
                                    attachmentPosition = attachmentPosition
                                )
                            )
                        }
                    }
                )
            }
            (binding.rvAttachmentList.adapter as? FormAttachmentAdapter)
                ?.submitList(item.selectedAttachments)
            binding.btnAdd.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onAction(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    inner class FileAttachmentViewHolder(
        private val binding: ItemFormFileAttachmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormItem, onAction: (FormAdapterAction) -> Unit) {
            binding.tvLabel.text = buildLabel(item.label, item.required)
            binding.tvCount.text = "已选择 ${item.selectedAttachments.size}/${item.maxCount}"
            binding.tvEmpty.text = if (item.selectedAttachments.isEmpty()) "暂未选择文件" else ""
            binding.tvEmpty.isVisible = item.selectedAttachments.isEmpty()
            binding.rvAttachmentList.isVisible = item.selectedAttachments.isNotEmpty()
            val canAddMore = item.selectedAttachments.size < item.maxCount
            binding.btnAdd.isEnabled = canAddMore
            binding.btnAdd.alpha = if (canAddMore) 1f else 0.6f

            if (binding.rvAttachmentList.adapter == null) {
                binding.rvAttachmentList.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rvAttachmentList.adapter = FormAttachmentAdapter(
                    itemKey = FormItem.KEY_FILE_PICKER,
                    onPreviewClick = {},
                    onDeleteClick = { attachmentPosition ->
                        val currentPosition = adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            onAction(
                                FormAdapterAction(
                                    item = item,
                                    itemPosition = currentPosition,
                                    type = FormActionType.ATTACHMENT_DELETE,
                                    attachmentPosition = attachmentPosition
                                )
                            )
                        }
                    }
                )
            }
            (binding.rvAttachmentList.adapter as? FormAttachmentAdapter)
                ?.submitList(item.selectedAttachments)
            binding.btnAdd.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onAction(FormAdapterAction(item, currentPosition, FormActionType.ITEM_CLICK))
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_INPUT = 1
        private const val VIEW_TYPE_SELECT = 2
        private const val VIEW_TYPE_MULTI_SELECT = 3
        private const val VIEW_TYPE_LOCATION = 4
        private const val VIEW_TYPE_DATE = 5
        private const val VIEW_TYPE_SWITCH = 6
        private const val VIEW_TYPE_TEXTAREA = 7
        private const val VIEW_TYPE_SUBMIT = 8
        private const val VIEW_TYPE_IMAGE_ATTACHMENT = 9
        private const val VIEW_TYPE_FILE_ATTACHMENT = 10

        private fun buildLabel(label: String, required: Boolean): String {
            return if (required) "$label *" else label
        }
    }
}

data class FormAdapterAction(
    val item: FormItem,
    val itemPosition: Int,
    val type: String,
    val attachmentPosition: Int = -1
)

object FormActionType {
    const val ITEM_CLICK = "item_click"
    const val ATTACHMENT_PREVIEW = "attachment_preview"
    const val ATTACHMENT_DELETE = "attachment_delete"
}

private class SimpleTextWatcher(
    private val onTextChanged: (String) -> Unit
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        onTextChanged(s?.toString().orEmpty())
    }
}
