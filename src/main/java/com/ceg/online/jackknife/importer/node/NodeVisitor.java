package com.ceg.online.jackknife.importer.node;

import javax.jcr.Node;

/**
 * @author djohnson
 */
public interface NodeVisitor {
    public void visitNode(Node node);
}
