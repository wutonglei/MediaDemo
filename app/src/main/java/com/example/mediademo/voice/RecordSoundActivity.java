package com.example.mediademo.voice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediademo.R;
import com.example.mediademo.voice.lib.AudioMerge;
import com.example.mediademo.voice.lib.AudioRecordManager;
import com.example.mediademo.voice.lib.MediaRecorderManager;
import com.example.mediademo.voice.lib.RecordService;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordSoundActivity extends AppCompatActivity  {
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.btn_mr_start)
    Button btnMrStart;
    @BindView(R.id.btn_mr_finish)
    Button btnMrFinish;
    @BindView(R.id.tv2)
    TextView tv2;
    @BindView(R.id.btn_a_start)
    Button btnAStart;
    @BindView(R.id.btn_a_finish)
    Button btnAFinish;

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE = 103;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView(R.id.btn_window_start)
    Button btnWindowStart;
    @BindView(R.id.btn_window_finish)
    Button btnWindowFinish;
    @BindView(R.id.btn_merge_wav)
    Button btnMergeWav;
    @BindView(R.id.btn_merge_mp3)
    Button btnMergeMp3;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;
    Context context;
    String pcmFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sound);
        ButterKnife.bind(this);
        context = this;
        /**
         * 存放音频的路径
         */
        pcmFilePath= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/pcm";
        startBtn = (Button) findViewById(R.id.btn_window_start);
        startBtn.setEnabled(false);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordService.isRunning()) {
                    recordService.stopRecord();
                    startBtn.setText(R.string.start_record);
                } else {
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                }
            }
        });
        if (ContextCompat.checkSelfPermission(RecordSoundActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(RecordSoundActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }

        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        audioRecordManager = new AudioRecordManager();
    }

    @OnClick({R.id.btn_merge_wav, R.id.btn_merge_mp3, R.id.btn_merge_pcm})
    public void onMergeClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_merge_wav:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String fileName = Thread.currentThread().getName() + "wutong.wav";
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/audio/" + fileName;

                        AudioMerge audioMerge = new AudioMerge();
                        try {
                            audioMerge.addWav(MediaRecorderManager.getInstance().fileList.get(0), MediaRecorderManager.getInstance().fileList.get(1), filePath);
                            audioMerge.updateFileHead(MediaRecorderManager.getInstance().fileList.get(0), false);
                            audioMerge.updateFileHead(MediaRecorderManager.getInstance().fileList.get(1), false);
                            audioMerge.updateFileHead(filePath, true);//头部合成
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(recordService, "" + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
                break;
            case R.id.btn_merge_mp3:
                saveRePlayData(0);

                break;
            case R.id.btn_merge_pcm:

                audioRecordManager.hebing(pcmFilePath );
                break;
        }
    }
    String TAG="trh"+this.getClass().getSimpleName();

    private void saveRePlayData(int type) {
        new AsyncTask<Void, Void, String>() {
            protected void onPreExecute() {

            }

            @Override
            protected String doInBackground(Void... params) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/merge/";
                String complete = "";
                File audioFile;
                /** 合并所有的录音文件*/
                File fileAll = new File(filePath);
                File[] files = fileAll.listFiles();
                complete =filePath + "audio.mp3";
                audioFile = new File(complete);

                FileOutputStream fileOutputStream = null;
                FileInputStream fileInputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(audioFile);
                    Log.i(TAG, "doInBackground: " + files.length);
                    for (int i = 0; i < files.length; i++) {
                        Log.i(TAG, "doInBackground: " + files[i].getName());
                        File file = files[i];
                        // 把因为暂停所录出的多段录音进行读取
                        fileInputStream = new FileInputStream(file);
                        byte[] mByte = new byte[fileInputStream.available()];
                        Log.i(TAG, "doInBackground: mByte"+mByte.length+"   "+mByte.toString());
                        int length = mByte.length;
                        // 第一个录音文件的前六位是不需要删除的
                        if (i == 0) {
                            while (fileInputStream.read(mByte) != -1) {
                                fileOutputStream.write(mByte, 0, length);
                                fileOutputStream.flush();
                            }
                        }
                        else {
                            while (fileInputStream.read(mByte) != -1) {
                                fileOutputStream.write(mByte, 6, length - 6);
                                fileOutputStream.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "doInBackground: 错误1" + e.toString());
                    e.printStackTrace();

                } finally {
                    Log.i(TAG, "doInBackground: finally");
                    try {

                        fileInputStream.close();
                        fileOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return complete;
            }

            protected void onPostExecute(String result) {
                Toast.makeText(context, "合并完成", Toast.LENGTH_SHORT).show();
            }

        }.execute();
    }






    int count = 0;

    AudioRecordManager audioRecordManager;

    int nameOrder=0;
    @OnClick({R.id.btn_mr_start, R.id.btn_mr_finish, R.id.btn_a_start, R.id.btn_a_finish, R.id.btn_window_start, R.id.btn_window_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_mr_start:
                MediaRecorderManager.getInstance().startRecord(this, count++ % 3);
                break;
            case R.id.btn_mr_finish:
                MediaRecorderManager.getInstance().finishRecord();

                break;
            case R.id.btn_a_start:
                audioRecordManager.startRecord(context,pcmFilePath,   "audioRecord"+nameOrder+".pcm");


//                PowerManager powerManager= (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//                powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
//                new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" +getPackageName()));
//
//                if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
//                    //1、请求开启电量优化
//                    context.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
//                    //2、显示系统对话框
//                    context.startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + getPackageName())));
//                }

                break;
            case R.id.btn_a_finish:
                audioRecordManager.stopRecord();
                nameOrder++;

                break;

            case R.id.btn_window_start:


                break;
            case R.id.btn_window_finish:
                break;


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        MediaRecorderManager.getInstance().finishRecord();
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            startBtn.setEnabled(true);
            startBtn.setText(recordService.isRunning() ? R.string.stop_record : R.string.start_record);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
            startBtn.setText(R.string.stop_record);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }


}
