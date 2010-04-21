package com.ceg.online.jackknife.importer.node;

import org.xml.sax.ContentHandler;

import javax.jcr.*;
import java.io.IOException;

import com.ceg.online.jackknife.session.SessionFactory;

/**
 * Simple exporter using the built-in jcr import/export api....
 */
public class JCRExporter {

    public Session sourceSession;
    private int nodesExported;

    public JCRExporter(Session source) {
        sourceSession = source;
        nodesExported = 0;
    }


    static public void main(String[] argv) throws RepositoryException, IOException {
        boolean fromJCR = true;
        boolean toJCR = true;
        String toHost = "localhost";
        String fromHost = "localhost";
        for (int i = 0; i < argv.length; i++) {
            String param = argv[i];
            if (param.equals("-fromCRX")) fromJCR = false;
            else if (param.equals("-toCRX")) toJCR = false;
            else if (param.equals("-toHost")) {
                toHost = argv[i + 1];
                i++;
                continue;
            } else if (param.equals("-fromHost")) {
                fromHost = argv[i + 1];
                i++;
                continue;
            } else System.out.println("Unknown parameter: " + param);
        }
        Session fromSession = SessionFactory.getJCRSession(fromHost, !fromJCR);
        Session toSession = SessionFactory.getJCRSession(toHost, !toJCR);
        JCRExporter exporter = new JCRExporter(fromSession);
        exporter.export(toSession);
    }


    /**
     * Export everything in the source session to the target session.
     *
     * @param targetSession
     * @throws RepositoryException
     */
    public void export(Session targetSession) throws RepositoryException {
        //First the Node...
        nodesExported = 0;
        NodeIterator ni = sourceSession.getRootNode().getNodes();
        while (ni.hasNext()) {
            Node n = ni.nextNode();

            exportNode(n, targetSession);
            //revert the node removal...
            sourceSession.refresh(false);
            targetSession.save();
            NodeIterator childItr = n.getNodes();
            while (childItr.hasNext()) {
                exportNode(childItr.nextNode(), targetSession);
            }
        }
        //Then copy the references...

        ni = sourceSession.getRootNode().getNodes();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            copyReferences(n, targetSession);
        }
        targetSession.save();
    }

    /**
     * Exports a node (sans references)
     *
     * @param sourceNode
     * @param targetSession
     * @throws RepositoryException
     */
    public void exportNode(Node sourceNode, Session targetSession) throws RepositoryException {
        System.out.println("Exporting "+sourceNode.getPath());
        ContentHandler handler = targetSession.getImportContentHandler(sourceNode.getParent().getPath(), ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        try {
            correctMixins(sourceNode);
            stripReferences(sourceNode);
            sourceNode.getSession().exportSystemView(sourceNode.getPath(), handler, true, true);
            ++nodesExported;
            sourceNode.refresh(false);
            targetSession.save();
        } catch (Exception e) {
            System.out.println("Problem with XML " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set the Node in the target session with the references from the source session..
     *
     * @param n             - the source node
     * @param targetSession the session we're exporting to.
     * @throws RepositoryException
     */
    public void copyReferences(Node n, Session targetSession) throws RepositoryException {
        PropertyIterator pi = n.getProperties();
        Node targetNode = targetSession.getNodeByUUID(n.getUUID());
        while (pi.hasNext()) {
            Property p = pi.nextProperty();

            if ((p.getType() == PropertyType.REFERENCE)
                 && (!p.getDefinition().isProtected())) {
                if (p.getDefinition().isMultiple()) {
                    Value[] sourceValues = p.getValues();
                    targetNode.setProperty(p.getName(), sourceValues);
                } else {
                    Value sourceValue = p.getValue();
                    targetNode.setProperty(p.getName(), sourceValue);
                }


            }
        }
        targetNode.save();
    }

    public void stripReferences(Node n) throws RepositoryException {
        PropertyIterator pi = n.getProperties();
        while (pi.hasNext()) {
            Property p = pi.nextProperty();
            if ((p.getType() == PropertyType.REFERENCE)
                && (!p.getDefinition().isProtected())) {
                p.remove();
            }
        }       
    }

    /**
     * We don't want to export versionable nodes: just the refernceable bit..
     * @param n
     * @throws RepositoryException
     */
    public void correctMixins(Node n) throws RepositoryException {
            n.removeMixin("mix:versionable");
            n.addMixin("mix:referenceable");
    }
}
