package nano.remexp.broadcaster;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nano.debugger.Debg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This object parses the given xml file and initializes certain important information on the
 * remote experiment broadcaster.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class XMLConfigParser {
	Document doc;
	RemoteExperimentBroadcaster cbr;
	
	/**
	 * Initializes and opens the configuration file.
	 * 
	 * @param path	the path to the xml configuration file
	 * @param c 	the RemoteExperimentBroadcaster that receives commands from the parsed xml file.
	 */
	protected XMLConfigParser(String path, RemoteExperimentBroadcaster c){
		File file = new File(path);
		cbr = c;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
		} catch (SAXException e) {} 
		catch (IOException e) {}
		catch (ParserConfigurationException e) {}
	}
	
	/**
	 * This function processes the whole file that was opened during construction time.
	 * The processed parameters are parsed into commands on the RemoteExperimentBroadcaster.
	 * 
	 * @return 		true if everything worked fine, false if an error occurred 
	 * 				and the server wasn't initialized properly.
	 */
	protected boolean processFile() {
		boolean isFine;
		if(doc == null) return false;
		if(doc.getDocumentElement().getNodeName() != "cbrinit"){
			System.err.println("Failed initializing configuration script! Quitting!");
			return false;
		}
		NodeList topNodes = doc.getChildNodes();
		for(int i = 0; i < topNodes.getLength(); i++) if(topNodes.item(i).getNodeName()=="cbrinit"){
			topNodes = topNodes.item(i).getChildNodes();
			Node tmpNode;
			String nme;
			for(int j = 0; j < topNodes.getLength(); j++){
				isFine = true;
				tmpNode = topNodes.item(j);
	            if(tmpNode.getNodeType() == Node.ELEMENT_NODE) {
	            	nme = ((Element)tmpNode).getNodeName();
	            	if(nme.equals("debugmode")) setDebugMode(tmpNode);
	            	if(nme.equals("initremexpname")) initRemexpName(tmpNode);
	            	if(nme.equals("initremexpports")) isFine = initRemexpPorts(tmpNode);
	            	if(nme.equals("initclientports")) isFine = initClientPorts(tmpNode);
	            	if(nme.equals("addallowedhosts")) isFine = addAllowedHosts(tmpNode);
	            	if(nme.equals("adduser")) addUser(tmpNode);
	            	if(nme.equals("addsample")) addSample(tmpNode);
	            	if(nme.equals("addrig")) addRig(tmpNode);
	            }
	            if(!isFine) return false;
			}
			cbr.finishedInitialization();
			return true;
		}
		return false;
	}

	/**
	 * Reads the debug flag and file extension for the log file.
	 * 
	 * @param n		the node with the appropriate information
	 */
	private void setDebugMode(Node n){
		String mode = getNodeValue(n, "mode");
		String log = getNodeValue(n, "logfileext");
		if(mode != null && log != null){
			try{
				cbr.initLogger(log, Integer.parseInt(mode));
			} catch(Exception e){Debg.err("Error in initializing Debugger: error in initializing debug mode: " + mode + ", " + log);e.printStackTrace();}
		} else Debg.err("Error in initializing Debugger");
	}

	/**
	 * Sets the name of the remote experiment.
	 * 
	 * @param n		the node with the appropriate information
	 */
	private void initRemexpName(Node n){
		String rn = getNodeValue(n, "name");
		if(rn != null) cbr.setRemExpName(rn);
	}

	/**
	 * Initializes the ports on which the server listens for the remote experiment to connect. 
	 * 
	 * @param n		the node with the appropriate information
	 * @return 		true if everything worked fine, false if an 
	 * 				error occurred and the server wasn't initialized properly.
	 */
	private boolean initRemexpPorts(Node n){
		String ep = getNodeValue(n, "eventport");
		String sp = getNodeValue(n, "streamport");
		if(ep != null && sp != null){
			try{
				cbr.initRemExpPorts(Integer.parseInt(ep), Integer.parseInt(sp));
				return true;
			} catch(Exception e){
				Debg.err("Error in initializing RemExpPorts: no cast to int possible" + ep + ", " + sp);
				e.printStackTrace();
			}
		} else Debg.err("Error in initializing RemExpPorts");
		return false;
	}

	/**
	 * Initializes the ports on which the server listens for connecting clients.
	 * 
	 * @param n		the node with the appropriate information
	 * @return 		true if everything worked fine, false if an
	 * 				error occurred and the server wasn't initialized properly.
	 */
	private boolean initClientPorts(Node n){
		String ep = getNodeValue(n, "eventport");
		String sp = getNodeValue(n, "streamport");
		if(ep != null && sp != null){
			try{
				cbr.initClientPorts(Integer.parseInt(ep), Integer.parseInt(sp));
				return true;
			} catch(Exception e){
				Debg.err("Error in initializing RemExpPorts: no cast to int possible: " + ep + ", " + sp);
				e.printStackTrace();
			}
		} else Debg.err("Error in initializing RemExpPorts");
		return false;
	}

	/**
	 * Initializes the hosts that are allowed to connect to the server as remote experiment.
	 * 
	 * @param n		the node with the appropriate information
	 * @return 		true if everything worked fine, false if an
	 * 				error occurred and the server wasn't initialized properly.
	 */
	private boolean addAllowedHosts(Node n){
		NodeList nl = ((Element) n).getElementsByTagName("host");
		if(nl != null){
			for(int i = 0; i < nl.getLength(); i++){
				NodeList lstEl = ((Element) nl.item(i)).getChildNodes();
				if (lstEl != null)  {
					if(lstEl.item(0) != null) {
						cbr.addRemExpHost(((Node) lstEl.item(0)).getNodeValue());
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Extracts and stores an user, the password and privilege in order to grant special access rights to the server.
	 * 
	 * @param n		the node with the appropriate information
	 */
	private void addUser(Node n){
		cbr.addUser(getNodeValue(n, "username"), getNodeValue(n, "password"), getNodeValue(n, "privilege"));
	}

	/**
	 * Extracts and stores the parameters needed for a sample definition.
	 * 
	 * @param n		the node with the appropriate information
	 */
	private void addSample(Node n){
		cbr.addSample(getNodeValue(n, "name"), getNodeValue(n, "commandmoveto"), getNodeValue(n, "commandreleaselock"),
				getNodeValue(n, "posx"), getNodeValue(n, "posy"), getNodeValue(n, "deltax"), getNodeValue(n, "deltay"));
	}

	/**
	 * Extracts and stores the parameter needed for a rig definition.
	 * The rig can also be used to grant access rights to users.
	 * 
	 * @param n		the node with the appropriate information
	 */
	private void addRig(Node n){
		String rig = getNodeValue(n, "rigname");
		if(rig != null) cbr.addRig(rig);
	}

	/**
	 * Extracts an elements value from a node. 
	 * 
	 * @param n		the node with the appropriate information
	 * @return		the value of the desired element within the node
	 */
	private String getNodeValue(Node n, String attr){
		NodeList nl = ((Element) n).getElementsByTagName(attr);
		if(nl != null){
			Element fstEl = (Element) nl.item(0);
			if(fstEl != null){
				NodeList lstEl = fstEl.getChildNodes();
				if (lstEl != null)  {
					if(lstEl.item(0) != null) return((Node) lstEl.item(0)).getNodeValue();
				}
			}
		}
		return null;
	}
}