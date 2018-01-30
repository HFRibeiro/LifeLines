#include "zedthread.h"
#include <QtCore>
#include <QDebug>

//// Standard includes
#include <stdio.h>
#include <string.h>

#include <opencv2/opencv.hpp>
//// ZED include
#include "sl/Camera.hpp"

#define SAVE_LEFT
#define SAVE_LEFT_SPLIT_COLORS
#define SAVE_RIGHT
#define SAVE_RIGHT_SPLIT_COLORS

#define SAVE_DEPTH

//// Using std and sl namespaces
using namespace std;
using namespace sl;

//// Sample variables (used everywhere)
CAMERA_SETTINGS camera_settings_ = CAMERA_SETTINGS_BRIGHTNESS; // create a camera settings handle
string str_camera_settings = "BRIGHTNESS";
int step_camera_setting = 1;


//// Sample functions
void updateCameraSettings(char key, Camera &zed);
void switchCameraSettings();
void printHelp();
void printCameraInformation(Camera &zed);
cv::Mat slMat2cvMat(Mat& input);

zedthread::zedthread()
{
    saving = false;
}

void zedthread::run()
{
    ///////// Create a ZED camera //////////////////////////
    Camera zed;

    ///////// Initialize and open the camera ///////////////
    ERROR_CODE err; // error state for all ZED SDK functions

    // Open the camera
    err = zed.open();

    if (err != SUCCESS)
    {
        cout << toString(err) << endl;
        zed.close();
        qDebug() << EXIT_FAILURE;

    }

    // Print help in console
    printHelp();

    // Print camera information
    printCameraInformation(zed);

    Resolution image_size = zed.getResolution();

    int width = image_size.width;
    int height = image_size.height;
    int fps = (int) zed.getCameraFPS();

    // Create a Mat to store images

    #ifdef SAVE_LEFT
    cv::VideoWriter video_left("video_left.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    Mat zed_image_left(width, height, MAT_TYPE_8U_C4);
    #endif

    #ifdef SAVE_LEFT_SPLIT_COLORS
    #ifndef SAVE_LEFT
         Mat zed_image_left(width, height, MAT_TYPE_8U_C4);
    #endif
    cv::VideoWriter video_left_blue("video_left_blue.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    cv::VideoWriter video_left_green("video_left_green.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    cv::VideoWriter video_left_red("video_left_red.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    #endif


    #ifdef SAVE_RIGHT
    cv::VideoWriter video_right("video_right.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    Mat  zed_image_right(width, height, MAT_TYPE_8U_C4);
    #endif

    #ifdef SAVE_RIGHT_SPLIT_COLORS
    #ifndef SAVE_RIGHT
         Mat zed_image_right(width, height, MAT_TYPE_8U_C4);
    #endif
    cv::VideoWriter video_right_blue("video_right_blue.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    cv::VideoWriter video_right_green("video_right_green.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    cv::VideoWriter video_right_red("video_right_red.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    #endif

    #ifdef SAVE_DEPTH
    cv::VideoWriter video_depth("video_depth.avi",CV_FOURCC('H','2','6','4'),fps, cv::Size(1280,720),true);
    Mat  depth_image(width, height, MAT_TYPE_32F_C1);
    #endif

    while(saving)
    {
        // Grab images and process them
        err = zed.grab();

        // Check that grab() is successful
        if (err == SUCCESS)
        {

            // Retrieve left image and display it with OpenCV
            #ifdef SAVE_LEFT

            zed.retrieveImage(zed_image_left, VIEW_LEFT);
            cv::Mat left_image_ocv = slMat2cvMat(zed_image_left);
            cv::cvtColor(left_image_ocv, left_image_ocv, CV_RGBA2RGB);
            video_left.write(left_image_ocv);
            //qDebug() << "saving left";
            //cv::imshow("VIEW_LEFT",left_image_ocv);
            #endif

            #ifdef SAVE_LEFT_SPLIT_COLORS
            #ifndef SAVE_LEFT
            zed.retrieveImage(zed_image_left, VIEW_LEFT);
            cv::Mat left_image_ocv = slMat2cvMat(zed_image_left);
            cv::cvtColor(left_image_ocv, left_image_ocv, CV_RGBA2RGB);
            #endif
            cv::Mat blue_left = cv::Mat::zeros(left_image_ocv.rows, left_image_ocv.cols, CV_8UC3 );
            cv::Mat green_left = cv::Mat::zeros(left_image_ocv.rows, left_image_ocv.cols, CV_8UC3 );
            cv::Mat red_left = cv::Mat::zeros(left_image_ocv.rows, left_image_ocv.cols, CV_8UC3 );

            cv::Mat channels_left[] = { red_left, green_left, blue_left };
            int from_to_left[] = {0,2, 1,4, 2,6 };

            cv::mixChannels( &left_image_ocv, 1, channels_left, 3, from_to_left, 3);

            video_left_blue.write(blue_left);
            video_left_green.write(green_left);
            video_left_red.write(red_left);

            //qDebug() << "saving left blue";
            //qDebug() << "saving left green";
            //qDebug() << "saving left red";

            //cv::imshow("blue left channel",blue_left);
            //cv::imshow("green left channel",green_left);
            //cv::imshow("red left channel",red_left);

            #endif

            #ifdef SAVE_RIGHT

            zed.retrieveImage(zed_image_right, VIEW_RIGHT);
            cv::Mat right_image_ocv = slMat2cvMat(zed_image_right);
            cv::cvtColor(right_image_ocv, right_image_ocv, CV_RGBA2RGB);
            video_right.write(right_image_ocv);
            //cv::imshow("VIEW_RIGHT", right_image_ocv);
            //qDebug() << "saving right";

            #endif

            #ifdef SAVE_RIGHT_SPLIT_COLORS
            #ifndef SAVE_RIGHT
            zed.retrieveImage(zed_image_right, VIEW_RIGHT);
            cv::Mat right_image_ocv = slMat2cvMat(zed_image_right);
            cv::cvtColor(right_image_ocv, right_image_ocv, CV_RGBA2RGB);
            #endif
            cv::Mat blue_right = cv::Mat::zeros(right_image_ocv.rows, right_image_ocv.cols, CV_8UC3 );
            cv::Mat green_right = cv::Mat::zeros(right_image_ocv.rows, right_image_ocv.cols, CV_8UC3 );
            cv::Mat red_right = cv::Mat::zeros(right_image_ocv.rows, right_image_ocv.cols, CV_8UC3 );

            cv::Mat channels_right[] = { red_right, green_right, blue_right };
            int from_to_right[] = {0,2, 1,4, 2,6 };

            cv::mixChannels( &right_image_ocv, 1, channels_right, 3, from_to_right, 3);

            video_right_blue.write(blue_right);
            video_right_green.write(green_right);
            video_right_red.write(red_right);

            //qDebug() << "saving right blue";
            //qDebug() << "saving right green";
            //qDebug() << "saving right red";

            //cv::imshow("blue right channel",blue_right);
            //cv::imshow("green right channel",green_right);
            //cv::imshow("red right channel",red_right);

            #endif

            #ifdef SAVE_DEPTH

            zed.retrieveImage(depth_image, VIEW_DEPTH);
            cv::Mat depth_image_ocv = slMat2cvMat(depth_image);
            cv::cvtColor(depth_image_ocv, depth_image_ocv, CV_RGBA2RGB);
            video_depth.write(depth_image_ocv);

            //qDebug() << "saving depth";

            //cv::imshow("VIEW_DEPTH", depth_image_ocv);

            #endif

            sleep_ms(20);

            // Handle keyboard shortcuts
            //updateCameraSettings(key, zed);

        }
    }
    // Exit

    zed.close();
    #ifdef SAVE_LEFT
    video_left.release();
    #endif
    #ifdef SAVE_RIGHT
    video_right.release();
    #endif
    #ifdef SAVE_DEPTH
    video_depth.release();
    #endif
    #ifdef SAVE_LEFT_SPLIT_COLORS
    video_left_blue.release();
    video_left_green.release();
    video_left_red.release();
    #endif
    #ifdef SAVE_RIGHT_SPLIT_COLORS
    video_right_blue.release();
    video_right_green.release();
    video_right_red.release();
    #endif

}


/**
 *  This function updates the ZED camera settings
 **/
void updateCameraSettings(char key, sl::Camera &zed) {
    int current_value;

    // Keyboard shortcuts
    switch (key) {

            // Switch to next camera parameter
        case 's':
            switchCameraSettings();
            break;

            // Increase camera settings value ('+' key)
        case '+':
            current_value = zed.getCameraSettings(camera_settings_);
            zed.setCameraSettings(camera_settings_, current_value + step_camera_setting);
            std::cout << str_camera_settings << ": " << current_value + step_camera_setting << std::endl;
            break;

            // Decrease camera settings value ('-' key)
        case '-':
            current_value = zed.getCameraSettings(camera_settings_);
            if (current_value >= 1) {
                zed.setCameraSettings(camera_settings_, current_value - step_camera_setting);
                std::cout << str_camera_settings << ": " << current_value - step_camera_setting << std::endl;
            }
            break;

            // Reset default parameters
        case 'r':
            std::cout << "Reset all settings to default" << std::endl;
            zed.setCameraSettings(sl::CAMERA_SETTINGS_BRIGHTNESS, -1, true);
            zed.setCameraSettings(sl::CAMERA_SETTINGS_CONTRAST, -1, true);
            zed.setCameraSettings(sl::CAMERA_SETTINGS_HUE, -1, true);
            zed.setCameraSettings(sl::CAMERA_SETTINGS_SATURATION, -1, true);
            zed.setCameraSettings(sl::CAMERA_SETTINGS_GAIN, -1, true);
            zed.setCameraSettings(sl::CAMERA_SETTINGS_EXPOSURE, -1, true);
            zed.setCameraSettings(sl::CAMERA_SETTINGS_WHITEBALANCE, -1, true);
            break;
    }
}

/**
 *  This function switches between camera settings
 **/
void switchCameraSettings() {
    step_camera_setting = 1;
    switch (camera_settings_) {
        case CAMERA_SETTINGS_BRIGHTNESS:
            camera_settings_ = CAMERA_SETTINGS_CONTRAST;
            str_camera_settings = "Contrast";
            std::cout << "Camera Settings: CONTRAST" << std::endl;
            break;

        case CAMERA_SETTINGS_CONTRAST:
            camera_settings_ = CAMERA_SETTINGS_HUE;
            str_camera_settings = "Hue";
            std::cout << "Camera Settings: HUE" << std::endl;
            break;

        case CAMERA_SETTINGS_HUE:
            camera_settings_ = CAMERA_SETTINGS_SATURATION;
            str_camera_settings = "Saturation";
            std::cout << "Camera Settings: SATURATION" << std::endl;
            break;
        case CAMERA_SETTINGS_SATURATION:
            camera_settings_ = CAMERA_SETTINGS_GAIN;
            str_camera_settings = "Gain";
            std::cout << "Camera Settings: GAIN" << std::endl;
            break;

        case CAMERA_SETTINGS_GAIN:
            camera_settings_ = CAMERA_SETTINGS_EXPOSURE;
            str_camera_settings = "Exposure";
            std::cout << "Camera Settings: EXPOSURE" << std::endl;
            break;
        case CAMERA_SETTINGS_EXPOSURE:
            camera_settings_ = CAMERA_SETTINGS_WHITEBALANCE;
            str_camera_settings = "White Balance";
            step_camera_setting = 100;
            std::cout << "Camera Settings: WHITE BALANCE" << std::endl;
            break;

        case CAMERA_SETTINGS_WHITEBALANCE:
            camera_settings_ = CAMERA_SETTINGS_BRIGHTNESS;
            str_camera_settings = "Brightness";
            std::cout << "Camera Settings: BRIGHTNESS" << std::endl;
            break;

        case CAMERA_SETTINGS_AUTO_WHITEBALANCE:
        break;
        case CAMERA_SETTINGS_LAST:
        break;
    }
}

/**
 *  This function displays ZED camera information
 **/
void printCameraInformation(sl::Camera &zed) {

    qDebug() << "ZED Serial Number         : " << zed.getCameraInformation().serial_number;
    qDebug() << "ZED Firmware              : " << zed.getCameraInformation().firmware_version;
    qDebug() << "ZED Camera Resolution     : " << zed.getResolution().width << "x" << zed.getResolution().height;
    qDebug() << "ZED Camera FPS            : " << (int) zed.getCameraFPS();
}

/**
 *  This function displays help
 **/
void printHelp() {
    cout << endl;
    cout << endl;
    cout << "Camera controls hotkeys: " << endl;
    cout << "  Increase camera settings value:            '+'" << endl;
    cout << "  Decrease camera settings value:            '-'" << endl;
    cout << "  Switch camera settings:                    's'" << endl;
    cout << "  Reset all parameters:                      'r'" << endl;
    cout << endl;
    cout << "Exit : 'q'" << endl;
    cout << endl;
    cout << endl;
    cout << endl;
}

cv::Mat slMat2cvMat(Mat& input) {
    // Mapping between MAT_TYPE and CV_TYPE
    int cv_type = -1;
    switch (input.getDataType()) {
        case MAT_TYPE_32F_C1: cv_type = CV_32FC1; break;
        case MAT_TYPE_32F_C2: cv_type = CV_32FC2; break;
        case MAT_TYPE_32F_C3: cv_type = CV_32FC3; break;
        case MAT_TYPE_32F_C4: cv_type = CV_32FC4; break;
        case MAT_TYPE_8U_C1: cv_type = CV_8UC1; break;
        case MAT_TYPE_8U_C2: cv_type = CV_8UC2; break;
        case MAT_TYPE_8U_C3: cv_type = CV_8UC3; break;
        case MAT_TYPE_8U_C4: cv_type = CV_8UC4; break;
        default: break;
    }

    // Since cv::Mat data requires a uchar* pointer, we get the uchar1 pointer from sl::Mat (getPtr<T>())
    // cv::Mat and sl::Mat will share a single memory structure
    return cv::Mat(input.getHeight(), input.getWidth(), cv_type, input.getPtr<sl::uchar1>(MEM_CPU));
}





