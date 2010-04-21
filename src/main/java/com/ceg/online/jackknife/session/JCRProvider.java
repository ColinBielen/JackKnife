package com.ceg.online.jackknife.session;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Generic Interface for defining JCR Providers
 * @author colinb
 * @author jstanton
 */
public interface JCRProvider {
    public Session getSession(String host) throws RepositoryException;
}
