package com.example.faceidentification;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faceidentification.databinding.ActivityMainBinding;
import com.example.faceidentification.face.Face;
import com.example.faceidentification.face.FaceShapes;
import com.example.faceidentification.net.NetHandler;

import org.json.JSONException;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'faceidentification' library on application startup.
    static {
        System.loadLibrary("dlib");
        System.loadLibrary("opencv_java4");
        System.loadLibrary("faceidentification");
    }

    private ActivityMainBinding binding;

    private ImageView faceImage;

    private ImageView faceDetect;

    private Bitmap faceBitmap;

    public static TextView textView;

    public static ArrayList<Face> faces;

    private Button detectCameraButton;

    private Button detectLocalButton;

    private Button faceDetectButton;


    private Button faceRecordButton;

    private ActivityResultLauncher getLocalPhotoLauncher;

    private ActivityResultLauncher getCameraPhotoLauncher;

    public static TextView recognitionResText;

    private File currentImageFile = null;

    //读取图片旋转角度
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.w("readPictureDegree","readPictureDegree : orientation = " + orientation);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                degree = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                degree = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                degree = 270;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    //旋转图片
    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotation = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
        return rotation;
    }


    private Intent choosePhoto() {
        /**
         * 打开选择图片的界面
         */
        if (Build.VERSION.SDK_INT >= 30) {// Android 11 (API level 30)
            return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            return Intent.createChooser(intent, null);
        }

    }
//    public static void saveBitmap(String path, Bitmap bm) {
//        File f = new File(path);
//        if (f.exists()) {
//            f.delete();
//        }
//        try {
//            FileOutputStream out = new FileOutputStream(f);
//            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.flush();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    private Uri getTakePictureUri() {
        File dir = new File(Environment.getExternalStorageDirectory(), "pictures");
        if (dir.exists()) {
            dir.mkdirs();//在根路径下建子目录，子目录名是"pictures"
        }
        //命名临时图片的文件名
        currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");
        Uri uri_camera;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //test.xxx.com.myapplication.fileprovider 是在清单文件配置的 android:authorities
            return FileProvider.getUriForFile(MainActivity.this, "com.example.faceidentification.fileprovider", currentImageFile);
        } else {
            return Uri.fromFile(currentImageFile);
        }
    }

    private Intent openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        System.out.println("用户点击了拍照按钮");

        //SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        File dir = new File(Environment.getExternalStorageDirectory(), "pictures");
        if (dir.exists()) {
            dir.mkdirs();//在根路径下建子目录，子目录名是"pictures"
        }
        //命名临时图片的文件名
        currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");
        Uri uri_camera;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //test.xxx.com.myapplication.fileprovider 是在清单文件配置的 android:authorities
            uri_camera = FileProvider.getUriForFile(MainActivity.this, "com.example.faceidentification.fileprovider", currentImageFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            System.out.println("openCamera   cameraSavePath length==" + currentImageFile.length());
        } else {
            uri_camera = Uri.fromFile(currentImageFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri_camera);
        return intent;
    }

    private void initItems() {
        //加载默认照片
        //bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/test1.png"));
//        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        faceBitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/zhou.jpg"));

        //获取button
        faceDetectButton = findViewById(R.id.faceDetectButton);
        faceRecordButton = findViewById(R.id.faceRecord);
        detectLocalButton = findViewById(R.id.detectLocalButton);
        detectCameraButton = findViewById(R.id.detectCameraButton);

        faceImage = findViewById(R.id.faceImage);
        faceImage.setImageBitmap(faceBitmap);
        recognitionResText = findViewById(R.id.recognitionResText);
        faceDetect = findViewById(R.id.faceDetectRes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("sdcard ok");
        }
        //初始化人脸识别引擎 C++
        initEngine(getAssets());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initItems();
        NetHandler netHandler = new NetHandler(this);
        getLocalPhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Log.i("onActivityResult", "获取照片");
                if (result.getData() != null) {
                    Log.i("onActivityResult", "获取照片成功");
                    Uri uri = result.getData().getData();
                    faceImage.setImageURI(uri);
                    Mat faceMat = new Mat();
                    try {
                        faceBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
//                    Utils.bitmapToMat(bitmap,faceMat);
//                    int num = detectFaceNum(faceMat.getNativeObjAddr());
//                    if(num <= 0) {
//                        Toast.makeText(FaceRecordActivity.this,"未检测到人脸，不能上传该文件",Toast.LENGTH_SHORT).show();
//                        uploadButton.setClickable(false);
//                    }else {
//                        Toast.makeText(FaceRecordActivity.this,"检测到人脸，人脸数量"+num,Toast.LENGTH_SHORT).show();
//                        uploadButton.setClickable(true);
//                    }

                } else {
                    Log.w("onActivityResult", "获取照片失败");
                }
            }
        });

        getCameraPhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Log.e("onActivityResult", "拍照成功");
                    String imgPath = currentImageFile.getPath();
                    int degree = readPictureDegree(imgPath);
                    faceBitmap = rotateBitmap(degree, BitmapFactory.decodeFile(imgPath));
                    //faceBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(takePictureUri));
                    faceImage.setImageBitmap(faceBitmap);
                } else {
                    Log.e("onActivityResult", "拍照失败");
                }
                takePictureUri = null;
            }
        });
