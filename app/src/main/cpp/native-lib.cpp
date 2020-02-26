#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C" JNIEXPORT void JNICALL
Java_com_avon_spott_AddPhoto_AddPhotoActivity_detectEdgeJNI(JNIEnv *env, jobject instance,
        jlong inputImage, jlong outputImage,
        jint th1, jint th2, jint th3) {

    Mat &inputMat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputImage;


    cvtColor(inputMat, outputMat, COLOR_RGB2GRAY);

    medianBlur(outputMat, outputMat, th1);
    adaptiveThreshold(outputMat, outputMat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY,  th2, th3);

    bitwise_not ( outputMat, outputMat); //색상 반전


    cvtColor(outputMat, outputMat, COLOR_GRAY2RGBA);

    for(int i = 0; i<outputMat.rows;i++){ // 검정색(면) 부분 투명으로 //왜 선부분을 제외한 면부부만 투명이 되는지 모르겠음...
        for(int j=0;j<outputMat.cols;j++){
            outputMat.at<cv::Vec4b>(i,j)[3]=0;
        }
    }

    for(int i = 0; i<outputMat.rows;i++){ // 하얀색(윤곽선) 부분 하늘색으로
        for(int j=0;j<outputMat.cols;j++){
            if(outputMat.at<cv::Vec4b>(i,j)[0] == 255 && outputMat.at<cv::Vec4b>(i,j)[1] == 255 && outputMat.at<cv::Vec4b>(i,j)[2] ==255 )
            {
                outputMat.at<cv::Vec4b>(i,j)[0]=86; //86
                outputMat.at<cv::Vec4b>(i,j)[1]=144; //144
                outputMat.at<cv::Vec4b>(i,j)[2]=232; //232
                outputMat.at<cv::Vec4b>(i,j)[3]=255;
            }
        }
    }



}

