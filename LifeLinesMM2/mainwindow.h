#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QHostAddress>
#include <QDebug>
#include <QtNetwork>
#include <QObject>
#include <QTcpServer>
#include <QTcpSocket>
#include <QThread>
#include <QTimer>
#include <QTime>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private slots:
    void acceptConnection();

    void startRead();

    void on_btSend_clicked();

    void send(QString data);

    void closeEvent (QCloseEvent *event);

    void sendImage();

    bool checkZED();

    void sendRecordTime();

private:
    Ui::MainWindow *ui;
    QTcpServer server;
    QTcpSocket* client;
    QTimer *timeOwnGameBall;
    int recordTime;
};

#endif // MAINWINDOW_H
