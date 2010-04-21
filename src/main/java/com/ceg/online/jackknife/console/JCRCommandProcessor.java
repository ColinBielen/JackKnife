package com.ceg.online.jackknife.console;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import com.ceg.online.jackknife.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author djohnson
 * @author jstanton
 */
public class JCRCommandProcessor implements CommandProcessor {
	private Session session;
	private Node currentNode;
	private PrintWriter writer;
	private final String FIND_QUERY_PREFIX = "select * from nt:base where contains(*, '";
	private final String FIND_QUERY_SUFFIX = "')";
	private static Log log = LogFactory.getLog(JCRCommandProcessor.class);

    /**
	 * Creates a new instance of JCRCommandProcessor
	 * 
	 * @param session
	 * @param writer
	 */
	public JCRCommandProcessor(Session session, PrintWriter writer) {
		try {
			this.session = session;
			currentNode = session.getRootNode();
			this.writer = writer;
		} catch (RepositoryException ex) {
			log.error(ex);
		}
	}

	public String getCurrentPath() {
		try {
			return currentNode.getPath();
		} catch (RepositoryException ex) {
			log.error(ex);
		}
		return null;
	}

	public void setCurrentPath(String path) {
		currentNode = getNodeFromPath(path);
	}

	public static String valueToString(Value v) {
		try {
			switch (v.getType()) {
			case PropertyType.DATE:
				return v.getDate().toString();
			case PropertyType.LONG:
				return Long.toString(v.getLong());
			case PropertyType.BOOLEAN:
				return Boolean.toString(v.getBoolean());
			case PropertyType.DOUBLE:
				return Double.toString(v.getDouble());
			default:
				return v.getString();
			}
		} catch (RepositoryException rex) {
			return "EXCEPTION during valueToString";
		}
	}


