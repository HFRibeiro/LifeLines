#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QCloseEvent>

#include "zedthread.h"

zedthread* zThread;

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


