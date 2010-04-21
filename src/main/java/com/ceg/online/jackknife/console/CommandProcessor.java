package com.ceg.online.jackknife.console;

import java.util.Date;
import java.util.List;

import javax.jcr.NodeIterator;


/**
 * Interface for defining commands.
 * 
 * @author djohnson
 * @author jstanton
 */
public interface CommandProcessor {
    public String getCurrentPath() ;
    public void setCurrentPath(String path) ;
    
    public void setPathToNodeAtUUID(String uuid);

    public List<String> listPath(String path);
    
    public List<String> listCurrentPath();
    public void showCurrentPath();

    public NodeIterator processQuery(String query);
    public void processAndPrintQuery(String query);
    
    public void removeNode(String path);
    
    public void quit();
    
    public void moveNode(String sourcePath, String destPath);
    public void overwriteNode(String sourcePath, String destPath);
    
    public void makeNode(String path, String type);
    
    public void setProperty(String path, Long value);
    public void setProperty(String path, Date date);
    public void setProperty(String path, String string);
    public void setReferenceProperty(String path, String uuid);
    public void removeProperty(String path,String propName);

    public void showReferences(String uuid);
    
	public void lockPath(String path, boolean isDeep, boolean isSessionScoped);
    public void unlockPath(String path);
    public void statusOfPath(String path);
    
    public void addMixin(List<String> mixins, String path);

    public void find(String param);
    public NodeIterator findNodes(String param);
    public void rebuildNode(String path);
}
