#ifndef ZEDTHREAD_H
#define ZEDTHREAD_H
#include <QtCore>

class zedthread : public QThread
{

private slots:

public slots:
    bool isCameraOn();
    void setVideos(int id);

public:
    zedthread();
    void run();
    bool saving;

};

#endif // ZEDTHREAD_H
