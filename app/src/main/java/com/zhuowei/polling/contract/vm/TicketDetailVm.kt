package com.zhuowei.polling.contract.vm

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.bean.AreaListBean
import com.zhuowei.polling.bean.TreeNode
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

    data class AreaTreeDisplayData(
        val rootNodes: List<TreeNode>
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

    override fun getAreaTreeData(callBack: TicketDetailContract.GetAreaListCallBack?) {
        if (mAreaRows.isNotEmpty()) {
            callBack?.onSuccessful(buildAreaTreeDisplayData())
            return
        }
        mModel.getAreaList(
            object : BaseCallBack<BaseBean<AreaListBean>>(HttpConstants.GET_AREA_URL) {
                override fun onSuccessful(t: BaseBean<AreaListBean>?) {
                    mAreaRows = t?.data?.rows
                        .orEmpty()
                        .filter(::isAreaNodeValid)
                        .sortedByAreaOrder()
                    callBack?.onSuccessful(buildAreaTreeDisplayData())
                }
            })
    }

    // 树形弹窗点击任意节点后，都以该节点自身作为最终提交的区域。
    fun buildSelectedAreaResult(selectedNode: TreeNode?): SelectedAreaResult? {
        val targetNode = selectedNode ?: return null
        val areaId = targetNode.id.toIntOrNull() ?: return null
        if (targetNode.name.isEmpty()) {
            return null
        }
        return SelectedAreaResult(
            displayName = targetNode.name,
            areaId = areaId
        )
    }

    // 将接口返回的扁平 rows 按 parentId 组装成任意层级的树结构。
    private fun buildAreaTreeDisplayData(): AreaTreeDisplayData {
        val childrenByParent = mAreaRows.groupBy { it.parentId }
        val rowById = mAreaRows.associateBy { it.orgId }
        val rootRows = resolveAreaRootRows(rowById)
        val rootNodes = rootRows.map { rootRow ->
            buildAreaTreeNode(
                row = rootRow,
                childrenByParent = childrenByParent
            ).apply {
                isExpand = true
            }
        }
        rootNodes.forEach { it.refreshPositionInfo() }
        return AreaTreeDisplayData(rootNodes = rootNodes)
    }

    // 过滤掉接口里无效的区域节点，避免空名称或非法 id 进入 Picker。
    private fun isAreaNodeValid(area: AreaListBean.RowsDTO): Boolean {
        val areaName = area.orgName?.trim().orEmpty()
        return area.orgId > 0 && areaName.isNotEmpty()
    }

    // 从扁平区域列表里找出树的第一层节点。
    // 正常情况下，根节点满足两种特征之一：
    // 1. parentId <= 0，说明它本身就是顶层；
    // 2. parentId 在当前数据集中找不到对应父节点，说明后端没有把它的父级一起返回。
    // 如果这两种方式都找不到根节点，就退回到 ancestors 兜底：
    // 取 ancestors 链路最短的一批节点，视为当前数据里的最上层节点。
    private fun resolveAreaRootRows(
        rowById: Map<Int, AreaListBean.RowsDTO>
    ): List<AreaListBean.RowsDTO> {
        val rootRows = mAreaRows.filter { area ->
            area.parentId <= 0 || !rowById.containsKey(area.parentId)
        }.sortedByAreaOrder()
        //  if (rootRows.isNotEmpty()) {
        return rootRows
        //}
//        // ancestors 记录的是祖先 id 链，链路越短，层级越靠上。
//        // 这里先求出当前列表里最短的祖先深度，再把这批节点作为根节点返回。
//        val minAncestorDepth = mAreaRows.minOfOrNull { area ->
//            area.ancestors
//                ?.split(",")
//                .orEmpty()
//                .mapNotNull { it.trim().toIntOrNull() }
//                .size
//        } ?: 0
//        return mAreaRows.filter { area ->
//            area.ancestors
//                ?.split(",")
//                .orEmpty()
//                .mapNotNull { it.trim().toIntOrNull() }
//                .size == minAncestorDepth
//        }.sortedByAreaOrder()
    }

    // 递归构建树节点：
    // 先把当前 row 转成 TreeNode，再根据 orgId 找它的直属子节点，
    // 对每个子节点继续递归，直到某个节点没有下级为止。
    // childrenByParent 是提前按 parentId 分组后的映射，查子节点时不需要每次全表遍历。
    private fun buildAreaTreeNode(
        row: AreaListBean.RowsDTO,
        childrenByParent: Map<Int, List<AreaListBean.RowsDTO>>
    ): TreeNode {
        val node = TreeNode(
            id = row.orgId.toString(),
            name = row.orgName?.trim().orEmpty()
        )
        // 当前节点的 orgId，会成为下一级节点的 parentId。
        childrenByParent[row.orgId]
            .orEmpty()
            .sortedByAreaOrder()
            .forEach { childRow ->
                // addChild 内部会顺带补 parent、level 等树结构信息。
                node.addChild(buildAreaTreeNode(childRow, childrenByParent))
            }
        return node
    }

    // 按后台配置的排序号优先排序，排序号相同时再按 orgId 兜底，保证顺序稳定。
    private fun List<AreaListBean.RowsDTO>.sortedByAreaOrder(): List<AreaListBean.RowsDTO> {
        return sortedWith(compareBy<AreaListBean.RowsDTO> { it.orderNum }.thenBy { it.orgId })
    }
}
