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

public class XMLConfigParser {
	Document doc;
	RemoteExperimentBroadcaster cbr;
	
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
	
	protected boolean processFile() {
		if(doc == null) return false;
		if(doc.getDocumentElement().getNodeName() != "cbrinit"){
			System.err.println("Failed initializing configuration script! Quitting!");
			return false;
		}
		NodeList topNodes = doc.getChildNodes().item(0).getChildNodes();
		Node tmpNode;
		String nme;
		for(int i = 0; i < topNodes.getLength(); i++){
			tmpNode = topNodes.item(i);
            if(tmpNode.getNodeType() == Node.ELEMENT_NODE) {
            	nme = ((Element)tmpNode).getNodeName();
            	if(nme.equals("debugmode")) setDebugMode(tmpNode);
            	if(nme.equals("initremexpname")) initRemexpName(tmpNode);
            	if(nme.equals("initremexpports")) initRemexpPorts(tmpNode);
            	if(nme.equals("initclientports")) initClientPorts(tmpNode);
            	if(nme.equals("addallowedhosts")) addAllowedHosts(tmpNode);
            	if(nme.equals("adduser")) addUser(tmpNode);
            	if(nme.equals("addsample")) addSample(tmpNode);
            	if(nme.equals("addrig")) addRig(tmpNode);
            }
		}
		cbr.finishedInitialization();
		return true;
	}

	private void setDebugMode(Node n){
		String cons = getNodeValue(n, "console");
		String log = getNodeValue(n, "logfile");
		if(cons != null && log != null){
			try{
				cbr.initLogger(Integer.parseInt(cons), Integer.parseInt(log));
			} catch(Exception e){Debg.err("Error in initializing Debugger: no cast to int possible: " + cons + ", " + log);e.printStackTrace();}
		} else Debg.err("Error in initializing Debugger");
	}

	private void initRemexpName(Node n){
		String rn = getNodeValue(n, "name");
		if(rn != null) cbr.setRemExpName(rn);
	}
	
	private void initRemexpPorts(Node n){
		String ep = getNodeValue(n, "eventport");
		String sp = getNodeValue(n, "streamport");
		if(ep != null && sp != null){
			try{
				cbr.initRemExpPorts(Integer.parseInt(ep), Integer.parseInt(sp));
			} catch(Exception e){Debg.err("Error in initializing RemExpPorts: no cast to int possible" + ep + ", " + sp);e.printStackTrace();}
		} else Debg.err("Error in initializing RemExpPorts");
	}

	private void initClientPorts(Node n){
		String ep = getNodeValue(n, "eventport");
		String sp = getNodeValue(n, "streamport");
		if(ep != null && sp != null){
			try{
				cbr.initClientPorts(Integer.parseInt(ep), Integer.parseInt(sp));
			} catch(Exception e){Debg.err("Error in initializing RemExpPorts: no cast to int possible: " + ep + ", " + sp);e.printStackTrace();}
		} else Debg.err("Error in initializing RemExpPorts");
	}

	private void addAllowedHosts(Node n){
		NodeList nl = ((Element) n).getElementsByTagName("host");
		if(nl != null){
			for(int i = 0; i < nl.getLength(); i++){
				NodeList lstEl = ((Element) nl.item(i)).getChildNodes();
				if (lstEl != null)  {
					if(lstEl.item(0) != null) cbr.addRemExpHost(((Node) lstEl.item(0)).getNodeValue());
				}
			}
		}
	}

	private void addUser(Node n){
		cbr.addUser(getNodeValue(n, "username"), getNodeValue(n, "password"), getNodeValue(n, "privilege"));
	}

	private void addSample(Node n){
		cbr.addSample(getNodeValue(n, "name"), getNodeValue(n, "commandmoveto"), getNodeValue(n, "commandreleaselock"),
				getNodeValue(n, "posx"), getNodeValue(n, "posy"), getNodeValue(n, "deltax"), getNodeValue(n, "deltay"));
	}

	private void addRig(Node n){
		String rig = getNodeValue(n, "rigname");
		if(rig != null) cbr.addRig(rig);
	}
	
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