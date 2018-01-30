#ifndef ZEDTHREAD_H
#define ZEDTHREAD_H
#include <QtCore>

class zedthread : public QThread
{

public:
    zedthread();
    void run();
    bool saving;
};

#endif // ZEDTHREAD_H
