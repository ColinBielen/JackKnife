package com.ceg.online.jackknife.importer.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Pass One of a two pass copy.  This first pass only copies the basic structure
 * i.e., type, node location and name, mixins and the uuid if the node has one.
 * @author djohnson
 * @author jstanton
 */
public class CopyBasicNodeVisitor implements NodeVisitor {
    private static Log log = LogFactory.getLog(CopyBasicNodeVisitor.class);
    private Session toSession;
    private Workspace toWorkspace;
    private DocumentBuilder docBuilder;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private JackrabbitImporter importer;
    
    /** Creates a new instance of CopyBasicNodeVisitor */
    public CopyBasicNodeVisitor(Session to, String host) throws ParserConfigurationException, TransformerConfigurationException, IOException {
        toSession = to;
        toWorkspace = toSession.getWorkspace();
        importer = new JackrabbitImporter(to);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        docBuilder = dbf.newDocumentBuilder();
    }
    
    public void visitNode(Node node) {
        String uuid = null;
        try {
            uuid = node.getUUID();
        } catch(RepositoryException uroex) {
            uuid = null;
        }
        
        List<String> mixins = new ArrayList<String>();
        try {
            NodeType [] mixinArray = node.getMixinNodeTypes();
            
            for(int pos = 0; pos < mixinArray.length; pos++) {
                mixins.add(mixinArray[pos].getName());
            }
        } catch(RepositoryException rex) {}

        try {
            importer.addNode(node.getParent().getPath(), node.getName(), uuid,
                node.getPrimaryNodeType().getName(), (String [])mixins.toArray(new String[0]));
        } catch(RepositoryException rex) {
            rex.printStackTrace();
        }
    }
    
    public void finish() {
        importer.finishImport();
    }
    
    
    
    public void visitNodeOld(Node node) {
        try {
            Document document = docBuilder.newDocument();
            Element root = document.createElement("sv:node");
            root.setAttribute("xmlns:jcr","http://www.jcp.org/jcr/1.0");
            root.setAttribute("xmlns:nt","http://www.jcp.org/jcr/nt/1.0");
            root.setAttribute("xmlns:mix","http://www.jcp.org/jcr/mix/1.0");
            root.setAttribute("xmlns:sv","http://www.jcp.org/jcr/sv/1.0");
            root.setAttribute("xmlns:eol","http://jcr.eonline.com");
            root.setAttribute("sv:name", node.getName());
            document.appendChild(root);
            
            // primary type
            Element primaryType = document.createElement("sv:property");
            primaryType.setAttribute("sv:name", "jcr:primaryType");
            primaryType.setAttribute("sv:type", "Name");
            Element primaryTypeValue = document.createElement("sv:value");
            Text valueNode = document.createTextNode(node.getPrimaryNodeType().getName());
            primaryTypeValue.appendChild(valueNode);
            primaryType.appendChild(primaryTypeValue);
            root.appendChild(primaryType);
            
            // mixins
            NodeType [] mixinArray = node.getMixinNodeTypes();
            if(mixinArray.length > 0) {
                Element mixins = document.createElement("sv:property");
                mixins.setAttribute("sv:name", "jcr:mixinTypes");
                mixins.setAttribute("sv:type", "Name");
                
                for(int i = 0 ; i < mixinArray.length; i++) {
                    Element mixinsValue = document.createElement("sv:value");
                    Text mixinsValueNode = document.createTextNode(mixinArray[i].getName());
                    mixinsValue.appendChild(mixinsValueNode);
                    mixins.appendChild(mixinsValue);
                }
                
                root.appendChild(mixins);
            }
            
            // uuid - if it has one
            try {
                Element uuid = document.createElement("sv:property");
                uuid.setAttribute("sv:name", "jcr:uuid");
                uuid.setAttribute("sv:type", "Name");
                
                Element uuidValue = document.createElement("sv:value");
                Text uuidValueNode = document.createTextNode(node.getUUID());
                uuidValue.appendChild(uuidValueNode);
                uuid.appendChild(uuidValue);
                
                root.appendChild(uuid);
            } catch(UnsupportedRepositoryOperationException ignore) {}
            
            OutputFormat of = new OutputFormat(document);
            of.setIndenting(true);
            
            XMLSerializer serializer = new XMLSerializer(baos, of);
            serializer.serialize(document);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            toWorkspace.importXML(node.getParent().getPath(), bais, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            
            // clear out the byte array output stream for the next node
            baos.reset();
        } catch(RepositoryException rex) {
            log.error("RepositoryException", rex);
        } catch(IOException ioex) {
            log.error("IOException", ioex);
        }
    }
}