    private Node getNodeFromPath(String path) {
		int index = StringUtils.getFinalIndexFromPath(path);
		try {
			if (isRootPath(path)) {
				return session.getRootNode();
			}
			while (path.endsWith("/")) path = StringUtils.stripLastSlash(path); 
			if (!isAbsolutePath(path)) return currentNode.getNode(path);
			return getNodeFromAbsPath(index, path);
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
		return currentNode;
	}
	
	public Node getNodeFromAbsPath(int index, String path) {
		try {
			Item i = session.getItem(path);
			if(i.isNode()) {
				return (Node) i;
			} else {
				setPathToNodeAtUUID(((Property)i).getValues()[index].getString());
				return currentNode;
			}
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
		return currentNode;
	}

	private List<String> listNode(Node node) {
		List<String> ret = new ArrayList<String>();
		try {
			PropertyIterator pi = node.getProperties();
			while (pi.hasNext()) {
				Property prop = pi.nextProperty();
				if (prop.getDefinition().isMultiple()) {
					Value[] values = prop.getValues();

					for (int i = 0; i < values.length; i++) {
						ret.add(prop.getName() + "[" + (i+1) + "]" + "\t"
								+ valueToString(values[i]));
					}
				} else {
					ret.add(prop.getName() + "\t"
							+ valueToString(prop.getValue()));
				}
			}

			NodeIterator ni = node.getNodes();
			while (ni.hasNext()) {
				Node n = ni.nextNode();
				ret.add(n.getPrimaryNodeType().getName() + "\t" + n.getName()
						+ "/");
			}
		} catch (RepositoryException rex) {
			log.error(rex);
		}

		return ret;
	}

	public List<String> listPath(String path) {
		Node node = getNodeFromPath(path);
		List<String> ret = listNode(node);
		for (String str : ret) {
			writer.println(str);
		}
		return ret;
	}

	public List<String> listCurrentPath() {
		List<String> ret = listNode(currentNode);
		for (String str : ret) {
			writer.println(str);
		}
		return ret;
	}

	public QueryResult getQueryResult(String queryString) throws RepositoryException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		Query query = qm.createQuery(queryString, Query.SQL);
		return query.execute();
	}
	
	public NodeIterator processQuery(String queryString) {
		try {
			QueryResult qr = getQueryResult(queryString);
			return qr.getNodes();
		} catch(RepositoryException rex) {
			log.error(rex);
		}
		return null;
	}
	
	public void quit() {
		System.exit(0);
	}
	
	public void processAndPrintQuery(String queryString) {
		try {
			QueryResult qr = getQueryResult(queryString);
			RowIterator ri = qr.getRows();
			String[] columnNames = qr.getColumnNames();
			for (String name : columnNames) {
				writer.print(name);
				writer.print("\t");
			}
			writer.println();
			while (ri.hasNext()) {
				Row row = ri.nextRow();
				Value[] values = row.getValues();
				for (Value value : values) {
					if (value != null) {
						switch (value.getType()) {
						case PropertyType.STRING:
							writer.print(value.getString());
							break;
						case PropertyType.DATE:
							writer.print(value.getDate());
							break;
						case PropertyType.LONG:
							writer.print(value.getLong());
							break;
						case PropertyType.PATH:
							writer.print(value.getString());
							break;
						case PropertyType.REFERENCE:
							writer.print(value.getString());
							break;
						default:
							writer.print(PropertyType.nameFromValue(value
									.getType())
									+ "::" + value.toString());
							break;
						}
					} else {
						writer.print("null");
					}
					writer.print("\t");
				}
				writer.println();
			}
			writer.println(ri.getSize()+" results");
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
	}

	public void removeNode(String path) {
		Node node = null;
		try {
			if (isAbsolutePath(path)) {
				node = session.getRootNode().getNode(StringUtils.stripLeadingSlash(path));
			} else {
				node = currentNode.getNode(path);
			}
			node.remove();
			session.save();
		} catch (PathNotFoundException ex) {
			writer.println("Unable to remove node at non-existant path: "+path);
		} catch (RepositoryException ex) {
			log.error(ex);
		}
	}

    /**
     * Removes a property from a given node. Useful for when
     * we have UUIDs referenceing content that doesn't exist anymore.
     * @param path
     * @param propertyName
     */
    public void removeProperty(String path,String propertyName) {
        Node node = null;
        try {
			if (isAbsolutePath(path)) {
				node = session.getRootNode().getNode(StringUtils.stripLeadingSlash(path));
			} else {
				node = currentNode.getNode(path);
			}
		    Property p = node.getProperty(propertyName);
            log.info("Removing '"+propertyName+"' from node at path '"+path+"'");
            p.remove();
            session.save();
		} catch (PathNotFoundException ex) {
			writer.println("Unable to remove property at non-existant path: "+path);
		} catch (RepositoryException ex) {
			log.error(ex);
		}
    }

    public void moveNode(String sourcePath, String destPath) {
		handleDestinationWithIndex(destPath);
		boolean rename = !destPath.endsWith("/");
		destPath = StringUtils.stripLastSlash(destPath);
		sourcePath = StringUtils.stripLastSlash(sourcePath);
		Node fromNode = null;
		Node newParentNode = null;
		try {
			if (isAbsolutePath(sourcePath)) {
					fromNode = session.getRootNode().getNode(
							StringUtils.stripLeadingSlash(sourcePath));
			} else {
				fromNode = currentNode.getNode(sourcePath);
			}
			if (isAbsolutePath(destPath)) {
				String basePath = StringUtils.stripLeadingSlash(destPath);
				basePath = StringUtils.stripLastPathChunk(basePath);
				newParentNode = session.getRootNode().getNode(basePath);
			} else {
				//Need to deal with mv blah ../../blah
				if(!destPath.contains("/")) newParentNode = currentNode;
				else if(rename) newParentNode = currentNode.getNode(StringUtils.stripLastPathChunk(destPath));
				else newParentNode = currentNode.getNode(destPath);
			}
			String newPath = newParentNode.getPath();
			if(!destPath.contains("/")) newPath += "/" + destPath;
			if(!rename) newPath += StringUtils.getLastPathChunk(fromNode.getPath());
			else newPath += StringUtils.getLastPathChunk(destPath);
			writer.println("New path for move: "+newPath);
			session.move(fromNode.getPath(), newPath);
			session.save();
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException e) {
			writer.println("RepositoryException: "+e.getMessage());
		}
	}

	public void overwriteNode(String sourcePath, String destPath) {
		handleDestinationWithIndex(destPath);
		destPath = StringUtils.stripLastSlash(destPath);
		sourcePath = StringUtils.stripLastSlash(sourcePath);
		Node fromNode = null;
		Node toNode = null;
		try {
			if (isAbsolutePath(sourcePath)) {
				fromNode = session.getRootNode().getNode(
						StringUtils.stripLeadingSlash(sourcePath));
			} else {
				fromNode = currentNode.getNode(sourcePath);
			}
			if (isAbsolutePath(destPath)) {
				toNode = session.getRootNode().getNode(
						StringUtils.stripLeadingSlash(destPath));
			} else {
				toNode = currentNode.getNode(destPath);
			}
			String fromPath = fromNode.getPath();
			String toPath = toNode.getPath();
			if (fromPath.equals(toPath)) {
				writer.println("Cannot overwrite a node with itself (no-op).");
			} else if (StringUtils.isChildOf(fromPath, fromNode.getDepth(), toPath, toNode.getDepth())) {
				writer.println("Cannot overwrite a parent node with a child node.");
			} else {
				// Get rid of the old one...
				toNode.remove();
				// Move the new one into the old one's place...
				session.move(fromPath, toPath);
				session.save();
			}
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException e) {
			writer.println("RepositoryException: "+e.getMessage());
		}
	}

	public void makeNode(String path, String nodetype) {
		String parent = null;
		String name = null;

		if (path.contains("/")) {
			parent = path.substring(0, path.lastIndexOf('/'));
			name = path.substring(path.lastIndexOf('/') + 1);
		} else {
			name = path;
		}

		try {
			if (isAbsolutePath(path)) {
				if (parent.equals("")) {
					session.getRootNode().addNode(name, nodetype);
				} else {
					session.getRootNode().getNode(parent.substring(1)).addNode(
							name, nodetype);
				}
			} else {
				if (parent != null)
					currentNode.getNode(parent).addNode(name, nodetype);
				else
					currentNode.addNode(name, nodetype);
			}
			session.save();
		} catch (RepositoryException rex) {
			log.error(rex);
		}
	}

	private boolean isAbsolutePath(String path) {
		return path != null && path.startsWith("/");
	}

	private boolean isRootPath(String path) {
		return path.equals("/");
	}

	public void setProperty(String path, Long value) {
		try {
			if (isAbsolutePath(path)) {
				Property property = session.getRootNode().getProperty(
						path.substring(1));
				property.setValue(value.longValue());
			} else {
				Property property = currentNode.getProperty(path);
				property.setValue(value.longValue());
			}
			session.save();
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
	}

	public void setProperty(String path, Date date) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			if (isAbsolutePath(path)) {
				Property property = session.getRootNode().getProperty(path.substring(1));
				property.setValue(cal);
			} else {
				Property property = currentNode.getProperty(path);
				property.setValue(cal);
			}
			session.save();
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
	}

	public void setProperty(String path, String string) {
		try {
			if (isAbsolutePath(path)) {
				Property property = session.getRootNode().getProperty(
						path.substring(1));
				property.setValue(string);
			} else {
				Property property = currentNode.getProperty(path);
				property.setValue(string);
			}
			session.save();
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
	}

	public void setReferenceProperty(String path, String uuid) {
		setProperty(path, uuid);
	}

	public void showReferences(String uuid) {
		try {
			Node node = session.getNodeByUUID(uuid);
			PropertyIterator pi = node.getReferences();
			while (pi.hasNext()) {
				Property p = pi.nextProperty();

				Value[] values = p.getValues();
				for (Value value : values) {
					writer.println(value.getString());
				}
			}
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
	}

	public void setPathToNodeAtUUID(String uuid) {
		try {
			currentNode = session.getNodeByUUID(uuid);
		} catch (RepositoryException rex) {
			writer.println("RepositoryException: "+rex.getMessage());
		}
	}

	private void handleDestinationWithIndex(String dest) {
		if (StringUtils.endsWithArrayIndex(dest)) {
			throw new IllegalArgumentException(
					"Illegal Destination Format: Cannot give a destination an index: "
							+ dest);
		}
	}

	public void lockPath(String path, boolean isDeep, boolean isSessionScoped) {
		try {
			Node node;
			if (isAbsolutePath(path)) {
				node = session.getRootNode().getNode(
						StringUtils.stripLeadingSlash(path));
			} else {
				node = currentNode.getNode(path);
			}
			node.lock(isDeep, isSessionScoped);
			session.save();
		} catch (PathNotFoundException e) {
			writer.println("Unable to find path: "+e.getMessage());
		} catch (RepositoryException e) {
			writer.println("Problem locking node with path " + path
					+ "\nReason: " + e.getMessage());
		}
	}

	public void unlockPath(String path) {
		try {
			Node node;
			if (isAbsolutePath(path)) {
				node = session.getRootNode().getNode(
						StringUtils.stripLeadingSlash(path));
			} else {
				node = currentNode.getNode(path);
			}
            
            node.unlock();
			session.save();
		} catch (RepositoryException e) {
			writer.println("Problem unlocking node with path " + path
					+ "\nReason: " + e.getMessage());
		}
	}

	public void statusOfPath(String path) {
		try {
			Node node;
			if (isAbsolutePath(path)) {
				node = session.getRootNode().getNode(StringUtils.stripLeadingSlash(path));
			} else {
				node = currentNode.getNode(path);
			}
			writer.println("STATUS: " + (node.isLocked() ? "locked" : "not locked"));
		} catch (RepositoryException e) {
			writer.println("Problem obtaining the status of " + path + ": "
					+ e.getMessage());
		}
	}

	public void addMixin(List<String> mixins, String path) {
		try {
			Node node;
			if (isAbsolutePath(path)) {
				node = session.getRootNode().getNode(StringUtils.stripLeadingSlash(path));
			} else {
				node = currentNode.getNode(path);
			}
			writer.println("There is/are "+mixins.size()+" mixin(s) to add.");
			for(String mixin:mixins) {
				writer.println("Adding "+mixin+" to "+path);
				node.addMixin(mixin);
			}
			session.save();
		} catch (RepositoryException e) {
			writer.println("Problem adding mixin to " + path + ": "
					+ e.getMessage());
		}
	}

	public void showCurrentPath() {
		try {
			writer.println(currentNode.getPath());
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void find(String param) {
		String searchQuery = FIND_QUERY_PREFIX+StringUtils.stripLeadingWhiteSpace(param)+FIND_QUERY_SUFFIX;
		processAndPrintQuery(searchQuery);
	}

	public NodeIterator findNodes(String param) {
		String searchQuery = FIND_QUERY_PREFIX+StringUtils.stripLeadingWhiteSpace(param)+FIND_QUERY_SUFFIX;
		writer.println("Query: "+searchQuery);
		return processQuery(searchQuery);
	}

    /**
     * Recreates the node at the specified path.
     * Does NOT re-create subnodes or UUIDs. Its main use is for when certain nodes end
     * up in a "locked" state but won't "unlock". 
     * @param path
     */
    public void rebuildNode(String path) {
            writer.println("Stub Code: Rebuilding Node "+path+"...");


    }

}
