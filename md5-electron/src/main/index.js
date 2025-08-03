import { app, shell, BrowserWindow, ipcMain, dialog } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const path = require('path')
const fs = require('fs')
const crypto = require('crypto')


function createWindow() {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    width: 400,
    height: 370,
    show: false,
    autoHideMenuBar: true,
    ...(process.platform === 'linux' ? { icon } : {}),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false
    }
  })

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  electronApp.setAppUserModelId('com.electron')

  // Default open or close DevTools by F12 in development
  // and ignore CommandOrControl + R in production.
  // see https://github.com/alex8088/electron-toolkit/tree/master/packages/utils
  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window)
  })

  let stream = null;
  let streamEnd = false
  // IPC test
  ipcMain.on('ping', (event, arg) => {
    console.log('pong')
    event.sender.send('async-progress', 82);
  })
  ipcMain.on('task-cancel', (event, arg) => {
    if (stream && !streamEnd) {
      stream.pause()
      stream.close()
    }
  })

  ipcMain.on('file-dialog', (event, arg) => {
    // console.log(dialog.showOpenDialog({ properties: ['openFile', 'multiSelections'] }))
    dialog.showOpenDialog({
      properties: ['openFile']
    }).then(result => {
      console.log(result.canceled)
      console.log(result.filePaths)
      if (result.canceled) {
        event.returnValue = []
      } else {
        const filepath = result.filePaths[0]
        console.log(filepath)
        const stats = fs.statSync(filepath);
        event.returnValue = [filepath, stats.size]

        stream = fs.createReadStream(filepath)
        streamEnd = false
        let auto = 0;
        const hash = crypto.createHash('md5')
        stream.on('data', chunk => {
            hash.update(chunk, 'utf8')
            event.sender.send('async-progress', auto++);
        })
        stream.on('end', () => {
            const md5sum = hash.digest('hex')
            console.log(md5sum)
            event.sender.send('async-md5sum', md5sum);
            streamEnd = true
        })
        stream.on('close', ()=> {
            event.sender.send('async-close', '');
            streamEnd = true
        })
      }
    }).catch(err => {
      console.log(err)
    })
  })

  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// In this file you can include the rest of your app"s specific main process
// code. You can also put them in separate files and require them here.
