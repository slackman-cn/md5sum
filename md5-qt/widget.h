#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>
#include <QPushButton>
#include <QLabel>
#include <QLineEdit>
#include <QProgressBar>
#include <QGridLayout>
#include <QVBoxLayout>
#include <QHBoxLayout>

#include "checksumtask.h"

class Widget : public QWidget
{
    Q_OBJECT

public:
    Widget(QWidget *parent = nullptr);
    ~Widget();

private:
    QPushButton *btnSelect;
    QPushButton *btnCancel;
    QPushButton *btnVerify;
    QLineEdit *tFile;
    QProgressBar *progress;
    QLabel *tooltip;
    QLabel *tInfo;
    QLabel *tSize;
    QLineEdit *tMD5;
    QLineEdit *tVerify;

    CheckSumTask *task;

protected slots:
     void onSelect();
     void onCancel();
     void onVerify();
};
#endif // WIDGET_H
