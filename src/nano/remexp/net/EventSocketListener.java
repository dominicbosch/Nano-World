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


/**
 * An interface that enables classes to act as event socket listeners and
 * perform actions on events
 *  
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public interface EventSocketListener {
	/**
	 * Takes actions if there is an incoming event on the socket.
	 * 
	 * @param sock The socket on which the event has been detected.
	 * @param msg The message that has been sent.
	 */
	public abstract void performSocketEvent(EventSocket sock, String msg);
	
	/**
	 * After the socket hasn't reacted for a while it is removed.
	 * 
	 * @param sock The socket to be removed.
	 */
	public abstract void removeEventSocket(EventSocket sock);

	/**
	 * Used to login the client connection with a username and password.
	 * 
	 * @param sock The socket that tries to gain privileges.
	 * @param user The username used to gain privileges.
	 * @param pass The password passed as a MD-5 hash in case of a non-LiLa login, empty if LiLa login shall be attempted.
	 * @return The privilege this user has now.
	 */
	public abstract void login(EventSocket sock, String user, String pass);
}
