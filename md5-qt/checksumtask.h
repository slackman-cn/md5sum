#ifndef CHECKSUMTASK_H
#define CHECKSUMTASK_H

#include <QThread>
#include <QFile>
#include <QCryptographicHash>

class CheckSumTask : public QThread
{
    Q_OBJECT
public:
    explicit CheckSumTask(const QString &fpath, QObject *parent = nullptr);
    QString m_fpath;
    void run() override;

signals:
    void resultReady(const QString& s);
    void progress(int);
};

#endif // CHECKSUMTASK_H
