package com.chenming.common.dialog

import android.Manifest
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chenming.common.R
import com.permissionx.guolindev.dialog.RationaleDialog
import com.permissionx.guolindev.dialog.allSpecialPermissions
import java.util.*

/**
 * +----------------------------------------------------------------------
 * | com.runde
 * +----------------------------------------------------------------------
 * | 功能描述:
 * +----------------------------------------------------------------------
 * | 时　　间: 2021/10/8.
 * +----------------------------------------------------------------------
 * | 代码创建: chenmingdu
 * +----------------------------------------------------------------------
 */
class CustomPermissionDialog(
    context: Context,
    private val message: String,
    private val permissions: List<String>,
    private var positiveText: String,
    private var negativeText: String,
) :
    RationaleDialog(context, R.style.permission_tip_dialog) {

    private val permissionMap = mapOf(
        //新增扫描的信息
        "android.permission.BLUETOOTH_SCAN" to "android.permission-group.NEARBY_DEVICES",
        "android.permission.BLUETOOTH_ADVERTISE" to "android.permission-group.NEARBY_DEVICES",
        "android.permission.BLUETOOTH_CONNECT" to "android.permission-group.NEARBY_DEVICES",
        Manifest.permission.READ_CALENDAR to Manifest.permission_group.CALENDAR,
        Manifest.permission.WRITE_CALENDAR to Manifest.permission_group.CALENDAR,
        Manifest.permission.READ_CALL_LOG to Manifest.permission_group.CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG to Manifest.permission_group.CALL_LOG,
        "android.permission.PROCESS_OUTGOING_CALLS" to Manifest.permission_group.CALL_LOG,
        Manifest.permission.CAMERA to Manifest.permission_group.CAMERA,
        Manifest.permission.READ_CONTACTS to Manifest.permission_group.CONTACTS,
        Manifest.permission.WRITE_CONTACTS to Manifest.permission_group.CONTACTS,
        Manifest.permission.GET_ACCOUNTS to Manifest.permission_group.CONTACTS,
        Manifest.permission.ACCESS_FINE_LOCATION to Manifest.permission_group.LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION to Manifest.permission_group.LOCATION,
        "android.permission.ACCESS_BACKGROUND_LOCATION" to Manifest.permission_group.LOCATION,
        Manifest.permission.RECORD_AUDIO to Manifest.permission_group.MICROPHONE,
        Manifest.permission.READ_PHONE_STATE to Manifest.permission_group.PHONE,
        Manifest.permission.READ_PHONE_NUMBERS to Manifest.permission_group.PHONE,
        Manifest.permission.CALL_PHONE to Manifest.permission_group.PHONE,
        Manifest.permission.ANSWER_PHONE_CALLS to Manifest.permission_group.PHONE,
        Manifest.permission.ADD_VOICEMAIL to Manifest.permission_group.PHONE,
        Manifest.permission.USE_SIP to Manifest.permission_group.PHONE,
        Manifest.permission.ACCEPT_HANDOVER to Manifest.permission_group.PHONE,
        Manifest.permission.BODY_SENSORS to Manifest.permission_group.SENSORS,
        "android.permission.ACTIVITY_RECOGNITION" to "android.permission-group.ACTIVITY_RECOGNITION",
        Manifest.permission.SEND_SMS to Manifest.permission_group.SMS,
        Manifest.permission.RECEIVE_SMS to Manifest.permission_group.SMS,
        Manifest.permission.READ_SMS to Manifest.permission_group.SMS,
        Manifest.permission.RECEIVE_WAP_PUSH to Manifest.permission_group.SMS,
        Manifest.permission.RECEIVE_MMS to Manifest.permission_group.SMS,
        Manifest.permission.READ_EXTERNAL_STORAGE to Manifest.permission_group.STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE to Manifest.permission_group.STORAGE,
        "android.permission.ACCESS_MEDIA_LOCATION" to Manifest.permission_group.STORAGE
    )

    private val groupSet = HashSet<String>()
    private var permissionsLayout: LinearLayout? = null
    private var mSubTitle: TextView? = null
    private var mTitle: TextView? = null
    private var mSure: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_custom_permission)
        permissionsLayout = findViewById(R.id.permissionsLayout);
        mSubTitle = findViewById(R.id.tv_subTitle)
        mSubTitle?.text = message
        mTitle = findViewById(R.id.tv_tip_title_commodialog)
        mSure = findViewById(R.id.tv_sure_stolentip)
        addView()
    }


    override fun getPositiveButton(): View {
        val view = findViewById<TextView>(R.id.tv_sure_stolentip)
        view.text = positiveText
        return view
    }

    override fun getNegativeButton(): View? {
        val view = findViewById<TextView>(R.id.tv_cancle_stolentip)
        view.text = negativeText
        return view
    }

    override fun getPermissionsToRequest(): List<String> {
        return permissions
    }


    private fun addView() {
        for (permission in permissions) {
            val permissionGroup = permissionMap[permission]
            if ((permission in allSpecialPermissions && !groupSet.contains(permission))
                || (permissionGroup != null && !groupSet.contains(permissionGroup))
            ) {
                val view: View = LayoutInflater.from(context).inflate(
                    R.layout.permissions_item_view,
                    null, false
                )
                val bodyItem: TextView = view.findViewById(R.id.bodyItem)
                val permissions_icon: ImageView = view.findViewById(R.id.permissions_icon)
                when (permission) {
                    Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        bodyItem.text =
                            context.getString(com.permissionx.guolindev.R.string.permissionx_system_alert_window)
                        permissions_icon.setImageResource(com.permissionx.guolindev.R.drawable.permissionx_ic_alert)
                    }
                    Manifest.permission.WRITE_SETTINGS -> {
                        bodyItem.text =
                            context.getString(com.permissionx.guolindev.R.string.permissionx_write_settings)
                        permissions_icon.setImageResource(com.permissionx.guolindev.R.drawable.permissionx_ic_setting)
                    }
                    "android.permission.MANAGE_EXTERNAL_STORAGE" -> {
                        bodyItem.text =
                            context.getString(com.permissionx.guolindev.R.string.permissionx_manage_external_storage)
                        permissions_icon.setImageResource(com.permissionx.guolindev.R.drawable.permissionx_ic_storage)
                    }


                    Manifest.permission.BODY_SENSORS -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )

                        permissions_icon.setImageResource(R.drawable.permission_sensor)
                    }

                    //内存
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_storage)
                    }

                    //地址
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "android.permission.ACCESS_BACKGROUND_LOCATION" -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_location)
                    }

                    //蓝牙
                    "android.permission.BLUETOOTH_SCAN",
                    "android.permission.BLUETOOTH_ADVERTISE",
                    "android.permission.BLUETOOTH_CONNECT" -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_blue)
                    }

                    //录音
                    Manifest.permission.RECORD_AUDIO -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_audio)
                    }


                    //电话
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.ADD_VOICEMAIL,
                    Manifest.permission.USE_SIP,
                    Manifest.permission.ACCEPT_HANDOVER -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_phone)
                    }

                    //短信
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_WAP_PUSH,
                    Manifest.permission.RECEIVE_MMS -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_sms)
                    }

                    //相机
                    Manifest.permission.CAMERA -> {
                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )
                        permissions_icon.setImageResource(R.drawable.permission_camera)
                    }


                    else -> {

                        bodyItem.text =
                            context.getString(
                                context.packageManager.getPermissionGroupInfo(
                                    permissionGroup!!,
                                    0
                                ).labelRes
                            )

                        permissions_icon.setImageResource(
                            context.packageManager.getPermissionGroupInfo(
                                permissionGroup,
                                0
                            ).icon
                        )


                    }

                }


                permissionsLayout?.addView(view)
                groupSet.add(permissionGroup!!)
            }

        }
    }
}