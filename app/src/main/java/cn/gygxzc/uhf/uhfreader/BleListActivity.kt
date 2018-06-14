package cn.gygxzc.uhf.uhfreader

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.ble.IBleModel
import cn.gygxzc.uhf.kotlin.flatEvent
import cn.tonyandmoney.anko.adapter.KAdapter
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout

/**
 * @author niantuo
 * @createdTime 2018/5/14 15:11
 *
 *  蓝牙连接列表
 */
class BleListActivity : AppCompatActivity(), AnkoLogger {

    private val mLoading: MaterialDialog by lazy { createLoadingDialog() }
    private val mAdapter = KAdapter<BluetoothDevice>()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private val mBleModel: IBleModel by lazy { IBleModel.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            swipeRefreshLayout {
                mSwipeRefreshLayout = this
                onRefresh { startScan() }
                setColorSchemeResources(R.color.colorAccent)
                recyclerView {
                    mAdapter.bindToRecyclerView(this)
                    layoutManager = LinearLayoutManager(ctx)
                }
            }
        }
        mLoading.show()

        mAdapter.setItemView {
            verticalLayout {
                textView {
                    textSize = 14f
                    id = R.id.ble_name
                    leftPadding = dip(10)
                    rightPadding = dip(10)
                }.lparams(matchParent, wrapContent)
                textView {
                    id = R.id.ble_address
                    textSize = 13f
                    padding = dip(10)
                }.lparams(matchParent, wrapContent)
            }
        }
                .setDataBind { holder, bluetoothDevice ->
                    holder.setText(R.id.ble_name, bluetoothDevice.name)
                    holder.setText(R.id.ble_address, bluetoothDevice.address)
                }

        mAdapter.setOnItemClickListener { _, _, position ->
            val entity = mAdapter.getItem(position) ?: return@setOnItemClickListener
            connect(entity)
        }
        mAdapter.setNewData(mutableListOf())
        startScan()
    }


    private fun connect(device: BluetoothDevice) {
        mLoading.show()
        BleInstance.BLE.connect(device)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatEvent()
                .doOnComplete {
                    mLoading.hide()
                    toast("连接成功")
                    finish()
                }
                .doOnError {
                    mLoading.hide()
                    error("蓝牙连接失败->", it)
                    longToast("连接失败")
                }
                .subscribe()
    }


    private fun startScan() {
        if (!mBleModel.isBleEnable()) {
            MaterialDialog.Builder(this)
                    .title("提示")
                    .content("蓝牙未开启或者不可用，去设置？")
                    .positiveText("设置")
                    .onPositive { _, _ ->
                        mBleModel.bleEnable(12)
                    }
            return
        }

        mAdapter.setNewData(mBleModel.getBondedDevices().toMutableList())


        mBleModel.startScan()
                .filter { !mAdapter.data.contains(it) }
                .subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatEvent()
                .doOnNext { mAdapter.addData(it) }
                .doOnComplete {
                    mLoading.hide()
                    mSwipeRefreshLayout.isRefreshing = false
                    toast("扫描完成")
                }
                .doOnError {
                    mLoading.hide()
                    error("蓝牙扫描->$it")
                    longToast(it.message?:"扫描错误，已停止")
                    mSwipeRefreshLayout.isRefreshing = false
                }
                .subscribe()

    }

    private fun createLoadingDialog(): MaterialDialog {
        return MaterialDialog.Builder(this)
                .title("正在加载")
                .progress(true, 0)
                .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoading.dismiss()
        mBleModel.cancelScan()

    }

}