package com.ceg.online.jackknife.importer.nodetype;




/**
 * Interface for defining commands.
 * 
 * @author djohnson
 */
public interface CommandProcessor {
    public void quit();
    public void importNodeTypes(String path);
    public void updateNodeTypes(String path);
}
