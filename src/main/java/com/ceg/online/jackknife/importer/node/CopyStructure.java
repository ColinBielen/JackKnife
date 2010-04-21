package com.ceg.online.jackknife.importer.node;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ceg.online.jackknife.session.SessionFactory;


/**
 *
 * @author jstanton
 * @author djohnson
 */
public class CopyStructure {
    private static Log log = LogFactory.getLog(CopyStructure.class);
    private Session fromSession, toSession;
    
    /** Creates a new instance of CopyStructure */
    public CopyStructure(String fromHost, String toHost, boolean fromJCR, boolean toJCR) {
        try {
        	fromSession = SessionFactory.getJCRSession(fromHost, !fromJCR); 
        	toSession = SessionFactory.getJCRSession(toHost, !toJCR);
        } catch (RepositoryException ex) {
            ex.printStackTrace();
        }
    }
    
    
    public void visitChildren(NodeVisitor nv, Node parent) {
        try {
            log.info("Copying children of " + parent.getPath());
            NodeIterator ni = parent.getNodes();

            while(ni.hasNext()) {
                Node cur = ni.nextNode();
                nv.visitNode(cur);
                visitChildren(nv, cur);
            }
        } catch(RepositoryException rex) {
            log.error("RepositoryException in copyChildren(Node)", rex);
        }
    }
    
    public void start(String host) throws ParserConfigurationException, RepositoryException, TransformerConfigurationException, IOException {
    	NodeIterator ni = fromSession.getRootNode().getNodes();
    	log.debug("Starting NODE copy...");
        while(ni.hasNext()) {
    		Node node = (Node)ni.next();
    		String nodeName = node.getName();
            
            if(!nodeName.startsWith("jcr:") && !nodeName.startsWith("rep:")) {
	    		toSession.getRootNode().addNode(nodeName, node.getPrimaryNodeType().getName());
		        // Assuming an empty destination repository
		        toSession.save();
		        // First copy the basic node structure of the repository - nodes, node types, uuids, mixins
		        CopyBasicNodeVisitor cbnv = new CopyBasicNodeVisitor(toSession, host);
		        visitChildren(cbnv, fromSession.getRootNode().getNode(nodeName));
                toSession.save();
            }
        }
            ni = fromSession.getRootNode().getNodes();
            log.debug("Starting REFERENCES copy...");
        while(ni.hasNext()) {
    		Node n = (Node)ni.next();
    		String nn = n.getName();
            if(!nn.startsWith("jcr:") && !nn.startsWith("rep:")) {
	    	    CopyPropertiesNodeVisitor cpnv = new CopyPropertiesNodeVisitor(toSession, host);
		        visitChildren(cpnv, fromSession.getRootNode().getNode(nn));
                toSession.save();

            }





        }
    }
    
    static public void main(String [] argv) throws ParserConfigurationException, RepositoryException, IOException, TransformerConfigurationException {
      //  FileAppender fileAppender = new FileAppender(new PatternLayout(), "copystructure.log");
    //    BasicConfigurator.configure(fileAppender);
    	boolean fromJCR = true;
    	boolean toJCR = true;
    	String toHost = "localhost";
    	String fromHost = "localhost";
        for(int i = 0; i < argv.length; i++) {
        	String param = argv[i];
        	if(param.equals("-fromCRX")) fromJCR = false;
        	else if(param.equals("-toCRX")) toJCR = false;
        	else if (param.equals("-toHost")) {
				toHost = argv[i+1];
				i++;
				continue;
			} else if (param.equals("-fromHost")) {
				fromHost = argv[i+1];
				i++;
				continue;
			} else log.info("Unknown parameter: "+param);
        }
        CopyStructure cs = new CopyStructure(fromHost, toHost, fromJCR, toJCR);
        cs.start(toHost);
        cs.finish();
    }

    /**
     * Closes all JCR sessions.
     * Does NOT save anything: Do that before you call this...
     * @throws RepositoryException
     */
    public void finish() throws RepositoryException {
      fromSession.logout();
      toSession.logout();
    }

}
