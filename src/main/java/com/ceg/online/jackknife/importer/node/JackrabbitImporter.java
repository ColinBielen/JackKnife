package com.ceg.online.jackknife.importer.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author jstanton
 */
public class JackrabbitImporter {
	private static Log log = LogFactory.getLog(JackrabbitImporter.class);

	private int unsavedNodeCount = 0;
	private int nodeCount = 0;
	private Session session;
	private DocumentBuilder docBuilder;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public JackrabbitImporter(Session session) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			docBuilder = dbf.newDocumentBuilder();
			this.session = session;
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	public void addNode(String path, String name, String uuid, String type,
			String[] mixinList) {
		try {
			createNode(path, name, uuid, type, mixinList);
			nodeCount++;
			if (unsavedNodeCount++ > 1000) {
				session.save();
				unsavedNodeCount = 0;
				System.out.println("--- node count is " + nodeCount);
			}
		} catch (RepositoryException rex) {
			log.error(rex);
		}
	}

	public void checkpointCommitImport() {
		try {
			session.save();
		} catch (RepositoryException ex) {
			log.error(ex);
		}
	}

	public void finishImport() {
		try {
			session.save();
		} catch (RepositoryException ex) {
			log.error(ex);
	    } finally {
			session.logout();
		}
	}

	private void createNode(String path, String name, String uuid, String type,
			String[] mixinList) throws RepositoryException {
		try {
			Document document = docBuilder.newDocument();
			Element root = document.createElement("sv:node");
			root.setAttribute("xmlns:jcr", "http://www.jcp.org/jcr/1.0");
			root.setAttribute("xmlns:nt", "http://www.jcp.org/jcr/nt/1.0");
			root.setAttribute("xmlns:mix", "http://www.jcp.org/jcr/mix/1.0");
			root.setAttribute("xmlns:sv", "http://www.jcp.org/jcr/sv/1.0");
			root.setAttribute("xmlns:eol", "http://jcr.eonline.com");
			root.setAttribute("sv:name", name);
			document.appendChild(root);

			// primary type
			Element primaryType = document.createElement("sv:property");
			primaryType.setAttribute("sv:name", "jcr:primaryType");
			primaryType.setAttribute("sv:type", "Name");
			Element primaryTypeValue = document.createElement("sv:value");
			Text valueNode = document.createTextNode(type);
			primaryTypeValue.appendChild(valueNode);
			primaryType.appendChild(primaryTypeValue);
			root.appendChild(primaryType);

			// mixins
			// NodeType [] mixinArray = node.getMixinNodeTypes();
			if (mixinList.length > 0) {
				Element mixins = document.createElement("sv:property");
				mixins.setAttribute("sv:name", "jcr:mixinTypes");
				mixins.setAttribute("sv:type", "Name");

				for (int i = 0; i < mixinList.length; i++) {
					Element mixinsValue = document.createElement("sv:value");
					Text mixinsValueNode = document
							.createTextNode(mixinList[i]);
					mixinsValue.appendChild(mixinsValueNode);
					mixins.appendChild(mixinsValue);
				}

				root.appendChild(mixins);
			}

			if (uuid != null) {
				// uuid - if it has one
				Element uuidElement = document.createElement("sv:property");
				uuidElement.setAttribute("sv:name", "jcr:uuid");
				uuidElement.setAttribute("sv:type", "Name");

				Element uuidValue = document.createElement("sv:value");
				Text uuidValueNode = document.createTextNode(uuid);
				uuidValue.appendChild(uuidValueNode);
				uuidElement.appendChild(uuidValue);

				root.appendChild(uuidElement);
			}

			OutputFormat of = new OutputFormat(document);
			of.setIndenting(true);

			XMLSerializer serializer = new XMLSerializer(baos, of);
			serializer.serialize(document);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());

			session.importXML(path, bais, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
			// clear out the byte array output stream for the next node
			baos.reset();
		} catch (IOException ioex) {
			log.error(ioex);
		}
	}

	private void saveNode(Node node, DynaBean db) throws RepositoryException {
		DynaProperty[] adp = db.getDynaClass().getDynaProperties();
		List<DynaProperty> ldp = Arrays.asList(adp);
		Iterator<DynaProperty> iter = ldp.iterator();

		while (iter.hasNext()) {
			DynaProperty dp = iter.next();
			String name = dp.getName();

			if (db.get(name) instanceof Value) {
				node.setProperty(name, (Value) db.get(name));
			} else {
				node.setProperty(name, (Value[]) db.get(name));
			}
		}

		nodeCount++;
		if (unsavedNodeCount++ > 1000) {
			session.save();
			unsavedNodeCount = 0;
			System.out.println("--- node count is " + nodeCount);
		}
	}

	public void saveNodeAtUUID(String uuid, DynaBean db) {
		try {
			saveNode(session.getNodeByUUID(uuid), db);
		} catch (ItemNotFoundException ex) {
			log.error(ex);
		} catch (RepositoryException ex) {
			log.error(ex);
		}
	}    
    
    public void saveNodeAtPath(String path, DynaBean db) {
        try {
            saveNode((Node)session.getItem(path), db);
        } catch (RepositoryException ex) {
			log.error(ex);
        }
    }
}