//        result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                if (result.getData() != null) {
//                    try {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.example.faceidentification.fileprovider", currentImageFile);
//                            Log.i("onActivityResult", "拍照成功");
//                            faceImage.setImageURI(contentUri);
//                            faceBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contentUri));
//                        } else {
//                            faceImage.setImageURI(Uri.fromFile(currentImageFile));
//                            faceBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(currentImageFile)));
//                        }
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                } else {
//                    Log.w("onActivityResult", "拍照失败");
//                }
//            }
//        }
//        NetHandler handler = new NetHandler();
//        File file = new File("/sdcard/header.png");
//        handler.FaceImageUpload(face,file);


        //System.out.println("sdcard failed");

        //faceDetect.setImageBitmap(faceBitmap);


        faceDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mat faceMat = new Mat();
                Bitmap bitmap = Bitmap.createBitmap(faceBitmap);
                Utils.bitmapToMat(bitmap, faceMat);
                System.out.println("face detecting begin....................");
                int[] arr = faceDetect(faceMat.getNativeObjAddr());
                Bitmap newImage = Bitmap.createBitmap(arr, faceMat.width(), faceMat.height(), Bitmap.Config.ARGB_8888);
                faceDetect.setImageBitmap(newImage);
                int num = getFaceNum();
                if (num <= 0) {
                    Toast.makeText(MainActivity.this, "未检测到人脸，请重新上传文件", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "检测到人脸个数:" + num, Toast.LENGTH_SHORT).show();
                    FaceShapes[][] landMarks = getFaceShapes();
                    System.out.println("get faces features......");
//                    for (int i = 0; i < landMarks.length; i++) {
//                        for (int j = 0; j < landMarks[i].length; j++) {
//                            System.out.printf("%d: [x=%d,y=%d]\n",j,landMarks[i][j].x,landMarks[i][j].y);
//                        }
//                    }
                    double[][] faceDes = getFacesDescriptors();
                    Log.i("face Des", "get faces descriptors...........");
                    System.out.println("faceDes length is :" + faceDes.length);
//                    for (int i = 0; i < faceDes.length; i++) {
//                        if(faceDes[i] != null && faceDes[i].length != 0) {
//                            for (int j = 0; j < faceDes[i].length; j++) {
//                                System.out.printf("%d:%f ", j, faceDes[i][j]);
//                            }
//                            System.out.println();
//                        }
//                    }
                    Face[] faces = new Face[faceDes.length];
                    int i = 0;
                    for (double[] facedes : faceDes) {
                        Face face = null;
                        try {
                            face = new Face(facedes);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        faces[i++] = face;
                    }
                    netHandler.FacesRecognition(faces);
                }
                System.out.println("face detecting end....................");

            }
        });
        //检测本地照片
        detectLocalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = choosePhoto();
                getLocalPhotoLauncher.launch(intent);
            }
        });
        detectCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
//                    Intent intent = openCamera();
                    takePictureUri = getTakePictureUri();
                    getCameraPhotoLauncher.launch(takePictureUri);
                }
            }
        });

        faceRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceRecordActivity.class);
                startActivity(intent);
            }
        });


    }

    private Uri takePictureUri;

    /**
     * A native method that is implemented by the 'faceidentification' native library,
     * which is packaged with this application.
     */
    public native void convertColor(long srcRGBMatAddr, long dstGrayMatAddr);

    public native int[] faceDetect(long srcFaceAddr);

    public native void initEngine(AssetManager assetManager);

    public native int getFaceNum();

    public native FaceShapes[][] getFaceShapes();

    public native double[][] getFacesDescriptors();

}