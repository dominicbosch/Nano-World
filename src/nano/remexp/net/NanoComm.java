package nano.remexp.net;

/**
 * The bits and pieces of the communication protocol.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class NanoComm {
	private NanoComm(){}
	
	public static final String DELIMITER = "=";
	
	public static final String COMMAND_CMD = "command";
	public static final String CMD_CALIBRATESTAGE = "calibratestage";
	public static final String CMD_AUTOAPPROACH = "autoapproach";
	public static final String CMD_STOPAPPROACH = "stopapproach";
	public static final String CMD_STOP = "stop";
	public static final String CMD_START = "start";
	public static final String CMD_WITHDRAW = "withdraw";
	public static final String CMD_SCANRANGE = "scanrange";
	public static final String CMD_MOVETIP = "movetip";
	public static final String CMD_CAMANGLE = "camangle";
	public static final String CMD_PING = "ping";
	public static final String CMD_PONG = "pong";

	public static final String COMMAND_STATE = "remexpstate";
	public static final int STATE_STAGEREADY = 10; 			// AFM and stage ready
	public static final int STATE_STAGEMOVING = 11; 		// stage moving
	public static final int STATE_STAGECALIBRATED = 12; 	// stage calibrated
	public static final int STATE_APPROACHING = 20; 		// AFM approaches
	public static final int STATE_APPROACHED = 21; 			// AFM approached, not scanning
	public static final int STATE_SCANNING = 22; 			// AFM approached, scanning
	public static final int STATE_WITHDRAWING = 23; 		// AFM withdraws

	public static final String COMMAND_INFO = "cbrinfo";
	public static final int INFO_REMEXP_CONNECTED = 201; 	// RemExp connected
	public static final int INFO_REMEXP_DISCONNECTED = 202; // RemExp not connected

	public static final String COMMAND_PRIV = "cbrpriv";
	public static final int PRIV_OBSERVER = 401; 			// observer privileges
	public static final int PRIV_CONTROLLER = 411; 			// controller privileges
	public static final int PRIV_ADMIN = 421; 				// admin privileges
	
	public static final String COMMAND_PARAM = "remexpparam";
	public static final int PARAM_SCANRANGE = 501; 			// scan range changes
	public static final int PARAM_STAGEPOSITION = 502; 		// position changes
	public static final int PARAM_SCANSTART = 503; 			// start of last measurement
	//public static final int PARAM_CAMANGLE = 504;// we don't inform the client about the camera angle
	public static final int PARAM_SAMPLEINFO = 511; 		// sample information
	public static final int PARAM_SAMPLESCLEAR = 512; 		// clear all samples from GUI
	public static final int PARAM_REMEXPNAME = 521; 		// RemExp name

	public static String strCmd(String cmd){return COMMAND_CMD + DELIMITER + cmd;}
	public static String strState(int state){return COMMAND_STATE + DELIMITER + state;}
	public static String strInfo(int info){return COMMAND_INFO + DELIMITER + info;}
	public static String strParam(String params){return COMMAND_PARAM + DELIMITER + params;}
	public static String strPriv(int val){return COMMAND_PRIV + DELIMITER + val;}
	
}
