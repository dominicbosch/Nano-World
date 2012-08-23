package nano.debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import nano.remexp.broadcaster.locks.ScanningLock;

/**
 * Used to imitate the remote experiments server by sending events captured by the standard input.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class SimpleRemExpEventServer {
	public SimpleRemExpEventServer(){
		boolean doRun = true;
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        //String host = "131.152.85.135";
        String host = "127.0.0.1";
        //String host = "rafmdmzdsvr.cs.unibas.ch";
        try {
            echoSocket = new Socket(host, 8012);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + host);
            System.exit(1);
        }

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String clientInput, answer;
		System.out.println("Initialized");
		try {
			while(doRun){
				answer = "";
				clientInput = in.readLine();
				if(clientInput.indexOf("command=ping") == -1) System.out.println("Received from CBR: " + clientInput);
				answer = processClientInput(clientInput);
				if(answer.equals("")) {
					System.out.println("Can't handle this CBR information automatically, please enter manual answer:");
					answer = stdIn.readLine();
				}
				try {
					Thread.sleep(1000); // Delay the answer so things go not to fast
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			    out.println(answer);
			    if(clientInput.indexOf("command=ping") == -1) System.out.println("Sent answer: " + answer);
			    if(answer == "exit") doRun = false;
			}
		} catch (IOException e) {}
	
		out.close();
		try {
			in.close();
			stdIn.close();
			echoSocket.close();
		} catch (IOException e) {}
		System.out.println("Server ended.");
	}

	private String processClientInput(String input){
		String answer = "";
		if(input.equals("command=goto0")) answer = "P0 OK";
		else if(input.equals("command=goto1")) answer = "P1 OK";
		else if(input.equals("command=goto2")) answer = "P2 OK";
		else if(input.equals("command=goto3")) answer = "P3 OK";
		else if(input.equals("command=goto4")) answer = "P4 OK";
		else if(input.equals("command=autoapproach")) answer = "StatusApproached";
		else if(input.equals("command=start")) answer = "started measurment";
		else if(input.equals("command=stop")) answer = "stopped measurment";
		else if(input.equals("command=withdraw")) answer = "Withdrawn";
		else if(input.equals("command=ping")) answer = "command=pong";
		else if(input.equals("command=calibratestage")) answer = "Instrument Calibrated";
		else if(input.equals("command=videoa")) answer = "StatusPrompt";
		else if(input.equals("command=videob")) answer = "StatusPrompt";
		else if(input.startsWith("command=adjustaxis")) answer = "Customposition : ";
		else if(input.startsWith("command=set name=scanrange")) answer = "savage";
		else answer = "I had no idea what to do...";
		return answer;
	}
	
	public class SocketListener implements Runnable{
		BufferedReader in;
		SimpleRemExpEventServer server;
		
		public SocketListener(BufferedReader input, SimpleRemExpEventServer serv){
			server = serv;
			in = input;
			System.out.println("constructed");
			new Thread(this, "Socket listener").start();
			System.out.println("started");
		}
		
		@Override
		public void run() {
			System.out.println("runs");
			String userInput;
			try {
				while ((userInput = in.readLine()) != null) {
				    System.out.println("echo: " + userInput);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("ended");
		}
	}
	
	public static void main(String[] args) {
		new SimpleRemExpEventServer();
	}
}
