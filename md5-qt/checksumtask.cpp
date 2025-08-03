#include "checksumtask.h"

#include <QDebug>

CheckSumTask::CheckSumTask(const QString &fpath, QObject *parent)
    : QThread{parent}, m_fpath(fpath)
{
//        QMessageBox::warning(this, "Read file", QString("Cannot open file: %1\n").arg(fpath));
}


void CheckSumTask::run()
{
    if (m_fpath.isEmpty())
    {
        emit resultReady("no file found");
        return;
    }
    QFile file(m_fpath);
    if (!file.open(QIODevice::ReadOnly))
    {
        emit resultReady("cannot read file");
        return;
    }

    QString result;
    qint32 readChunks=0;
    QCryptographicHash hash(QCryptographicHash::Md5);
    while (!file.atEnd()) {
        if (QThread::currentThread()->isInterruptionRequested())
        {
            qDebug() << "terminated now...";
            return;
        }

        QByteArray content = file.read(1 * 1024 * 1024);
        hash.addData(content);
        ++readChunks;
        emit progress(readChunks);
    }
    QByteArray bytes = hash.result();
    emit resultReady(bytes.toHex());
    file.close();
}
