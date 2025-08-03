#include "widget.h"

#include <QPushButton>
#include <QLabel>
#include <QLineEdit>
#include <QProgressBar>
#include <QGridLayout>
#include <QVBoxLayout>
#include <QHBoxLayout>

#include <QDebug>
#include <QMessageBox>
#include <QFileDialog>
#include <QCryptographicHash>
#include <QThread>

/**
 * http://qt5.digitser.top/5.15/zh-CN/qthread.html
 * http://qt5.digitser.top/5.15/zh-CN/layout.html
 * @brief Widget::Widget
 * @param parent
 */
Widget::Widget(QWidget *parent)
    : QWidget(parent)
{
    setWindowTitle("QtMD5");
    setFixedSize(550, 300);

    btnSelect = new QPushButton("Browse..", this);
    btnCancel = new QPushButton("Cancel  ", this);
    btnVerify = new QPushButton("Verify  ", this);
    btnCancel->setDisabled(true);

    tFile = new QLineEdit(this);
    progress = new QProgressBar(this);
    tooltip = new QLabel("Select a file to compute MD5 checksum (or drag and drop a file onto this window)", this);
    tInfo = new QLabel("File Size: ", this);
    tSize = new QLabel("n/a", this);

    tMD5 = new QLineEdit(this);
    tMD5->setReadOnly(true);
    tMD5->setPlaceholderText("n/a");
    tVerify = new QLineEdit(this);
    tVerify->setPlaceholderText("paste its original md5 value to verify");

    QVBoxLayout *layout = new QVBoxLayout(this);
    layout->setMargin(10);
    layout->setAlignment(Qt::AlignTop);
    setLayout(layout);
    layout->addWidget(tooltip);

    QGridLayout *grid = new QGridLayout(this);
    grid->setSpacing(10);
    grid->addWidget(tFile, 0, 0);
    grid->addWidget(btnSelect, 0, 1);
    grid->addWidget(progress, 1, 0);
    grid->addWidget(btnCancel, 1, 1);
    layout->addLayout(grid);

    layout->addSpacing(20);
    QHBoxLayout *g2 = new QHBoxLayout(this);
    g2->setAlignment(Qt::AlignLeft);
    g2->addWidget(tInfo);
    g2->addWidget(tSize);
    layout->addLayout(g2);

    layout->addWidget(tMD5);

    QHBoxLayout *g3 = new QHBoxLayout(this);
    g3->addWidget(tVerify);
    g3->addWidget(btnVerify);
    layout->addLayout(g3);

    connect(btnSelect, &QPushButton::clicked, this, &Widget::onSelect);
    connect(btnCancel, &QPushButton::clicked, this, &Widget::onCancel);
    connect(btnVerify, &QPushButton::clicked, this, &Widget::onVerify);
}


Widget::~Widget()
{
}

//  QString fpath = "/home/cnki/centos.iso";
//    qInfo() << "Total:" << size;
//    qInfo() << size / 1024 << "," << size / (1024 * 1024);
//    qInfo() << "Total Chunks: " << totalChunks;
void Widget::onSelect()
{

    QString fpath = QFileDialog::getOpenFileName(this, "Open File");
    if (fpath.isEmpty())
    {
      return;
    }
    QFileInfo fileInfo(fpath);
    qint64 size = fileInfo.size();
    tSize->setText(QString("%1 bytes").arg(size));
    tFile->setText(fpath);

    int totalChunks =size/ (1024*1024);
    progress->setMaximum(totalChunks);
    progress->setValue(0);
    task = new CheckSumTask(fpath);
    connect(task, &QThread::finished, task, &QThread::deleteLater);
    connect(task, &CheckSumTask::progress, this, [&](int value) {
       progress->setValue(value);
    });
    connect(task, &CheckSumTask::resultReady, this, [&](const QString &result) {
       tMD5->setText(result);
       btnCancel->setDisabled(true);
    });
    task->start();
    btnCancel->setDisabled(false);
    qDebug() << "Task Start..";
}

void Widget::onCancel()
{
    if (task && task->isRunning())
    {
        task->requestInterruption();
    }
    tFile->clear();
    tSize->setText("n/a");
    progress->setValue(0);
    btnCancel->setDisabled(true);
}

void Widget::onVerify()
{
    QString s1 = tMD5->text();
    QString s2 = tVerify->text();
    QString content = QString("Original: %1\nCurrent: %2").arg(s1, s2);
    if (QString::compare(s1, s2, Qt::CaseInsensitive) == 0)
    {
        QMessageBox::information(this, "Matched", content);
    }
    else
    {
        QMessageBox::warning(this, "Not Matched", content);
    }
}
