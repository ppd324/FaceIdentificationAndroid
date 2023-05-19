# FaceIdentification

FaceIdentification是在安卓端运行的人脸识别APP，基于opencv以及dlib人脸识别库实现，可进行人脸68点检测，提取人脸特征。

## 版本

- OpenCV 4.6.0的安卓 SDK

- Dlib 19.10 

## 特征

FaceIdentification具有以下特性：

- 检测人脸

- 工作在安卓端

- 支持多张人脸检测

- 支持连接云端人脸识别库，识别人脸

## 演示

<img title="" src="https://github.com/ppd324/FaceIdentificationAndroid/blob/master/images/Screenshot_20230519-152155_FaceIdentification.jpg" alt="" width="472">

可上传人脸至云端人脸库，安卓端采集人脸特征后，发送人脸特征数据至云端，云端返回识别结果。

<img title="" src="https://github.com/ppd324/FaceIdentificationAndroid/blob/master/images/Screenshot_20230519-161014_FaceIdentification.jpg" alt="" width="263"><img title="" src="https://github.com/ppd324/FaceIdentificationAndroid/blob/master/images/Screenshot_20230519-161030_FaceIdentification.jpg" alt="" width="262">

## 说明

- 项目依赖dlib_face_recognition_resnet_model_v1.dat与shape_predictor_68_face_landmarks.dat模型均存放在assets目录。

- 人脸识别需要连接到云端人脸库，云端基于[go-face]([ppd324/go-face: Face recognition with Go (github.com)](https://github.com/ppd324/go-face))项目，云端代码基于golang编写，地址[go-face-server]([ppd324/go-face-server: 基于dlib go-face库实现远程人脸识别server (github.com)](https://github.com/ppd324/go-face-server))部署于Linux服务器。

- 目前 OpenCV 图片加载和提取人脸，特征标记、识别都是使用 Dlib。

- 经过测试Dlib的单张人脸特征在三星s10上提取耗时为350ms左右。发送识别请求到阿里云服务器人脸识别返回结果耗时100ms左右。




