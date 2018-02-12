#ifndef ZEDTHREAD_H
#define ZEDTHREAD_H
#include <QtCore>

class zedthread : public QThread
{

private slots:

public slots:
    bool isCameraOn();
    void setVideos(int id);
    void setTrackName(QString name);

public:
    zedthread();
    void run();
    bool saving;
    QString trackName;

};

#endif // ZEDTHREAD_H
