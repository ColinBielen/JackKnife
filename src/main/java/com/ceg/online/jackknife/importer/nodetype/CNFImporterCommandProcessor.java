package com.ceg.online.jackknife.importer.nodetype;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.jcr.RepositoryException;

/**
 * @author jstanton
 */
public class CNFImporterCommandProcessor implements CommandProcessor {
	private PrintWriter writer;
	private JackrabbitCNFImporter importer;

    /**
	 * Creates a new instance of JCRCommandProcessor
	 * 
	 * @param host
	 * @param writer
     * @throws RepositoryException 
	 */
	public CNFImporterCommandProcessor(String host, PrintWriter writer) throws RepositoryException {
		this.writer = writer;
		importer = new JackrabbitCNFImporter(host);
	}

	public void quit() {
		System.exit(0);
	}
	
	public void importNodeTypes(String path) {
		importer.importCNF(getStream(path));
	}

	public void updateNodeTypes(String path) {
		importer.updateCNF(getStream(path));
	}

	private InputStream getStream(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			writer.println("Unable to find file at path: "+path);
		}
		return null;
	}
}
