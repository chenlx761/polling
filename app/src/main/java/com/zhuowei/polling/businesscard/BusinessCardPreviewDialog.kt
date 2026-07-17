package com.zhuowei.polling.businesscard

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.DialogBusinessCardPreviewBinding

class BusinessCardPreviewDialog : DialogFragment() {

    companion object {
        const val TAG = "BusinessCardPreviewDialog"
        private const val ARG_TEMPLATE_JSON = "arg_template_json"

        fun newInstance(templateJson: String): BusinessCardPreviewDialog {
            return BusinessCardPreviewDialog().apply {
                arguments = Bundle().apply { putString(ARG_TEMPLATE_JSON, templateJson) }
            }
        }
    }

    private var binding: DialogBusinessCardPreviewBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val state = BusinessCardStateCodec.decode(
            requireArguments().getString(ARG_TEMPLATE_JSON).orEmpty()
        )
        val dialogBinding = DialogBusinessCardPreviewBinding.inflate(layoutInflater)
        binding = dialogBinding
        dialogBinding.previewCanvas.setEditingEnabled(false)
        dialogBinding.previewCanvas.setState(state)
        updateCanvasRatio(dialogBinding, state.canvas.orientation)
        dialogBinding.btnClosePreview.setOnClickListener { dismiss() }

        return Dialog(requireContext(), R.style.ImagePreviewTheme).apply {
            setContentView(dialogBinding.root)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun updateCanvasRatio(
        binding: DialogBusinessCardPreviewBinding,
        orientation: BusinessCardOrientation
    ) {
        val params = binding.previewCanvasContainer.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = if (orientation == BusinessCardOrientation.PORTRAIT) "3:5" else "5:3"
        binding.previewCanvasContainer.layoutParams = params
    }
}
