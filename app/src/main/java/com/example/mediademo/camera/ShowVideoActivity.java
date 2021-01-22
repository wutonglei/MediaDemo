package com.example.mediademo.camera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mediademo.R;
import com.example.mediademo.databinding.ActivityShowVideoBinding;

public class ShowVideoActivity extends AppCompatActivity {

    ActivityShowVideoBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_show_video);
        mBinding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.startActivityForResult(ShowVideoActivity.this);
            }
        });
    }
    private int width, height;
    private String filePath, coverFilePath;
    private boolean isVideo;
    private static final String TAG = "ShowVideoActivity";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: 111111111111xxxx"+resultCode+ "xxxx"+requestCode  );
        if (resultCode == RESULT_OK && requestCode == CameraActivity.REQ_CAPTURE && data != null) {
            width = data.getIntExtra(CameraActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CameraActivity.RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CameraActivity.RESULT_FILE_PATH);
            isVideo = data.getBooleanExtra(CameraActivity.RESULT_FILE_TYPE, false);
            Log.i(TAG, "onActivityResult: 22222222222222");
            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {

        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();
        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.cover.setImageUrl(filePath);
        mBinding.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        mBinding.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.startActivityForResult(ShowVideoActivity.this, filePath, isVideo, null);
            }
        });
    }
}
