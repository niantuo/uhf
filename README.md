# uhf
玖锐的UHF 1000 系列的读写器设备

## 需要的依赖

- RxJava

        implementation "io.reactivex.rxjava2:rxjava:2.1.12"
        implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'


## 更新日志

### 2018/5、21
修复在无蓝牙适配器上闪退的问题，添加判断蓝牙适配器是否可用的方法

### 2018/6/14
1、添加全局的广播监听器，监听蓝牙连接状态
2、修复二次扫描蓝牙直接完成的问题
3、修复蓝牙连接状态不准确问题