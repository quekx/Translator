package com.example.qkx.translator.ui.orc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.Constants;
import com.example.qkx.translator.R;
import com.example.qkx.translator.data.ResultBean;
import com.example.qkx.translator.rest.RestSource;
import com.example.qkx.translator.ui.base.BaseDetailActivity;
import com.example.qkx.translator.utils.FileUtil;
import com.example.qkx.translator.utils.ImageUtil;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.List;

public class OrcActivity extends BaseDetailActivity {
    private static final int CROP = 22;
    private static final String TAG = OrcActivity.class.getSimpleName();
    private static final int MSG_ORC = 0;
    private static final int MSG_RESOURCE_COPY = 1;

    private String SD_PATH;
    private Bitmap mBitmap;

    @Bind(R.id.btn_orc)
    Button btnOrc;
    private MyHandler handler;
    @Bind(R.id.iv_pic)
    ImageView imageView;

    private Uri mCurrentImgUri;
    private Uri mCurrentCropUri;

    private boolean isOrcRunning = false;
    private TessBaseAPI tessBaseAPI;

    @Bind(R.id.tv_result)
    TextView tvResult;
    @Bind(R.id.tv_result_translate)
    TextView tvResultTranslate;

    private StringBuffer mBuffer = new StringBuffer();

    private boolean checkResourceExist() {
        String path = SD_PATH + "/tessdata/eng.traineddata";
        File file = new File(path);
        if (file.exists()) {
            KLog.i(TAG, "eng.traineddata exist!");
            return true;
        } else {
            KLog.i(TAG, "eng.traineddata do not exist!");
            return false;
        }
    }

    private void init() {
        SD_PATH = Environment.getExternalStorageDirectory().getPath();
        KLog.d(TAG, "SD >> " + SD_PATH);
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
                        KLog.i(TAG, file.getPath() + " : copy finish!");
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

                    handler.sendEmptyMessage(MSG_RESOURCE_COPY);
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

        KLog.d(TAG, "parseImgUri: uri >> " + uri.toString());
        try {
            mBitmap = ImageUtil.decodeBitmapByRatioSize(this, 800, 800, uri);
            imageView.setImageBitmap(mBitmap);
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
            case Constants.REQUEST_PHOTO_GALLERY_CROP:
                if (data != null) {
                    KLog.d(TAG, "result gallery: Uri >> " + data.getData().toString());
                    doCrop(data.getData());
                }
                break;
            case Constants.REQUEST_PHOTO_CAMERA_CROP:
//                if (data != null) {
//                    Uri uri = data.getData();
//                    if (uri == null) {
//                        Bitmap bitmap = data.getExtras().getParcelable("data");
//                        uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
//                                bitmap, null, null));
//                        KLog.d(TAG, "result camera: Uri >> " + uri.toString());
//                    }
//                    doCrop(uri);
//                }

                KLog.d(TAG, "result camera: mCurrentImgUri >> " + mCurrentImgUri);
                if (mCurrentImgUri == null) {
                    return;
                }

                addPhotoToMedia(mCurrentImgUri);

                doCrop(mCurrentImgUri);
                mCurrentImgUri = null;
                break;
            case CROP:
                if (data != null) {
                    if (mCurrentCropUri != null) {
                        KLog.d(TAG, "result crop: mCurrentCropUri >> " + mCurrentCropUri.toString());
                        parseImgUri(mCurrentCropUri);

                        mCurrentCropUri = null;
                    }
                }
                break;
            case Constants.REQUEST_PHOTO_CAMERA:
                KLog.d(TAG, "result camera: mCurrentImgUri >> " + mCurrentImgUri);
                if (mCurrentImgUri == null) {
                    return;
                }

                addPhotoToMedia(mCurrentImgUri);

                parseImgUri(mCurrentImgUri);
                mCurrentImgUri = null;
                break;
            case Constants.REQUEST_PHOTO_GALLERY:
                if (data != null) {
                    parseImgUri(data.getData());
                }
                break;
        }
    }

