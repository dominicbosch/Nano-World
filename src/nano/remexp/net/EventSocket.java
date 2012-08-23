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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Hashtable;

import nano.debugger.Debg;
import nano.remexp.CommandExecutor;
import nano.remexp.Parser;

/**
 * The lowest layer within this project that handles the socket events
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public class EventSocket extends NanoSocket {
	private EventSocket me;
	private PrintStream outPs;
	private BufferedReader inBr;
	private String userID;
	private Parser myParser;
	protected EventSocketListener myListener = null;
	private int privilege = NanoComm.PRIV_OBSERVER;
	private boolean isInitialized = false;
	
	/**
	 * Instantiation of an object of this class, starting of a thread that
	 * waits for messages from the socket.
	 * 
	 * @param myClientSocket the socket this thread has to listen on
	 * @param listener the event listener that will be informed about events on this socket
	 */
    public EventSocket(Socket myClientSocket, EventSocketListener chief){
    	super(EventSocket.class.getSimpleName(), myClientSocket);
		outPs = new PrintStream(bos, false);
		inBr = new BufferedReader(new InputStreamReader(bis));
    	me = this;
    	myListener = chief;
    	initParser();
	    setActiveSocket(true);
	    isInitialized = true;
	    Debg.print("JVM uses " + Runtime.getRuntime().totalMemory()/1024/1024 + " Mb memory");
    }

	/**
	 * Initializes the parser and fills it with command handlers
	 */
	protected void initParser() {
		Hashtable<String, CommandExecutor> commands = new Hashtable<String, CommandExecutor>();
		commands.put(NanoComm.CMD_PING, new pingCommandExecutor());
		commands.put(NanoComm.CMD_PONG, new pongCommandExecutor());
		commands.put("lilalogin", new LiLaLoginCommandExecutor());
		commands.put("login", new LoginCommandExecutor());
		myParser = new Parser(commands);
	}

	/**
	 * Listening on the in buffer for events that are being passed to the listener
	 */
	public void doTask() {
		boolean wasSocketCommand;
		String inputLine = "";
		if(isInitialized){
			try {
				while ((inputLine = inBr.readLine()) != null) {
					wasSocketCommand = false;
					this.gotLifeSign();
					if(myParser != null) wasSocketCommand = myParser.executedCommand(inputLine);
					if(!wasSocketCommand){
						if(myListener != null){
							myListener.performSocketEvent(this, inputLine);
						} else {
							Debg.err("No listener has been defined!");
						}
					}
				}
			} catch (IOException e) {
				Debg.err("Socket failed: " + getRemoteID());
				shutDown();
			} catch (NullPointerException ne) {
				Debg.err("NullPointerEsception on: " + getRemoteID());
			}
		}
	}

	@Override
	public void ping(){
		put(NanoComm.strCmd(NanoComm.CMD_PING));
	}
	
	/**
	 * Sends the message through the socket
	 * 
	 * @param message the message to be sent
	 */
	public void put(String message){
		try {
			outPs.print(message + "\015\012"); //TODO println(message)?
			outPs.flush();
		} catch (Exception e) {
			Debg.print("Unable to send " + message + ", shutting down this socket: " + getRemoteID());
			shutDown();
		}
	}
	
	/**
	 * The user ID belonging to this socket if a booking has been made.
	 * 
	 * @return A string containing the user id.
	 */
	public String getUserID() {
		if(userID == null) return "";
		else return userID;
	}
	
	/**
	 * Returns the integer defining the privilege the user has gained through this socket.
	 * Check this class's constants starting with PRIV_* for the available privileges.
	 * 
	 * @return the integer defining the privilege.
	 */
	public int getPrivilege(){
		return privilege;
	}
	
	/**
	 * This informs the user about his new privileges.
	 * 
	 * @param priv the privilege the user on the other end of this socket has now.
	 */
	public void setPrivilege(int priv){
		Debg.print("privilege set to: " + priv + ", old privilege: " + privilege);
		privilege = priv;
		switch(priv){
			case NanoComm.PRIV_ADMIN:
			case NanoComm.PRIV_CONTROLLER:
			case NanoComm.PRIV_OBSERVER:
				put(NanoComm.strPriv(priv));
				break;
			default:
				break;
		}
	}

	/**
	 * The socket removes itself from its listener.
	 */
	protected void removeYourself() {
		if(myListener != null) {
			myListener.removeEventSocket(this);
			Debg.print("removing myself");
		} else Debg.print("no listener, can't remove myself: " + getRemoteID());
	}
	
	/**
	 * calls the calls the super's method too, closes everything and cleans up
	 */
	public synchronized void shutDown() {
		try {
			if(outPs != null) outPs.close();
			if(inBr != null) inBr.close();
		} catch (Exception e) {Debg.err("Unable to close I/O!");}
		privilege = -1;
		outPs = null;
		inBr = null;
		super.shutDown();
		myListener = null;
	}

	/**
	 * The command=ping handler that executes the pong method
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class pingCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			put(NanoComm.strCmd(NanoComm.CMD_PONG));
			//Debg.print("got ping, putting pong");
		}
	}

	/**
	 * The command=pong handler that notifies about a successful ping-pong.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class pongCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			//Debg.print("PingPong successful remote host responded!");
			//me.gotLifeSign(); // life sign is anyways updated if datac oes through
		}
	}
	
	/**
	 * The command=login handler that tries to gain privileges.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class LoginCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			String user = tags.get("username");
			String pass = tags.get("password");
			if(user != null && pass != null) myListener.login(me, user, pass);
		}
	}
	
	/**
	 * The command=login handler that tries to gain privileges.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class LiLaLoginCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			userID = tags.get("userid");
			if(userID != null) myListener.login(me, userID, "");
		}
	}

}
