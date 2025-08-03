package org.example;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ProgressBar;

public class Checksum {

	protected Display display;
	protected Shell shell;
	private Text tFile;
	private Text tVerify;
	private Button btnCancel;
	private ProgressBar progressBar;
	private Label tSize;
	private Text tMD5;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Checksum window = new Checksum();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
//		shell.setImage(SWTResourceManager.getImage(ChecksumDemo.class, "/res/file.png"));
		shell.setSize(600, 380);
		shell.setText("JavaMD5 (swt)");
		shell.setLayout(new GridLayout(3, false));
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblNewLabel.setText("Select a file to compute MD5 checksum");
		
		tFile = new Text(shell, SWT.BORDER);
		tFile.setEditable(false);
		tFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Button btnSelect = new Button(shell, SWT.NONE);
		btnSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				String fpath = fileDialog.open();
				File file = new File(fpath);
				if (fpath == null || !file.exists()) {
					return;
				}
				btnCancel.setEnabled(false);
				tFile.setText(fpath);
				tSize.setText(String.format("File Size: %d bytes", file.length()));
				System.out.println(fpath);
				System.out.println(file.length());
				progressBar.setSelection(0);
				progressBar.setMaximum(Math.toIntExact(file.length()));
				new CheckSumTask(file).start();
			}
		});
		btnSelect.setText("Browser..");
		
		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		btnCancel = new Button(shell, SWT.NONE);
		btnCancel.setEnabled(false);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnCancel.setText(" Cancel  ");
		
		Label label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		tSize = new Label(shell, SWT.NONE);
		tSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		tSize.setText("FileSize: n/a");
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		Label lblCurrentFileMd = new Label(shell, SWT.NONE);
		lblCurrentFileMd.setText("Current file MD5 checksum value:");
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		tMD5 = new Text(shell, SWT.BORDER);
		tMD5.setEditable(false);
		tMD5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setText("Original file MD5 checksum value:");
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		tVerify = new Text(shell, SWT.BORDER);
		tVerify.setToolTipText("paste its original md5 value to verify");
		tVerify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnVerify = new Button(shell, SWT.NONE);
		btnVerify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean matched = Objects.equals(tMD5.getText().trim(), tVerify.getText().trim());
				String msg = "Original:\t" + tMD5.getText() + "\nCurrent:\t" + tVerify.getText();
				if (matched) {
					MessageDialog.openInformation(shell, "Matched", msg);
				} else {
					MessageDialog.openWarning(shell, "Not Matched", msg);
				}
			}
		});
		btnVerify.setText("Verify");
		new Label(shell, SWT.NONE);

	}
	
	class CheckSumTask extends Thread {
		
		private File file;
		
		public CheckSumTask(File file) {
			super();
			this.file = file;
		}

		private static final int BSIZE = 2048;

		@Override
		public void run() {
			if (file == null || !file.exists()) {
	            return;
	        }
	        long max = file.length();
	        try(FileInputStream input = new FileInputStream(file))  {
	            MessageDigest md5 = MessageDigest.getInstance("MD5");
	            FileChannel channel = input.getChannel();
	            ByteBuffer buf = ByteBuffer.allocate(BSIZE);

	            long size = 0;
	            while (true) {
	                int read = channel.read(buf);
	                if (read == -1) {
	                	display.asyncExec(new Runnable() {
	    					public void run() {
	    						progressBar.setSelection(Math.toIntExact(file.length()));
	    					}
	    				});
	                    break;
	                }
	                size += read;
//	                progressBar.setSelection(Math.toIntExact(size));
	                final long fSize = size;
	                display.asyncExec(new Runnable() {
    					public void run() {
    						progressBar.setSelection(Math.toIntExact(fSize));
    					}
    				});

	                buf.flip();
	                md5.update(buf);
	                buf.clear();
	            }

	            String ret = new BigInteger(1, md5.digest()).toString(16);
	            display.asyncExec(new Runnable() {
					public void run() {
						tMD5.setText(ret);
					}
				});
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}

}