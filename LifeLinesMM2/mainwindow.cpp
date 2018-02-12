#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QCloseEvent>

#include <opencv2/opencv.hpp>
//// ZED include
#include "sl/Camera.hpp"

#include "zedthread.h"

zedthread* zThread;

//// Using std and sl namespaces
using namespace std;
using namespace sl;

cv::Mat slMat2cvMat(Mat& input);

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    client = NULL;

    connect(&server, SIGNAL(newConnection()),this, SLOT(acceptConnection()));


    server.listen(QHostAddress::Any, 1234);

    zThread = new zedthread();

}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::acceptConnection()
{
    client = server.nextPendingConnection();

    connect(client, SIGNAL(readyRead()),this, SLOT(startRead()));

    qDebug() << "Connected";
}

void MainWindow::startRead()
{
    char buffer[1024] = {0};
    client->read(buffer, client->bytesAvailable());
    QString dataRecieved = QString(buffer);

    if(dataRecieved.contains("zedstatus"))
    {
        bool status = zThread->isCameraOn();
        send(QString::number((status)));
        qDebug() << "Status: " << QString::number(status).toLocal8Bit();
    }
    else if(dataRecieved.contains("setVideo"))
    {
        QString id = dataRecieved.replace("setVideo","");
        if(id.at(0)=='1') zThread->setVideos(0);
        else zThread->setVideos(5);
        if(id.at(1)=='1') zThread->setVideos(1);
        else zThread->setVideos(6);
        if(id.at(2)=='1') zThread->setVideos(2);
        else zThread->setVideos(7);
        if(id.at(3)=='1') zThread->setVideos(3);
        else zThread->setVideos(8);
        if(id.at(4)=='1') zThread->setVideos(4);
        else zThread->setVideos(9);

    }
    else if(dataRecieved.contains("startSaving"))
    {
        if(!zThread->saving) {
            zThread->start();
            zThread->saving = true;
        }
    }
    else if(dataRecieved.contains("stopSaving"))
    {
        if(zThread->saving) {
            zThread->saving = false;
            zThread->quit();
        }
    }
    else if(dataRecieved.contains("getImage"))
    {
      sendImage();
    }
    else if(dataRecieved.contains("checkZED"))
    {
      bool check = checkZED();
      if(check) send("ZED_OK");
      else send("ZED_KO");
    }
    else if(dataRecieved.contains("checkSaving"))
    {
      if(zThread->saving) send("SAVING_OK");
    }


    ui->plainTextEdit->appendPlainText("Recived: "+dataRecieved+"\n");
}

void MainWindow::on_btSend_clicked()
{
    QString txt = ui->lineEdit->text();
    send(txt);
    qDebug() << txt.toLocal8Bit();
    /*
    if(zThread.saving) {
        zThread.saving = false;
        zThread.quit();
    }
    else {
        zThread.start();
        zThread.saving = true;
    }
    qDebug() << zThread.saving;
    */
}

void MainWindow::send(QString data)
{
    if(client!=NULL) client->write(data.toLocal8Bit()+'\n');
}

void MainWindow::closeEvent (QCloseEvent *event)
{
    qDebug() << "Close";
    event->accept();
}

cv::Mat slMat2cvMat2(Mat& input) {
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

void MainWindow::sendImage()
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
    else
    {
        Resolution image_size = zed.getResolution();
        int width = image_size.width;
        int height = image_size.height;
        Mat zed_image_left(width, height, MAT_TYPE_8U_C4);
        err = zed.grab();
        while(err!=0)
        {
            err = zed.grab();
            cout << "Err:" << err << toString(err) << endl;
        }

        zed.retrieveImage(zed_image_left, VIEW_LEFT);
        cv::Mat left_image_ocv = slMat2cvMat2(zed_image_left);

        //cv::imshow("Get",left_image_ocv);
        cv::imwrite("/var/www/html/cap/img.jpg",left_image_ocv);

        zed.close();
    }

}

bool MainWindow::checkZED()
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
        return false;
    }
    else return true;
}