    private void addPhotoToMedia(Uri uri) {
        if (uri == null) return;

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);
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
        if (mBitmap != null && !isOrcRunning) {
            Toast.makeText(this, "开始识别!", Toast.LENGTH_SHORT).show();
            btnOrc.setText("正在识别...");
            btnOrc.setClickable(false);
            isOrcRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    tessBaseAPI.setImage(mBitmap);
                    String res = tessBaseAPI.getUTF8Text();
                    KLog.i(TAG, "ORC： res >> " + res);
                    tessBaseAPI.clear();

                    Message msg = handler.obtainMessage(MSG_ORC, res);
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    private void doCrop(Uri uri) {
        if (uri == null) return;

        KLog.i(TAG, "doCrop: uri >> " + uri);

        File imgCropDir = getCropDir();
        if (!imgCropDir.exists() && !imgCropDir.isDirectory()) {
            imgCropDir.mkdirs();
        }
        mCurrentCropUri = Uri.fromFile(new File(imgCropDir, FileUtil.getCurrentTime() + ".png"));

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentCropUri);
        intent.putExtra("return-data", false);

//        if (intent.resolveActivity(getPackageManager()) != null) {
        startActivityForResult(intent, CROP);
//        } else {
//            KLog.e(TAG, "intent.resolveActivity(getPackageManager()) >> null");
//        }
    }

    @OnClick(R.id.btn_camera_with_crop)
    void takePhotoWithCrop() {
        File imgDir = getPhotoDir();
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }
        mCurrentImgUri = Uri.fromFile(new File(imgDir, FileUtil.getCurrentTime() + ".png"));

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImgUri);

        startActivityForResult(intent, Constants.REQUEST_PHOTO_CAMERA_CROP);
    }

    @OnClick(R.id.btn_pick_with_crop)
    void pickPhotoWithCrop() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, Constants.REQUEST_PHOTO_GALLERY_CROP);
    }

    @OnClick(R.id.btn_camera)
    void takePhoto() {
        File imgDir = getPhotoDir();
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }
        mCurrentImgUri = Uri.fromFile(new File(imgDir, FileUtil.getCurrentTime() + ".png"));

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImgUri);
        startActivityForResult(intent, Constants.REQUEST_PHOTO_CAMERA);
    }

    @OnClick(R.id.btn_pick)
    void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.REQUEST_PHOTO_GALLERY);
    }

    @OnClick(R.id.btn_translate)
    void translateResult() {
        String str = tvResult.getText().toString();
        if (TextUtils.isEmpty(str)) return;

        mBuffer.setLength(0);
        RestSource.getInstance().queryCh(str, new RestSource.TranslateCallback() {
            @Override
            public void onProcessResult(ResultBean resultBean) {
                if (resultBean == null) return;

                List<ResultBean.TransResult> res = resultBean.trans_result;
                if (res == null || res.size() == 0) return;

                for (ResultBean.TransResult result : res) {
                    mBuffer.append(result.dst + '\n');
                }

                tvResultTranslate.setText(mBuffer.toString());
            }
        });
    }

    private File getPhotoDir() {
        return new File(Environment.getExternalStorageDirectory(), "record/img");
    }

    private File getCropDir() {
        return new File(Environment.getExternalStorageDirectory(), "record/imgCrop");
    }

    static class MyHandler extends Handler {
        SoftReference<OrcActivity> softReference;

        MyHandler(OrcActivity orcActivity) {
            super();
            softReference = new SoftReference<OrcActivity>(orcActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ORC:
                    OrcActivity orcActivity;
                    if (softReference == null || (orcActivity = softReference.get()) == null)
                        return;
                    String res = (String) msg.obj;
                    orcActivity.tvResult.setText(res);
                    Toast.makeText(orcActivity, "识别完成!", Toast.LENGTH_SHORT).show();
                    orcActivity.isOrcRunning = false;
                    orcActivity.btnOrc.setText("开始识别");
                    orcActivity.btnOrc.setClickable(true);
                    break;
                case MSG_RESOURCE_COPY:
                    if (softReference == null || (orcActivity = softReference.get()) == null)
                        return;
                    Toast.makeText(orcActivity, "加载完成!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
