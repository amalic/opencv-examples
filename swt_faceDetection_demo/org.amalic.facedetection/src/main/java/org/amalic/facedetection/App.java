package org.amalic.facedetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

public class App {
    public static void main( String[] args ) throws Exception {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    	Display display = new Display();
    	final Shell shell = new Shell(display,SWT.SHELL_TRIM & (~SWT.RESIZE));
    	shell.setText("Face Detection Demo");
    	shell.setLayout(new GridLayout(1,false));
    	GridData gridData = new GridData(GridData.FILL);
    	
    	Button button = new Button(shell, SWT.PUSH);
    	button.setText("Load Image and detect Faces");
    	button.setLayoutData(gridData);
    	button.addSelectionListener(new SelectionAdapter() {
    		@Override
    		public void widgetSelected(SelectionEvent e) {
    			FileDialog fileDialog = new FileDialog(shell, SWT.NULL);
    			fileDialog.setFilterExtensions(new String[]{"*.jpg; *.png"});
    			String fileName = fileDialog.open();
    			if(null!=fileName && fileName.length()>0) {
	    			File file = new File(fileName);
	    			detectFacesAndLoadImage(shell, file);
    			}
    		}
		});
    	shell.setLayoutData(gridData);
    	    	
    	shell.pack();
    	shell.open();
    	while(!shell.isDisposed())
    		if(!display.readAndDispatch())
    			display.sleep();
    	display.dispose();
    }

	private static void detectFacesAndLoadImage(Shell shell, File file) {
		CascadeClassifier faceDetector = new CascadeClassifier((new File("src/main/resources/lbpcascade_frontalface.xml")).getAbsolutePath());
		Mat image = Highgui.imread(file.getAbsolutePath());
		
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		
		for(Rect rect : faceDetections.toArray())
			Core.rectangle(image,  new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		
		try {
			File tempFile = File.createTempFile("detected", ".png");
			tempFile.deleteOnExit();
			Highgui.imwrite(tempFile.getAbsolutePath(), image);
			loadImage(shell, new FileInputStream(tempFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void loadImage(final Shell shell, final InputStream is) {
		Display.getCurrent().asyncExec(new Runnable(){
			@Override
			public void run() {
				final Image image = new Image(Display.getCurrent(), is);
				shell.addListener(SWT.Paint, new Listener(){
					@Override
					public void handleEvent(Event event) {
						Rectangle rect = image.getBounds();
						event.gc.drawImage(image, 0, 0, rect.width, rect.height, 0, 0, rect.width, rect.height);
					}
				});
				shell.setSize(image.getBounds().width + 6, image.getBounds().height + 28);
			}
		});
	}
}
