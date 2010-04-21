package com.ceg.online.jackknife.importer.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author djohnson
 */
public class NodeGroupWriter {
    private static Log log = LogFactory.getLog(NodeGroupWriter.class);

    private Session fromSession;
    private Repository toRepository;
    private Map<String, String> written = new HashMap<String, String>();
    private List<String> current = new ArrayList<String>();
    private Map<String, String> knownPaths = new HashMap<String, String>();

    private Session toSession;
    
    /** Creates a new instance of NodeGroupWriter */
    public NodeGroupWriter(Session from, Repository to) {
        fromSession = from;
        toRepository = to;
    }
    
    private class PathAdder {
        private Session toSession;
        
        private void addPath(Node node) {
            try {
                // check if the path is already known - bail out if so...
                //if(knownPaths.containsKey(node.getParent().getPath())) return;
                
                log.info("Adding path " + node.getPath());
                checkAndAddPath(node.getParent());
                Node parentparent = (Node)toSession.getItem(node.getParent().getParent().getPath());
                parentparent.addNode(node.getParent().getName(), "nt:unstructured");
                toSession.save();
                String addedPath = node.getParent().getPath();
                knownPaths.put(addedPath, addedPath);
            } catch(RepositoryException rex) {
                log.error("RepositoryException in addPath", rex);
            }
        }

        private void checkAndAddPath(Node node) {
            try {
                toSession.getItem(node.getParent().getPath());
            } catch(RepositoryException rex) {
                addPath(node);
            }
        }
        
        public void start(Node node) throws RepositoryException {
            try {
                Credentials credJBoss = new SimpleCredentials("username", "password".toCharArray());
                toSession = toRepository.login(credJBoss);

                checkAndAddPath(node);
            } finally {
                toSession.logout();
                toSession = null;
            }
        }
    }
    
    
    private void copyNode(Node node) throws RepositoryException, SAXException {
        log.info("Starting copy of node at: " + node.getPath());
        new PathAdder().start(node);
        toSession.refresh(true);
        ContentHandler ch = toSession.getImportContentHandler(node.getParent().getPath(), ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        fromSession.exportSystemView(node.getPath(), ch, false, true);
    }
    
    public void writeNodeAndReferents(Node node) throws RepositoryException, SAXException {
        // This might be a good addition
        //if(written.containsKey(node.getUUID())) return;
        try {
            Credentials credJBoss = new SimpleCredentials("username", "password".toCharArray());
            toSession = toRepository.login(credJBoss);

            log.info("starting write of node " + node.getPath());
            createSubgraph(node);

            copyNode(node);
            Iterator<String> iter = current.listIterator();
            while(iter.hasNext()) {
                String uuid = iter.next();
                Node curNode = fromSession.getNodeByUUID(uuid);
                copyNode(curNode);
                log.info("\tWrote - " + curNode.getPath());
                written.put(uuid, uuid);
            }
            toSession.save();

            current.clear();
            log.info("session saved");
        } finally {
            toSession.logout();
            toSession = null;
        }
    }
    
    
    private void processUUIDForSubgraph(String uuid) throws RepositoryException {
        if(written.containsKey(uuid)) return;
        
        if(!current.contains(uuid)) {
            current.add(uuid);
            Node node = fromSession.getNodeByUUID(uuid);
            
            log.info("\tADDING " + node.getPath());
           
            createSubgraph(node);
        }
    }

    
    public void createSubgraph(Node node) {
        try {
            PropertyIterator piProps = node.getProperties();
            while(piProps.hasNext()) {
                Property prop = piProps.nextProperty();
                
                // skip jcr properties/references
                if(prop.getName().startsWith("jcr:")) continue;
                
                if(prop.getType() == PropertyType.REFERENCE) {

                    if(prop.getDefinition().isMultiple()) {
                        Value [] values = prop.getValues();
                        
                        for(int i = 0 ; i < values.length; i++) {
                            processUUIDForSubgraph(values[i].getString());
                        }
                    } else {
                        processUUIDForSubgraph(prop.getValue().getString());
                    }
                }
            }
        } catch(RepositoryException rex) {
            log.error("RepositoryException in createSubgraph ", rex);
        }
    }
}
