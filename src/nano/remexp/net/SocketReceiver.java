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

import java.net.Socket;


/**
 * An interface that enables classes to receive sockets from connectors.
 *  
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public interface SocketReceiver{
	/**
	 * A new socket has been found and was reported, thus adding it
	 * 
	 * @param newSocket The new connected socket.
	 * @param port The local port on which it has been connected.
	 */
    public void newSocket(Socket newSocket, int port);
}
