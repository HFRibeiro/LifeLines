#ifndef ZEDTHREAD_H
#define ZEDTHREAD_H
#include <QtCore>
#include <QTime>

class zedthread : public QThread
{

private slots:
    QString getDateNow();
public slots:
    bool isCameraOn();
    void setVideos(int id);

public:
    zedthread();
    void run();
    bool saving;
    QString trackName;

};

#endif // ZEDTHREAD_H
