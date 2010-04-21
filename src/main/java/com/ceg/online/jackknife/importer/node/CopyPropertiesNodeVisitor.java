package com.ceg.online.jackknife.importer.node;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author jstanton
 * @author djohnson
 */
public class CopyPropertiesNodeVisitor implements NodeVisitor {
    private static Log log = LogFactory.getLog(CopyPropertiesNodeVisitor.class);
    
    private Session toSession;
    private JackrabbitImporter importer;
    
    /** Creates a new instance of CopyPropertiesNodeVisitor */
    public CopyPropertiesNodeVisitor(Session to, String host) {
        toSession = to; 
        importer = new JackrabbitImporter(to);
    }
    
    public void visitNode(Node fromNode) {
        DynaBean dynabean = new LazyDynaBean();
        try {
            if(!fromNode.hasProperties()) return;
            
            PropertyIterator pi = fromNode.getProperties();
                        
            while(pi.hasNext()) {
                Property prop = pi.nextProperty();
                // Skip the system properties
                if(prop.getName().startsWith("jcr:")) continue;
                //log.info("processing property - " + prop.getName());
                if(prop.getDefinition().isMultiple()) {
                    dynabean.set(prop.getName(), prop.getValues());
                } else {
                    dynabean.set(prop.getName(), prop.getValue());
                }
            }
            
            if(fromNode.isNodeType("mix:referenceable")) {
                importer.saveNodeAtUUID(fromNode.getUUID(), dynabean);
            } else {
                importer.saveNodeAtPath(fromNode.getPath(), dynabean);
            }
        } catch(RepositoryException rex) {
            log.error("RepositoryException", rex);
        }
    }
    
    public void visitNodeOld(Node fromNode) {
        Node dynabean=null;
        try {
            if(!fromNode.hasProperties()) return;
            
            PropertyIterator pi = fromNode.getProperties();
            
            if(fromNode.isNodeType("mix:referenceable")) {
                log.info("getting referenceable node - " + fromNode.getPath() + ":" + fromNode.getUUID());
                dynabean = toSession.getNodeByUUID(fromNode.getUUID());
            } else {
                log.info("getting non-referenceable node - " + fromNode.getPath());
                dynabean = (Node)toSession.getItem(fromNode.getPath());
            }
            
            while(pi.hasNext()) {
                Property prop = pi.nextProperty();
                // Skip the system properties
                if(prop.getName().startsWith("jcr:")) continue;
                log.info("processing property - " + prop.getName());
                if(prop.getDefinition().isMultiple()) {
                    dynabean.setProperty(prop.getName(), prop.getValues());
                } else {
                    dynabean.setProperty(prop.getName(), prop.getValue());
                }
            }
            toSession.save();
        } catch(RepositoryException rex) {
            log.error("RepositoryException", rex);
        }
    }
    
    public void finish() {
        importer.finishImport();
    }
}
