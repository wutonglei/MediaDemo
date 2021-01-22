package com.example.mediademo.voice.lib;
//

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;



/**
 * 已经成功合并了多个音频文件  美滋滋
 * https://www.cnblogs.com/guanxinjing/p/10969824.html  这个看着比较靠谱    成功了我日哟
 * 当前代码可以多开录音但是只能有一个文件 且关闭时全部关闭
 * Created by jiuman on 2019/12/13.
 */

public class AudioRecordManager {
    private AudioRecord mAudioRecord;
    String TAG="trh"+this.getClass().getSimpleName();
    private void initAudioRecord() {

        initMinBufferSize();
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                , 8000
                , AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT
                , mRecordBufferSize);
    }

    private Integer mRecordBufferSize;

    private void initMinBufferSize() {
        //获取每一帧的字节流大小
        mRecordBufferSize = AudioRecord.getMinBufferSize(8000
                , AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT);
    }

    private boolean mWhetherRecord;
    private File pcmFile;
    String path;
    Context context;


    public void startRecord(Context context, String parentFilePath,String fileName) {
        this.context = context;
        initAudioRecord();
        path = parentFilePath;
        pcmFile = new File(path, fileName);
        if (!pcmFile.getParentFile().exists())
            pcmFile.getParentFile().mkdirs();

        mWhetherRecord = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();//开始录制
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(pcmFile);
                    byte[] bytes = new byte[mRecordBufferSize];
                    while (mWhetherRecord) {
                        mAudioRecord.read(bytes, 0, bytes.length);//读取流
                        fileOutputStream.write(bytes);
                        fileOutputStream.flush();

                    }
                    Log.e(TAG, "run: 暂停录制");
                    mAudioRecord.stop();//停止录制
                    fileOutputStream.flush();
                    fileOutputStream.close();
//                    addHeadData(context);//添加音频头部信息并且转成wav格式
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mAudioRecord.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void stopRecord() {
        mWhetherRecord = false;
    }

    PcmToWavUtil pcmToWavUtil;

    private void addHeadData(Context context) {
        String fileName = "audioRecord0.pcm";
        pcmFile = new File(path, fileName);
        File handlerWavFile = new File(path, "audioRecord_handler.wav");

        pcmToWavUtil = new PcmToWavUtil(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        pcmToWavUtil.pcmToWav(pcmFile.toString(), handlerWavFile.toString());
    }

    private void addHeadData(Context context, File pcmFile) {
//        pcmFile = new File(path, "audioRecord1.pcm");
        File handlerWavFile = new File(path, "audioRecord_handlerxxxx.wav");

        pcmToWavUtil = new PcmToWavUtil(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        pcmToWavUtil.pcmToWav(pcmFile.toString(), handlerWavFile.toString());
    }


    public void release() {
        mWhetherRecord = false;
        mAudioRecord.release();
    }

    public void hebing(String parentPath) {

        File file3 = new File(path, "audio.pcm");
        byte[] data = new byte[AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)];
        try {
            File fileAll = new File(parentPath);
            if (!fileAll.exists())
                return;
            File[] files = fileAll.listFiles();

            FileOutputStream out = new FileOutputStream(file3);
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(file3.getName()))
                    continue;

                FileInputStream in = new FileInputStream(files[i]);
                while (in.read(data) != -1) {
                    out.write(data);
                    out.flush();
                }
                in.close();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addHeadData(context, file3);

    }

}

