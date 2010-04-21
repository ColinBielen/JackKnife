package com.ceg.online.jackknife.fixer;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ceg.online.jackknife.session.SessionFactory;

/**
 * Walks a repository testing for and removing missing or incomplete references.
 * It has to walk the tree because normally these kinds of problems crash the
 * content indexer, so doing things like "getReferences" won't work.
 * 
 * @author djohnson
 * @author jstanton
 */
public class BadRefFixer implements Fixer {

	private static Log log = LogFactory.getLog(BadRefFixer.class);
	private static long nodesChecked = 0l;
	private static Session session;

	public static void main(String args[]) {
		BadRefFixer bf = new BadRefFixer();
		boolean crx = false;
		String host = "localhost";

		for (int i = 0; i < args.length; i++) {
			String param = args[i];
			if (param.equals("-host")) {
				host = args[++i];
				continue;
			}
		}

		try {
			System.out.println("Connecting to " + host + "...");
			session = SessionFactory.getJCRSession(host, crx);
			bf.fix(session);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void fix(Session session) {
		try {
			NodeIterator ni = session.getRootNode().getNodes();
			while (ni.hasNext()) {
				Node n = ni.nextNode();
				if (n.getPath().startsWith("/jcr") || n.getPath().startsWith("/rep")) {
					System.out.println("Skipping '" + n.getPath() + "'");
				} else {
					checkNode(n);
				}
			}

		} catch (RepositoryException e) {
			log.error(e);
		}
	}

	public void checkNode(Node n) throws RepositoryException {
		try {
			System.out.print("Checking " + n.getPath() + "....");
			checkProperties(n);
			n.save();
			System.out.print("Done.\n");
		} catch (RepositoryException e) {
			log.error(e);
		}
		// Now check children:
		NodeIterator ni = n.getNodes();
		while (ni.hasNext()) {
			checkNode(ni.nextNode());
			++nodesChecked;
			if ((nodesChecked % 1000) == 0) {
				System.out.println("\n\n" + nodesChecked + " Nodes Checked..\n\n");
			}
		}
		// more...
	}

	public void checkProperties(Node n) throws RepositoryException {

		PropertyIterator pi = n.getProperties();
		while (pi.hasNext()) {
			Property p = pi.nextProperty();
			List<Value> goodRefs = new ArrayList<Value>();
			int numOfValues = 0;
			if (p.getType() == PropertyType.REFERENCE) {
				if (p.getDefinition().isMultiple()) {
					numOfValues = p.getValues().length;
					for (Value value : p.getValues()) {
						if (checkReference(n, p, value)) goodRefs.add(value);
					}
				} else {
					numOfValues = 1;
					Value value = p.getValue();
					if (checkReference(n, p, value))
						goodRefs.add(value);
				}
			}// end type
			if (goodRefs.size() < numOfValues)
				n.setProperty(p.getName(), goodRefs.toArray(new Value[0]));
		} // end while
	}

	private boolean checkReference(Node node, Property prop, Value value) {
		try {
			session.getNodeByUUID(value.getString());
		} catch (RepositoryException e) {
			log.error("There was an error while getting reference: " + value
					+ " for node " + node + " " + e.getMessage());
			return false;
		}
		return true;
	}

}
