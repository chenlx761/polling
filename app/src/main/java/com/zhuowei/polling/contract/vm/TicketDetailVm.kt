package com.zhuowei.polling.contract.vm

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.bean.AreaListBean
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.TicketDetailContract
import com.zhuowei.polling.contract.model.TicketDetailModel
import java.util.Objects

class TicketDetailVm : BaseViewModel<TicketDetailContract.ITicketDetailModel>(),
    TicketDetailContract.ITicketDetailVm {

    val mUpdateFinish: MutableLiveData<Boolean> = MutableLiveData()
    val mDetail: MutableLiveData<TicketListBean.RowsDTO> = MutableLiveData()
    private var mAreaRows: List<AreaListBean.RowsDTO> = emptyList()

    data class AreaSelection(
        val id: Int,
        val name: String
    )

    data class AreaPickerOption(
        val selection: AreaSelection,
        val isPlaceholder: Boolean = false
    )

    data class AreaPickerDisplayData(
        val level1Items: List<AreaPickerOption>,
        val level2Items: List<List<AreaPickerOption>>,
        val level3Items: List<List<List<AreaPickerOption>>>,
        val selectedLevel1: Int,
        val selectedLevel2: Int,
        val selectedLevel3: Int
    )


    override fun getModel(): TicketDetailContract.ITicketDetailModel? {
        return TicketDetailModel()
    }

    override fun getTicketDetail(ticketId: String?) {
        mStartLoadingDialog.postValue(true)
        mModel.getTicketDetail(
            ticketId,
            object :
                BaseCallBack<BaseBean<TicketListBean.RowsDTO>>(HttpConstants.GET_TICKET_DETAIL_URL) {
                override fun onSuccessful(t: BaseBean<TicketListBean.RowsDTO>?) {
                    mStartLoadingDialog.postValue(false)
                    if (t != null) {
                        val imageList = ArrayList<UploadFileResult>()
                        //设置图片列表
                        if (!TextUtils.isEmpty(t.data.images)) {
                            val split = t.data.images.split(",").filter { it.isNotEmpty() }
                            split.forEach {
                                val uploadFileResult = UploadFileResult()
                                uploadFileResult.filePath = HttpConstants.BASE_URL + it
                                imageList.add(uploadFileResult)
                            }
                        }
                        t.data.serverPhotosList = imageList

                        val governmentSplit = t.data.governmentImages
                        governmentSplit.forEach { it ->
                            it.filePath = HttpConstants.BASE_URL + it.filePath
                        }

                        mDetail.postValue(t.data)
                    }
                }


            })
    }

    override fun postTicketDetail(bean: TicketListBean.RowsDTO?, isCreateMode: Boolean) {
        mStartLoadingDialog.postValue(true)
        mModel.postTicketDetail(
            bean,
            isCreateMode,
            object : BaseCallBack<BaseBean<Objects>>(
                if (isCreateMode) HttpConstants.ADD_TICKET_DETAIL_URL
                else HttpConstants.EDIT_TICKET_DETAIL_URL
            ) {


                override fun onSuccessful(t: BaseBean<Objects>?) {
                    mStartLoadingDialog.postValue(false)
                    mUpdateFinish.postValue(true)
                }

            })
    }

    override fun getAreaPickerData(
        currentAreaText: String?,
        callBack: TicketDetailContract.GetAreaListCallBack?
    ) {
        if (mAreaRows.isNotEmpty()) {
            callBack?.onSuccessful(buildAreaPickerDisplayData(currentAreaText))
            return
        }
        mModel.getAreaList(
            object : BaseCallBack<BaseBean<AreaListBean>>(HttpConstants.GET_AREA_URL) {
                override fun onSuccessful(t: BaseBean<AreaListBean>?) {
                    mAreaRows = t?.data?.rows!!
                        .filter(::isAreaNodeValid)
                        .sortedByAreaOrder()
                    callBack?.onSuccessful(buildAreaPickerDisplayData(currentAreaText))
                }
            })
    }

    fun buildSelectedAreaText(
        pickerData: AreaPickerDisplayData,
        option1: Int,
        option2: Int,
        option3: Int
    ): String? {
        val level1Item = pickerData.level1Items.getOrNull(option1) ?: return null
        val level2Item =
            pickerData.level2Items.getOrNull(option1)?.getOrNull(option2) ?: return null
        val level3Item =
            pickerData.level3Items.getOrNull(option1)?.getOrNull(option2)?.getOrNull(option3)
        return buildList {
            add(level1Item.selection.name)
            add(level2Item.selection.name)
            if (level3Item != null && !level3Item.isPlaceholder && level3Item.selection.name.isNotEmpty()) {
                add(level3Item.selection.name)
            }
        }.joinToString("/")
    }

    private fun buildAreaPickerDisplayData(currentAreaText: String?): AreaPickerDisplayData {
        val childrenByParent = mAreaRows.groupBy { it.parentId }
        val level1Rows = childrenByParent[0].orEmpty().sortedByAreaOrder()
        val level1Items = mutableListOf<AreaPickerOption>()
        val level2Items = mutableListOf<List<AreaPickerOption>>()
        val level3Items = mutableListOf<List<List<AreaPickerOption>>>()

        level1Rows.forEach { level1Row ->
            val level2Rows = childrenByParent[level1Row.orgId].orEmpty().sortedByAreaOrder()
            if (level2Rows.isEmpty()) return@forEach

            level1Items.add(level1Row.toAreaPickerOption())
            level2Items.add(level2Rows.map { it.toAreaPickerOption() })
            level3Items.add(
                level2Rows.map { level2Row ->
                    val level3Rows = childrenByParent[level2Row.orgId].orEmpty().sortedByAreaOrder()
                    if (level3Rows.isEmpty()) {
                        listOf(level2Row.toPlaceholderPickerOption())
                    } else {
                        level3Rows.map { it.toAreaPickerOption() }
                    }
                }
            )
        }

        val pickerData = AreaPickerDisplayData(
            level1Items = level1Items,
            level2Items = level2Items,
            level3Items = level3Items,
            selectedLevel1 = 0,
            selectedLevel2 = 0,
            selectedLevel3 = 0
        )
        val (selectedLevel1, selectedLevel2, selectedLevel3) =
            resolveAreaPickerSelection(currentAreaText, pickerData)
        return pickerData.copy(
            selectedLevel1 = selectedLevel1,
            selectedLevel2 = selectedLevel2,
            selectedLevel3 = selectedLevel3
        )
    }

    private fun resolveAreaPickerSelection(
        currentAreaText: String?,
        pickerData: AreaPickerDisplayData
    ): Triple<Int, Int, Int> {
        val currentNames = currentAreaText.orEmpty()
            .split("/")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (currentNames.size !in 2..3) {
            return Triple(0, 0, 0)
        }
        val level1Index = pickerData.level1Items.indexOfFirst {
            it.selection.name == currentNames[0]
        }.takeIf { it >= 0 } ?: return Triple(0, 0, 0)
        val level2Index = pickerData.level2Items.getOrNull(level1Index)?.indexOfFirst {
            it.selection.name == currentNames[1]
        }?.takeIf { it >= 0 } ?: return Triple(level1Index, 0, 0)
        if (currentNames.size == 2) {
            return Triple(level1Index, level2Index, 0)
        }
        val level3Index =
            pickerData.level3Items.getOrNull(level1Index)?.getOrNull(level2Index)?.indexOfFirst {
                it.selection.name == currentNames[2]
            }?.takeIf { it >= 0 } ?: return Triple(level1Index, level2Index, 0)
        return Triple(level1Index, level2Index, level3Index)
    }

    private fun isAreaNodeValid(area: AreaListBean.RowsDTO): Boolean {
        val areaName = area.orgName?.trim().orEmpty()
        return area.orgId > 0 && areaName.isNotEmpty()
    }

    private fun AreaListBean.RowsDTO.toAreaPickerOption(): AreaPickerOption {
        return AreaPickerOption(
            selection = AreaSelection(
                id = orgId,
                name = orgName?.trim().orEmpty()
            )
        )
    }

    private fun AreaListBean.RowsDTO.toPlaceholderPickerOption(): AreaPickerOption {
        return AreaPickerOption(
            selection = AreaSelection(
                id = orgId,
                name = ""
            ),
            isPlaceholder = true
        )
    }

    private fun List<AreaListBean.RowsDTO>.sortedByAreaOrder(): List<AreaListBean.RowsDTO> {
        return sortedWith(compareBy<AreaListBean.RowsDTO> { it.orderNum }.thenBy { it.orgId })
    }
}
