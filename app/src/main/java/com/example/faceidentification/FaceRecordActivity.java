package com.example.faceidentification;

import static com.example.faceidentification.MainActivity.readPictureDegree;
import static com.example.faceidentification.MainActivity.rotateBitmap;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.faceidentification.face.Face;
import com.example.faceidentification.net.NetHandler;

import org.json.JSONException;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class FaceRecordActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button uploadLocalButton;

    private Button takePhotoButton;

    private File tempFile;

    private Button uploadServerButton;

    private Uri imageUri;

    private EditText faceName;
    private  ActivityResultLauncher takePictureLauncher;
    private EditText faceAge;

    public static File uriToFile(Uri uri, Context context) {
        File file = null;
        if(uri == null) return file;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis()+ Math.round((Math.random() + 1) * 1000)
                    +"."+ MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

//            注释掉的方法可以获取到原文件的文件名，但是比较耗时
//            Cursor cursor = contentResolver.query(uri, null, null, null, null);
//            if (cursor.moveToFirst()) {
//                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));}

            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(is, fos);
                }
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
    private Intent choosePhoto() {
        /**
         * 打开选择图片的界面
         */
        if (Build.VERSION.SDK_INT >=30) {// Android 11 (API level 30)
            return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            return Intent.createChooser(intent, null);
        }

    }
    private Uri getTakePictureUri() {
        File dir = new File(Environment.getExternalStorageDirectory(), "pictures");
        if (dir.exists()) {
            dir.mkdirs();//在根路径下建子目录，子目录名是"pictures"
        }
        //命名临时图片的文件名
        tempFile = new File(dir, System.currentTimeMillis() + ".jpg");
        Uri uri_camera;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //test.xxx.com.myapplication.fileprovider 是在清单文件配置的 android:authorities
            return FileProvider.getUriForFile(this, "com.example.faceidentification.fileprovider", tempFile);
        } else {
            return Uri.fromFile(tempFile);
        }
    }
    private Intent openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        System.out.println("用户点击了拍照按钮");

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String PhotoFileName = Environment.getExternalStorageDirectory()+ File.separator+ format.format(new Date())+"Photo.jpg";
        Uri uri_camera;
        tempFile =new File(PhotoFileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //test.xxx.com.myapplication.fileprovider 是在清单文件配置的 android:authorities
            uri_camera = FileProvider.getUriForFile(FaceRecordActivity.this, "test.xxx.com.myapplication.fileprovider", tempFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            System.out.println("openCamera   cameraSavePath length=="+tempFile.length());
        }else {
            uri_camera = Uri.fromFile(tempFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri_camera);
        return intent;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_record);
        imageView = findViewById(R.id.uploadImage);
        uploadLocalButton = findViewById(R.id.uploadButton);
        uploadLocalButton.setClickable(false);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        uploadServerButton = findViewById(R.id.upLoadServerButton);
        faceName = findViewById(R.id.faceName);
        faceAge = findViewById(R.id.faceAge);

        ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK) {
                    Log.i("onActivityResult", "获取照片");
                    if (result.getData() != null) {
                        Log.i("onActivityResult", "获取照片成功");
                        Uri uri = result.getData().getData();
                        tempFile = uriToFile(uri,this);
                        imageView.setImageURI(uri);
                        Mat faceMat = new Mat();
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        Utils.bitmapToMat(bitmap,faceMat);
                        int num = detectFaceNum(faceMat.getNativeObjAddr());
                        if(num <= 0) {
                            Toast.makeText(FaceRecordActivity.this,"未检测到人脸，不能上传该文件",Toast.LENGTH_SHORT).show();
                            uploadLocalButton.setClickable(false);
                        }else {
                            Toast.makeText(FaceRecordActivity.this,"检测到人脸，人脸数量"+num,Toast.LENGTH_SHORT).show();
                            uploadLocalButton.setClickable(true);
                        }

                    } else {
                        Log.w("onActivityResult", "获取照片失败");
                    }
                }
        });
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Log.e("onActivityResult", "拍照成功");
                    String imgPath = tempFile.getPath();
                    int degree = readPictureDegree(imgPath);
                    Bitmap faceBitmap = rotateBitmap(degree, BitmapFactory.decodeFile(imgPath));
                    imageView.setImageBitmap(faceBitmap);
                }else {
                    Log.e("onActivityResult", "拍照失败");
                }
            }
        } );

        uploadLocalButton.setOnClickListener(v -> {
            Intent intent = choosePhoto();
            launcher.launch(intent);

        });

        takePhotoButton.setOnClickListener(v -> {
            int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                //Intent intent = openCamera();
                Uri uri = getTakePictureUri();
                takePictureLauncher.launch(uri);
            }
        });

        uploadServerButton.setOnClickListener( v-> {
           String name = String.valueOf(faceName.getText());
           Log.i("faceName","EditText is :"+name);
            NetHandler netHandler = new NetHandler(this);
            Face face = null;
            try {
                 face = new Face(name,tempFile.getName());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            netHandler.FaceImageUpload(face,tempFile);
        });
    }
    public native int detectFaceNum(long secFaceAddr);

}