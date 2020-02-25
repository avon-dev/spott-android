#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C" JNIEXPORT void JNICALL
Java_com_avon_spott_AddPhoto_AddPhotoActivity_detectEdgeJNI(JNIEnv *env, jobject instance,
        jlong inputImage, jlong outputImage,
        jint th1, jint th2) {

// TODO
Mat &inputMat = *(Mat *) inputImage;
Mat &outputMat = *(Mat *) outputImage;

//Canny로 윤곽선 따오기
cvtColor(inputMat, outputMat, COLOR_RGB2GRAY);
Canny(outputMat, outputMat, th1, th2);


//색상 반전
Mat srcImage = outputMat;


Mat_<uchar>image(srcImage);
Mat_<uchar>destImage(srcImage.size());

for(int y = 0 ; y < image.rows ; y++){
for(int x = 0; x < image.cols; x++){
uchar r = image(y,x);
destImage(y,x) = 255 - r;

//            Vec4b & color = destImage.at<Vec4b>(y,x);
//            if(destImage(y,x)==255){
//                // get pixel
//                // ... do something to the color ....
//
//                color[1] =232;
//                color[2] =23;
//                color[3] =122;
//
//
//            }
//
//            destImage.at<Vec4b>(y,x) = color;


}
}


outputMat = destImage.clone();
}

