package com.ceg.online.jackknife.importer.nodetype;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader;
import org.apache.jackrabbit.name.QName;

import com.ceg.online.jackknife.session.SessionFactory;
import com.ceg.online.jackknife.session.JackrabbitProvider;
import com.ceg.online.jackknife.util.StringUtils;
import org.apache.jackrabbit.spi.Name;

/**
 * This is a jackrabbit specific node type importer.
 * @author root
 *
 */
public class JackrabbitCNFImporter {
    private static Log log = LogFactory.getLog(JackrabbitCNFImporter.class);
    private Session session;
    
    /** Creates a new instance of JackrabbitCNFImporterBean 
     * @throws RepositoryException */
    public JackrabbitCNFImporter(String host) throws RepositoryException {
    	session = SessionFactory.getJCRSession(host, false);
    }

    /**
     * Set a session explicitly: Mainly for unit testing.
     * @param s
     */
    protected JackrabbitCNFImporter(Session s) {
        session = s;        
    }

    public JackrabbitCNFImporter(String configFile,String homeDir) throws RepositoryException {
          try {
              session = new JackrabbitProvider().getLocalSession(configFile,homeDir);
          } catch(IOException e) {
              throw new RepositoryException(e);
          }
    }

    public void updateCNF(InputStream stream) {
    	doImport(stream, true, "", "");
	}

    public void importCNF(InputStream stream) {
    	doImport(stream, false, "", "");
    }

    public void doImport(InputStream stream, boolean update, String namespace, String namespacePath) {
    	try {
            Workspace ws = session.getWorkspace();
            //TODO: how do you know what namespace to create?? "eol", "http://jcr.eonline.com"
            if(!StringUtils.isNullOrEmpty(namespace) && !StringUtils.isNullOrEmpty(namespacePath)) ws.getNamespaceRegistry().registerNamespace(namespace, namespacePath);
            registerCustomNodeTypes(ws, stream, update);
            session.save();
        } catch(Exception fuex) {
            fuex.printStackTrace();
        } finally {
        	session.logout();
        }
    }
    
    @SuppressWarnings("unchecked")
	private void registerCustomNodeTypes(Workspace ws, InputStream is, boolean update) throws Exception {
        Reader reader = new InputStreamReader(is);
        
        // Create a CompactNodeTypeDefReader
        CompactNodeTypeDefReader cndReader = new CompactNodeTypeDefReader(reader, "System");

        // Get the List of NodeTypeDef objects
        List<NodeTypeDef> ntdList = (List<NodeTypeDef>)cndReader.getNodeTypeDefs() ;

        // Get the NodeTypeManager from the Workspace.
        // Note that it must be cast from the generic JCR NodeTypeManager to the
        // Jackrabbit-specific implementation.
        Object o = ws.getNodeTypeManager();
        log.info("I wanted a NodeTypeManagerImpl but got a: "+o.getClass());
        NodeTypeManagerImpl ntmgr =(NodeTypeManagerImpl)o;

        // Acquire the NodeTypeRegistry
        NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
        // If this is an update then only unregister the ones we know we're gonna load.
        if(update) ntreg.unregisterNodeTypes(ntdList);
        // If this is a pure load then unregister ALL node types.
        else {

            Name names[] = ntreg.getRegisteredNodeTypes();
            List customNodeTypes = new ArrayList();
            //It doesn't let you unregister built in nodetypes, so we skip those...
            for(int i=0;i<names.length;i++) {
                if(!(names[i].getNamespaceURI().startsWith("http://www.jcp.org")) &&
                   !(names[i].getNamespaceURI().startsWith("internal"))) {
                    customNodeTypes.add(names[i]);
                    log.info("Unregistering nodetype "+names[i].getNamespaceURI()+"...");
                }
            }
            ntreg.unregisterNodeTypes(customNodeTypes);
        }
        // Loop through the prepared NodeTypeDefs
        for (Iterator<NodeTypeDef> i = ntdList.iterator(); i.hasNext();) {
            // Get the NodeTypeDef...
            NodeTypeDef ntd = (NodeTypeDef)i.next();
        	log.info("About to register node type: "+ntd.getName());
            // ...and register it
			ntreg.registerNodeType(ntd);
        }
    }
    
    public static void main(String[] args) {
    	String host = "";
        String configFile ="";
        String homeDir ="";
        boolean update = false;
    	String filePath = "";
    	String namespace = "";
    	String namespacePath = "";
    	for(int i = 0; i < args.length; i++) {
    		String param = args[i];
    		if(param.equals("-host")) {
    			host = args[i+1];
    			continue;
    		} else if(param.equals("-update")) {
    			update = true;
    		} else if(param.equals("-file")) {
    			filePath = args[i+1];
    			continue;
            } else if(param.equals("-repHome")) {
    			homeDir = args[i+1];
    			continue;
            } else if(param.equals("-repConfig")) {
                 configFile = args[i+1];
                 continue;

            } else if(param.equals("-namespace")) {
    			namespace = args[++i];
    			namespacePath = args[++i];
    			continue;
    		}
    	}
    	if(!filePath.equals("")) {
	    	try {
	    		FileInputStream stream = new FileInputStream(filePath);
                if(!host.equals("")) {
                    new JackrabbitCNFImporter(host).doImport(stream, update, namespace, namespacePath);
                } else {
                    new JackrabbitCNFImporter(configFile,homeDir).doImport(stream, update, namespace, namespacePath);
                }
            } catch(RepositoryException rex) {
	    		log.error("RepositoryException occurred while trying to import CNF items.", rex);
	    	} catch (FileNotFoundException e) {
	    		log.error("Could not find file: "+filePath);
	    	}
    	} else log.error("Must give a file to load.");
    }
}
