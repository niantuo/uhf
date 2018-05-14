@file:Suppress("DEPRECATION")

package cn.gygxzc.uhf.uhf.util

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import cn.gygxzc.uhf.R
import java.util.*

object UhfUtils {


    private lateinit var sp: SoundPool
    private lateinit var am: AudioManager
    private var soundMap: MutableMap<Int, Int> = mutableMapOf()


    //初始化声音池
    fun initSoundPool(context: Context) {
        sp = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
        soundMap = HashMap()
        soundMap.put(1, sp.load(context, R.raw.msg, 1))
        am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    //播放声音池声音
    fun play(sound: Int, number: Int) {
        //返回当前AudioManager对象的音量值
        val audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val soundId = soundMap[sound]
        if (soundId != null) {
            sp.play(soundId, //播放的音乐Id
                    audioCurrentVolume, //左声道音量
                    audioCurrentVolume, //右声道音量
                    1, //优先级，0为最低
                    number, //循环次数，0无不循环，-1无永远循环
                    1f)//回放速度，值在0.5-2.0之间，1为正常速度
        }
    }


}
