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
        val level3Items: List<List<List<AreaPickerOption>>>
    )

    data class SelectedAreaResult(
        val displayName: String,
        val areaId: Int
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

    override fun getAreaPickerData(callBack: TicketDetailContract.GetAreaListCallBack?) {
        if (mAreaRows.isNotEmpty()) {
            callBack?.onSuccessful(buildAreaPickerDisplayData())
            return
        }
        mModel.getAreaList(
            object : BaseCallBack<BaseBean<AreaListBean>>(HttpConstants.GET_AREA_URL) {
                override fun onSuccessful(t: BaseBean<AreaListBean>?) {
                    mAreaRows = t?.data?.rows!!
                        .filter(::isAreaNodeValid)
                        .sortedByAreaOrder()
                    callBack?.onSuccessful(buildAreaPickerDisplayData())
                }
            })
    }

    // 根据 Picker 当前三列下标，得到最终选中的区域结果。
    // 输入框只显示末级名称，同时把末级对应的 orgId 一并返回给页面保存。
    fun buildSelectedAreaResult(
        pickerData: AreaPickerDisplayData,
        option1: Int,
        option2: Int,
        option3: Int
    ): SelectedAreaResult? {
        val level1Item = pickerData.level1Items.getOrNull(option1) ?: return null
        val level2Item =
            pickerData.level2Items.getOrNull(option1)?.getOrNull(option2)
        val level3Item =
            pickerData.level3Items.getOrNull(option1)?.getOrNull(option2)?.getOrNull(option3)
        val targetSelection =
            if (level3Item != null && !level3Item.isPlaceholder && level3Item.selection.name.isNotEmpty()) {
                level3Item.selection
            } else if (level2Item != null && !level2Item.isPlaceholder && level2Item.selection.name.isNotEmpty()) {
                level2Item.selection
            } else {
                level1Item.selection
            }
        return SelectedAreaResult(
            displayName = targetSelection.name,
            areaId = targetSelection.id
        )
    }

    // 将接口返回的扁平 rows 按 parentId 组装成 Picker 所需的三级结构。
    private fun buildAreaPickerDisplayData(): AreaPickerDisplayData {
        val childrenByParent = mAreaRows.groupBy { it.parentId }
        val level1Rows = childrenByParent[0].orEmpty().sortedByAreaOrder()
        val level1Items = mutableListOf<AreaPickerOption>()
        val level2Items = mutableListOf<List<AreaPickerOption>>()
        val level3Items = mutableListOf<List<List<AreaPickerOption>>>()

        level1Rows.forEach { level1Row ->
            val level2Rows = childrenByParent[level1Row.orgId].orEmpty().sortedByAreaOrder()
            level1Items.add(level1Row.toAreaPickerOption())
            if (level2Rows.isEmpty()) {
                val placeholderLevel2 = level1Row.toPlaceholderPickerOption()
                level2Items.add(listOf(placeholderLevel2))
                level3Items.add(listOf(listOf(level1Row.toPlaceholderPickerOption())))
            } else {
                level2Items.add(level2Rows.map { it.toAreaPickerOption() })
                level3Items.add(
                    level2Rows.map { level2Row ->
                        val level3Rows =
                            childrenByParent[level2Row.orgId].orEmpty().sortedByAreaOrder()
                        if (level3Rows.isEmpty()) {
                            listOf(level2Row.toPlaceholderPickerOption())
                        } else {
                            level3Rows.map { it.toAreaPickerOption() }
                        }
                    }
                )
            }
        }

        return AreaPickerDisplayData(
            level1Items = level1Items,
            level2Items = level2Items,
            level3Items = level3Items
        )
    }

    // 过滤掉接口里无效的区域节点，避免空名称或非法 id 进入 Picker。
    private fun isAreaNodeValid(area: AreaListBean.RowsDTO): Boolean {
        val areaName = area.orgName?.trim().orEmpty()
        return area.orgId > 0 && areaName.isNotEmpty()
    }

    // 将接口节点转换成 Picker 可直接消费的选项模型。
    private fun AreaListBean.RowsDTO.toAreaPickerOption(): AreaPickerOption {
        return AreaPickerOption(
            selection = AreaSelection(
                id = orgId,
                name = orgName?.trim().orEmpty()
            )
        )
    }

    // 两级数据没有下级节点时，给第三级补一个空白占位项，
    // 保持三级联动组件的数据结构完整，但界面上不展示额外文案。
    private fun AreaListBean.RowsDTO.toPlaceholderPickerOption(): AreaPickerOption {
        return AreaPickerOption(
            selection = AreaSelection(
                id = orgId,
                name = ""
            ),
            isPlaceholder = true
        )
    }

    // 按后台配置的排序号优先排序，排序号相同时再按 orgId 兜底，保证顺序稳定。
    private fun List<AreaListBean.RowsDTO>.sortedByAreaOrder(): List<AreaListBean.RowsDTO> {
        return sortedWith(compareBy<AreaListBean.RowsDTO> { it.orderNum }.thenBy { it.orgId })
    }
}
