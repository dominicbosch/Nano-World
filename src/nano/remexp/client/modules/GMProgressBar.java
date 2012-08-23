package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import Jama.Matrix;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.ThreadHandler;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds a progress bar for the reflection
 * of the scan state during measurements.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMProgressBar implements GUIModule{
	public static final int APPROX_SCANTIME = 495;
	public static final int SCANSIZE = 256;
	public static final int MATRIX_SIZE = 20;
	public static final int NUM_OF_FIXPOINTS = 8;// Can't exceed MATRIX_SIZE
	
	public ClientGUI gui = null;
	private ProgressUpdater progressBarUpdater;
	private JProgressBar progressBar;
	private Matrix matrixX;
	private Matrix matrixY;
	private double m;
	private Date lastChange;
	private long scanStarted;
	private int oldLine;
	private boolean isInitialized = false;
	private Dimension size;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		size = new Dimension(520, 20);
		lastChange = null;
		oldLine = 0;
		scanStarted = 0;
		matrixX = new Matrix(MATRIX_SIZE, 1);
		matrixY = new Matrix(MATRIX_SIZE, 1);
		m = 2;
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		ClientGUI.setupComponent(panel, size, Color.gray);

		progressBar = new JProgressBar(0, SCANSIZE);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		ClientGUI.setupComponent(progressBar, size, Color.cyan);
		progressBarUpdater = new ProgressUpdater();
		progressBarUpdater.start("ProgressBarUpdater");
		progressBarUpdater.doWait(true);
		panel.add(progressBar);
		isInitialized = true;
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){
		try{
			int val = Integer.parseInt(state.substring(NanoComm.COMMAND_STATE.length() + 1));
			switch(val){
				case NanoComm.STATE_SCANNING:
					if(isInitialized) {
						progressBarUpdater.doWait(false);
						Debg.print("finally scanning");
						synchronized(gui){
							gui.notify();
						}
					}
					initLeastSquaresCalc();
				default:
					progressBarUpdater.doWait(true);
					Debg.print("stopped scanning");
					scanStarted = 0;
					lastChange = null;
					oldLine = 0;
			}
		} catch (NumberFormatException e){}
	}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){
		int cmd = Integer.parseInt(Parser.getValue(param, NanoComm.COMMAND_PARAM));
		if(cmd == NanoComm.PARAM_SCANSTART) scanStarted = Long.parseLong(Parser.getValue(param, "value"));
	}

	/**
	 * Initializes the least squares calculation for the scanning time approximation.
	 */
	private void initLeastSquaresCalc(){
		for(int i = 0; i < matrixX.getRowDimension(); i++){
			if(i % 2 == 0){
				matrixX.set(i, 0, 0);
				matrixY.set(i, 0, 0);
			} else {
				matrixX.set(i, 0, SCANSIZE);
				matrixY.set(i, 0, APPROX_SCANTIME);
			}
		}
	}
	
	/**
	 * Updates the progress bar according to the currently scanned line.
	 * 
	 * @param currLine	the line the remote experiment just scanned.
	 */
	private void updateProgress(int currLine){
		if(isInitialized){
			if(scanStarted == 0) {
				progressBar.setString("0:00");
				progressBar.setValue(0);
			} else {
				progressBar.setValue(currLine);
				if(currLine != oldLine) {
					// Keeping NUM_OF_FIXPOINTS init values since we trust them more
					// than the inaccurate intermediate reports,
					// except for the last six lines where we come to the end. 
					int len = NUM_OF_FIXPOINTS;
					if(SCANSIZE - currLine < len) len = SCANSIZE - currLine;
					for(int i = len; i < matrixX.getRowDimension() - 1; i ++){
						matrixX.set(i, 0, matrixX.get(i + 1, 0));
						matrixY.set(i, 0, matrixY.get(i + 1, 0));
					}
					matrixX.set(matrixX.getRowDimension() - 1, 0, currLine);
					matrixY.set(matrixY.getRowDimension() - 1, 0, (new Date().getTime() / 1000) - scanStarted);
					calculateLeastSquares();
					oldLine = currLine;
					lastChange = new Date();
				}
				if(lastChange != null){
					long now = new Date().getTime();
					long diff = now - lastChange.getTime(); 
					if(diff < 10000){
						long seconds = (long) (m * SCANSIZE - (now / 1000) + scanStarted);
						int mins = (int) (seconds / 60);
						seconds = seconds % 60;
						String secs = "";
						if(seconds < 0) secs = "00";
						else if(seconds < 10) secs = "0" + seconds;
						else secs += seconds;
						progressBar.setString(mins + ":" + secs);
					} else {
						progressBar.setString("0:00");
						progressBar.setValue(0);
					}
				}
			}
		}
	}
	
	/**
	 * Calculates the approximated slope of the linear scan time.
	 */
	private void calculateLeastSquares(){
		Matrix aTrans = matrixX.transpose();
		Matrix x = (aTrans.times(matrixX)).inverse().times(aTrans).times(matrixY);
		m = x.get(0, 0);
	}
	
	/**
	 * A thread that updates the progress bar during a measurement.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012 
	 */
	private class ProgressUpdater extends ThreadHandler{
		private boolean iWait = false;
		private void doWait(boolean doWait){iWait = doWait;}
		@Override
		public void doTask() {
			Debg.print("Doing task");
			if(iWait){
				synchronized(gui){
					try {gui.wait();
					} catch (InterruptedException e) {Debg.err("Impossible to wait");}
				}
			}
			if(gui != null) updateProgress(gui.getCurrentLine());
		}
		@Override
		public void shutDown() {
			Debg.print("shutting down");
			stopThread();
			synchronized(gui){
				gui.notify();
			}
		}
	}
	@Override
	public void terminate() {
		progressBarUpdater.shutDown();
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		gui = null;
		size = null;
		matrixX = null;
		matrixY = null;
		lastChange = null;
		progressBar = null;
	}
}

