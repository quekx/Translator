package com.example.qkx.translator.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.Constants;
import com.example.qkx.translator.R;
import com.example.qkx.translator.ui.base.BaseDetailActivity;
import com.example.qkx.translator.utils.ImageUtil;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class OrcActivity extends BaseDetailActivity {
    private static final int CROP = 22;
    private static final String TAG = OrcActivity.class.getSimpleName();
    private static final int WHAT_ORC = 0;
    private static final int WHAT_RESOURCE = 1;
    private String SD_PATH;
    private Bitmap bitmap;
    @Bind(R.id.btn_orc)
    Button btnOrc;
    private MyHandler handler;
    @Bind(R.id.iv_pic)
    ImageView imageView;
    private Uri imgUri;
    private boolean isOrcRunning = false;
    private TessBaseAPI tessBaseAPI;
    @Bind(R.id.tv_result)
    TextView tvResult;

    private boolean checkResourceExist() {
        String path = SD_PATH + "/tessdata/eng.traineddata";
        File file = new File(path);
        if (file.exists()) {
            Log.i(TAG, "eng.traineddata exist!");
            return true;
        } else {
            Log.i(TAG, "eng.traineddata do not exist!");
            return false;
        }
    }

    private void doCrop(Uri uri) {
        if (uri == null) return;

        File imgDir = new File(Environment.getExternalStorageDirectory(), "record/imgCrop");
        if (!imgDir.exists() && !imgDir.isDirectory()) {
            imgDir.mkdirs();
        }
        imgUri = Uri.fromFile(new File(imgDir, "img" + System.currentTimeMillis() + ".png"));

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setData(uri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROP);
    }

    private void init() {
        SD_PATH = Environment.getExternalStorageDirectory().getPath();
        Log.d(TAG, "SD >> " + SD_PATH);
        imageView = (ImageView) findViewById(R.id.iv_pic);
        tvResult = (TextView) findViewById(R.id.tv_result);
        handler = new MyHandler(this);
        initOrcResource();
    }

    private void initOrcResource() {
        if (!checkResourceExist()) {
            Toast.makeText(this, "初始化资源文件......", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream in = null;
                    FileOutputStream fos = null;
                    try {
                        in = OrcActivity.this.getAssets().open("eng.traineddata");

//                        String path = SD_PATH + "/tessdata/eng.traineddata";
                        String dirPath = SD_PATH + "/tessdata";
                        File dir = new File(dirPath);
                        dir.mkdir();

                        File file = new File(dir, "eng.traineddata");
                        fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(bytes)) > 0) {
                            fos.write(bytes, 0, bytesRead);
                        }
                        Log.i(TAG, file.getPath() + " : copy finish!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    initTessBaseAPI();

                    handler.sendEmptyMessage(WHAT_RESOURCE);
                }
            }).start();
        } else {
            initTessBaseAPI();
        }
    }

    private void initTessBaseAPI() {
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(SD_PATH, "eng");
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
    }

    private void parseImgUri(Uri uri) {
        if (uri == null) return;

        Log.d(TAG, "uri >> " + uri.toString());
        try {
            bitmap = ImageUtil.decodeBitmapByRatioSize(this, 800, 800, uri);
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constants.PHOTO_REQUEST_GALLERY_CROP:
                if (data != null) {
                    Log.d(TAG, "Uri >> " + data.getData().toString());
                    doCrop(data.getData());
                }
                break;
            case Constants.PHOTO_REQUEST_CAMERA_CROP:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri == null) {
                        Bitmap bitmap = data.getExtras().getParcelable("data");
                        uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                                bitmap, null, null));
                        Log.d(TAG, "Uri >> " + uri.toString());
                    }
                    doCrop(uri);
                }
                break;
            case CROP:
                if (data != null) {
                    if (imgUri != null) {
                        Log.d(TAG, "uri ---> " + imgUri.toString());
                        try {
                            bitmap = ImageUtil.decodeBitmapByRatioSize(this, 800, 800, imgUri);
                            imageView.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case Constants.PHOTO_REQUEST_CAMERA:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri == null) {
                        Bitmap bitmap = data.getExtras().getParcelable("data");
                        uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                                bitmap, null, null));
                    }
                    parseImgUri(uri);
                }
                break;
            case Constants.PHOTO_REQUEST_GALLERY:
                if (data != null) {
                    parseImgUri(data.getData());
                }
                break;
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_orc;
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        ButterKnife.bind(this);
        setTitle(getResources().getString(R.string.title_orc));
        init();
    }

    @OnClick(R.id.btn_orc)
    void orcTest() {
        if (bitmap != null && !isOrcRunning) {
            Toast.makeText(this, "开始识别!", Toast.LENGTH_SHORT).show();
            btnOrc.setText("正在识别...");
            btnOrc.setClickable(false);
            isOrcRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    tessBaseAPI.setImage(bitmap);

                    String res = tessBaseAPI.getUTF8Text();
                    tessBaseAPI.clear();

                    Message msg = handler.obtainMessage();
                    msg.what = WHAT_ORC;
                    msg.obj = res;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    @OnClick(R.id.btn_pick_with_crop)
    void pickPhotoWithCrop() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PHOTO_REQUEST_GALLERY_CROP);
    }

    @OnClick(R.id.btn_pick)
    void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PHOTO_REQUEST_GALLERY);
    }

    @OnClick(R.id.btn_camera_with_crop)
    void takePhotoWithCrop() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, Constants.PHOTO_REQUEST_CAMERA_CROP);
    }

    @OnClick(R.id.btn_camera)
    void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, Constants.PHOTO_REQUEST_CAMERA);
    }

    static class MyHandler extends Handler {
        WeakReference<OrcActivity> weakReference;

        MyHandler(OrcActivity orcActivity) {
            super();
            weakReference = new WeakReference<>(orcActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_ORC:
                    String res = (String) msg.obj;
                    OrcActivity orcActivity = weakReference.get();
                    if (orcActivity != null) {
                        orcActivity.tvResult.setText(String.format("识别结果:\n%s", res));
                        Toast.makeText(orcActivity, "识别完成!", Toast.LENGTH_SHORT).show();
                        orcActivity.isOrcRunning = false;
                        orcActivity.btnOrc.setText("开始识别");
                        orcActivity.btnOrc.setClickable(true);
                    }
                    break;
                case WHAT_RESOURCE:
                    Toast.makeText(weakReference.get(), "加载完成!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
