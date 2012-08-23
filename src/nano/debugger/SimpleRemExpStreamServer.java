package nano.debugger;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;

/**
 * Used to imitate the remote experiments server by sending events captured by the standard input.
 * 
 * @author dominic
 */
public class SimpleRemExpStreamServer {
	private SimpleRemExpStreamServer server;
	private boolean waitFlag;
	
	public SimpleRemExpStreamServer(){
		server = this;
		waitFlag = false;
		new KeyboardListener();

        Socket echoSocket = null;
        BufferedOutputStream out = null;
        String host = "rafmdmzdsvr.cs.unibas.ch";
        //String host = "131.152.85.135";
        //String host = "localhost";
        
        try {
            echoSocket = new Socket(host, 8014);
            out = new BufferedOutputStream(echoSocket.getOutputStream());
			//new SocketListener(in, this);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + host);
            System.exit(1);
        }
        
        URL url = getClass().getResource("sampleScan.png");
        if(url == null) {
        	System.err.println("Image sampleScan.png not found!");
        	System.exit(-1);
        }
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		BufferedImage bi = null;
        try {
			bi = op.filter(ImageIO.read(url), null);
	        System.out.println("Initialized");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("width: " + bi.getWidth() + ", height: " + bi.getHeight());
		int[] arrInts = null;
		int width, height;
		byte[] arrBytes = new byte[256];
		boolean hasException = false;
        if(bi != null) {
        	if(bi.getHeight() > 256) height = 256;
        	else height = bi.getHeight();
	    	if(bi.getWidth() > 256) width = 256;
	    	else width = bi.getWidth();
        	while(!hasException){
        		for(int i = 0; i < height; i++){
					try {
				    	synchronized(server){
				    		if(waitFlag) server.wait();
				    	}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						i = height;
					}
        			
		        	arrInts = bi.getRGB(0, i, 256, 1, null, 0, 256); 
		        	for(int j = 0; j < width; j++){
		        		arrBytes[j] = (byte)(getPixelData(arrInts[j])[0] - 128);
		        	}
		        	arrBytes[0] = (byte)(i - 128);
			        try {
						out.write(arrBytes,0,256);
				        out.flush();
					} catch (IOException e) {
						e.printStackTrace();
						hasException = true;
						i = height;
					}
		        	try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						hasException = true;
						i = height;
					}
		        	if(i == 0){
		        		for(int j = 0; j < width; j++){
		        			arrBytes[j] = (byte)(j - 128);
		        		}
				        try {
							out.write(arrBytes,0,256);
					        out.flush();
						} catch (IOException e) {
							e.printStackTrace();
							hasException = true;
							i = height;
						}
		        	}
				}
	        }
        }
		try {
			out.close();
			echoSocket.close();
		} catch (IOException e) {}
		System.out.println("Server ended.");
	}

	private static int[] getPixelData(int argb) {
		int rgb[] = new int[] {
		    (argb >> 16) & 0xff, //red
		    (argb >>  8) & 0xff, //green
		    (argb      ) & 0xff  //blue
		};
		return rgb;
	}
	
	public class KeyboardListener implements Runnable{
		BufferedReader stdIn;
		
		public KeyboardListener(){
			stdIn = new BufferedReader(new InputStreamReader(System.in));
			new Thread(this, "Keyboard listener").start();
		}
		
		@Override
		public void run() {
			System.out.println("runs");
			String userInput;
			try {
				while ((userInput = stdIn.readLine()) != null) {
				    System.out.println("echo: " + userInput);
				    if(userInput.matches("stop")) waitFlag = true;
				    else if(userInput.matches("start")) {
				    	waitFlag = false;
				    	synchronized(server){
				    		server.notifyAll();
				    	}
				    }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("ended");
		}
	}
	
	public static void main(String[] args) {
		new SimpleRemExpStreamServer();
	}
}
