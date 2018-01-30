#include "mainwindow.h"
#include "ui_mainwindow.h"

#include "zedthread.h"

zedthread zThread;

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    client = NULL;

    connect(&server, SIGNAL(newConnection()),this, SLOT(acceptConnection()));

    server.listen(QHostAddress::Any, 1234);

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
    if(client!=NULL) client->write(buffer);
    ui->plainTextEdit->appendPlainText("Recived: "+QString(buffer)+"\n");
}

void MainWindow::on_btSend_clicked()
{
    //QString txt = ui->lineEdit->text();
    //if(client!=NULL) client->write(txt.toLocal8Bit());
    if(zThread.saving) {
        zThread.saving = false;
        zThread.quit();
    }
    else {
        zThread.start();
        zThread.saving = true;
    }
    qDebug() << zThread.saving;
}


