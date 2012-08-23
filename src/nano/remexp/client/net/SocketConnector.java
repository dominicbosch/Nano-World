/*
 * Copyright (c) 2011 by Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch and The 
 * Regents of the University of Basel. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF BASEL BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * BASEL HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF BASEL SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF BASEL HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Authors: Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch <vexp@nano-world.net>
 * 
 */ 

package nano.remexp.client.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;
import nano.remexp.client.ClientApplet;

/**
 * This thread tries to connect to to the remote experiment's broadcaster.
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public class SocketConnector extends ThreadHandler{
	private ClientApplet myListener;
    private Socket socketConnector;
    private String remoteURL;
	private int remotePort;

	/**
	 * Instantiates an object of this class and starts to try connecting to the remote
	 * experiment's host address. 
	 * 
	 * @param listener The applet that handles the communication coming from and going to this socket.
	 * @param url The remote experiment's host URL.
	 * @param newPort The remote experiment's host port.
	 */
    public SocketConnector(ClientApplet listener, String url, int newPort){
    	myListener = listener;
	    remoteURL = url;
	    remotePort = newPort;
	    super.start(this.getClass().getSimpleName() + " -> " + url + ":" + newPort);
	    Debg.print("Started listening for new event sockets on port " + newPort);
    }

    /**
     * The lifecycle loop of this thread tries in intervals to connect to a remote host
     * and passes the acquired socket to the listener that is in need for this socket.
     */
	@Override
	public void doTask() {
		if(socketConnector == null){
			try {
				socketConnector = new Socket(remoteURL, remotePort);
	            Debg.print("Added new socket for remote " 
						+ socketConnector.getInetAddress().getHostAddress() + ":" + socketConnector.getPort());
	            if(myListener != null){
	            	myListener.newSocket(socketConnector, remotePort);
	            }
	        } catch (UnknownHostException e) {
	        } catch (IOException e) {}
		} else { // we are already connected, thus we wait until we are good to try again for an available connection
			synchronized(myListener){
				try { myListener.wait();
				} catch (InterruptedException e1) {Debg.err("Couldn't wait on release lock!");}
			}
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
	}
	/*
	public void restartConnecting(){
		releaseSocket();
		if(!this.isAliveThread()) restart();
	}*/
	
	public void setHost(String url, int port){
		setURL(url);
		setPort(port);
	}
	
	public void setURL(String url){
		remoteURL = url;
	}
	
	public void setPort(int port){
		remotePort = port;
	}

	public void releaseSocket(){
		if(socketConnector != null){
			try {
				socketConnector.close();
			} catch (IOException e) {e.printStackTrace();}
			socketConnector = null;
		}
		synchronized(myListener){myListener.notifyAll();}
	}
	
	/**
	 * Shutting down this thread by setting everything to null.
	 */
	@Override
	public void shutDown() {
		stopThread();
		releaseSocket();
		myListener = null;
	}
}
