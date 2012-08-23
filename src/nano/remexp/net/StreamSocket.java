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

package nano.remexp.net;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.StreamReceiver;

/**
 * This class handles the stream sockets that receive stream data from the
 * remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public class StreamSocket extends NanoSocket{
    private Vector<StreamReceiver> myDisplays = new Vector<StreamReceiver>();
    private StreamSocketInterface chief;
    /**
     * The constructor creates an in and out buffer on the socket.
     * 
     * @param sock The socket over which the data streaming happens.
     */
    public StreamSocket(Socket sock, StreamSocketInterface ssi){
    	super(StreamSocket.class.getSimpleName(), sock);
    	chief = ssi;
    }

    /**
     * The lifecycle loop of this thread that checks the socket for incoming
     * data and sends it to its connected displays. 
     */
    public void doTask(){
		byte theLine[] = new byte[256];
		try{
			while(bis.read(theLine, 0, 256)!=-1){
			    for(StreamReceiver sr: myDisplays) sr.write(theLine);
			}
			Debg.err("bis.read was -1: " + getRemoteID());
		} catch(IOException e) {
			Debg.err("Connection lost, shutting this socket down for " + getRemoteID());
    	}
		theLine = null;
		shutDown();
    }

    /**
     * Adds a display in order for it to treceive the stream too.
     * 
     * @param newDisplay The new display that also wants to receive the streamed data.
     */
    public void plugDisplay(StreamReceiver newDisplay){
    	if(myDisplays != null) myDisplays.add(newDisplay);
    }

	@Override
	public void ping() {} // Ping is not necessary for the data stream
    
	@Override
	protected void removeYourself() {
		if(chief != null) chief.removeStreamSocket(this);
	}

    /**
     * Shuts down the connection and sets all references to null.
     */
    public void shutDown(){
    	if(myDisplays != null) myDisplays.clear();
    	myDisplays = null;
		super.shutDown();
		chief = null;
    }

}
