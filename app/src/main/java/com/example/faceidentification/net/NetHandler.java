package com.example.faceidentification.net;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.faceidentification.MainActivity;
import com.example.faceidentification.face.Face;
import com.example.faceidentification.face.FaceShapes;
import com.example.faceidentification.utils.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetHandler {


    private static String requestGroupPath = "http://139.196.179.105:8000/faces/";

    private Context context;

    public NetHandler(Context context) {
        this.context = context;
    }

    public void GetAllFaces() throws IOException, JSONException {
        OkHttpUtils.GetAsyncData(requestGroupPath, new ResponseCallBack() {
            @Override
            public void success(String json) throws JSONException {
                ArrayList<Face> faces = new ArrayList<>();
                Log.i(TAG,"request success------------->");
                System.out.println("recv data ---->:"+json);
                JSONArray jsonArray = new JSONArray(json);
                for(int i = 0; i < jsonArray.length(); ++i) {
                    Face face = new Face();
                    JSONObject object =  jsonArray.getJSONObject(i);
                    face.setJsonObject(object);
                    faces.add(face);
                }
                MainActivity.faces = faces;
                for (int i = 0; i <faces.size() ; i++) {
                    MainActivity.textView.append("face id:"+faces.get(i).getId()+
                            "name:"+faces.get(i).getName()+
                            "filename:"+faces.get(i).getFileName()+"\n");
                }

            }

            @Override
            public void error(String json) {
                Log.w("NetHandler","bad request");
            }
        });
//        String json = OkHttpUtils.GetSyncData(requestGroupPath);
//        Log.i(TAG,"request success------------->");
//        System.out.println("recv data ---->:"+json);
//        JSONArray jsonArray = new JSONArray(json);
//        for(int i = 0; i < jsonArray.length(); ++i) {
//            Face face = new Face();
//            JSONObject object =  jsonArray.getJSONObject(i);
//            face.setJsonObject(object);
//            faces.add(face);
//        }
//        return faces;
    }

    public boolean CreateNewFace(Face face) {
        boolean status = false;
        OkHttpUtils.postAsyncJson(requestGroupPath, face.getJsonStr(), new ResponseCallBack() {
            @Override
            public void success(String json) throws JSONException {
            }

            @Override
            public void error(String json) {

            }
        });
        return status;
    }

    public void FacesRecognition(Face[] faces) {
        for(Face face : faces) {
//            FaceShapes[] faceFeature = face.getFaceFeature();
//            if (faceFeature == null) {
//                Log.e("FacesRecognition", "face feature is null,cannot request server");
//                return;
//            }
            MainActivity.recognitionResText.setText("");
            OkHttpUtils.postAsyncJson(requestGroupPath + "recognition", face.getJsonStr(), new ResponseCallBack() {
                @Override
                public void success(String jsonStr) throws JSONException {
                    Log.d("FacesRecognition","success request,response:"+jsonStr);
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    if(jsonObject.has("name")) {
                        String name = jsonObject.getString("name");
                        MainActivity.recognitionResText.append(name+" ");
                        if (name != "unknown") {
                            ToastUtil.showMessage(context, "检测到人脸，姓名" + name);
                        }else {
                            ToastUtil.showMessage(context, "不能识别该人脸，请先录入该人脸");
                        }
                    }else {
                        Log.w("FacesRecognition","json string error");
                    }
                }
                @Override
                public void error(String json) {
                    Log.w("FacesRecognition","bad request");
                    ToastUtil.showMessage(context,"人脸识别失败，检查网络连接");
                }
            });
        }
    }

    public void FaceImageUpload(Face face, File file) {
        Map<String,Object> map = new HashMap<>();
        map.put("id",face.getId());
        map.put("name",face.getName());
        map.put("fileName",face.getFileName());
        OkHttpUtils.postAsyncFileWithFormdata(requestGroupPath, map, file, new ResponseCallBack() {
            @Override
            public void success(String json) throws JSONException {
                Log.d("FaceImageUpload","success request,response:"+json);
                MainActivity.textView.append("upload success");
                ToastUtil.showMessage(context,"上传成功");
            }

            @Override
            public void error(String json) {
                Log.w("FaceImageUpload","json string error");
                ToastUtil.showMessage(context,"上传失败，请检查网络连接");
            }
        });

    }

}
