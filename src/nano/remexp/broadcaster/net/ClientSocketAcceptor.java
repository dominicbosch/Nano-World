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

package nano.remexp.broadcaster.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;

/**
 * This class enables to scan on a port for incoming sockets and to add them to the desired listener.
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public class ClientSocketAcceptor extends ThreadHandler{
	private FilteringPool myListener;
	private ServerSocket myEventAcceptSocket;
	private int myPort;

	/**
	 * Instantiates an object of this class and calls the class {@see ThreadHandler} with
	 * this' class name 
	 * 
	 * @param newPort
	 * @param newListener
	 */
    public ClientSocketAcceptor(int newPort, FilteringPool newListener){
    	myEventAcceptSocket = null;
	    myListener = newListener;
	    myPort = newPort;
	    super.start(this.getClass().getSimpleName());
    }

    /**
     * Listening on the predefined port for incoming sockets. Waiting 10 seconds if the port is used already.  
     */
	@Override
	public void doTask() {
        Socket dummySocket;
        Debg.print("Listening for inbound sockets on port: " + myPort);
        try{
            if(myEventAcceptSocket == null) myEventAcceptSocket = new ServerSocket(myPort);
            dummySocket = myEventAcceptSocket.accept();
            myListener.newSocket(dummySocket, myPort);
            Debg.print("Added new socket on port " + myPort + " for remote " 
						+ dummySocket.getInetAddress().getHostAddress() + ":" + dummySocket.getPort());
        }  catch(IOException e){
			try {Thread.sleep(10000);} catch (InterruptedException e1) {Debg.err("Couldn't sleep...");}
	        Debg.err("Error: port " + myPort + " already in use, please free the resource, trying again in 10 seconds...");
        }
	}

	/**
	 * Calling stopThread method, trying to close the ServerSocket and then unlinking all resources. 
	 */
	@Override
	public void shutDown() {
		stopThread();
		if(myEventAcceptSocket != null){
			try {myEventAcceptSocket.close();} catch (IOException e) {Debg.err("Error on shut down");}
			myEventAcceptSocket = null; 
		}
		myListener = null;
	}
}
