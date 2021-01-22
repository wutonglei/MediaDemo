package com.example.mediademo.voice.lib;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by jiuman on 2019/12/13.
 */

public class MediaRecorderManager {
    private static MediaRecorderManager mediaRecorderManager = new MediaRecorderManager();

    public List<String> fileList = new ArrayList<>();

    public static MediaRecorderManager getInstance() {
        return mediaRecorderManager;
    }

    private MediaRecorderManager() {

    }

    MediaRecorder mMediaRecorder;
    String fileName;
    public String filePath;
    String TAG = "trh" + this.getClass().getSimpleName();

    public void startRecord(Context context, int index) {
//        String [] str={".m4a",".mp3",".wav"};
        finishRecord();
        // 开始录音
    /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
        /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
        /*
        https://blog.csdn.net/dodod2012/article/details/80474490
         * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
         * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
         */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样
        AAC属于有损压缩的格格式    AMR_NB编码声音的无视频纯声音
        * */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

//            fileName = "wutong" + System.currentTimeMillis() + str[index];
            fileName = "wutong" + System.currentTimeMillis() + ".wav";
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/audio/" + fileName;
            fileList.add(filePath);
            Log.i(TAG, "startRecord: filePath" + filePath);
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

        /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);

            mMediaRecorder.prepare();
        /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public void finishRecord() {

        if (mMediaRecorder != null)
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                filePath = "";
            } catch (RuntimeException e) {
                Log.e(TAG, e.toString());
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;

                File file = new File(filePath);
                if (file.exists())
                    file.delete();
                filePath = "";
            }
    }
}
