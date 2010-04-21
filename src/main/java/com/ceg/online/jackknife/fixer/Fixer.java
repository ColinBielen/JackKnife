package com.ceg.online.jackknife.fixer;

import javax.jcr.Session;
/**
 * @author cbielen
 * @author jstanton
 */
public interface Fixer {
    public void fix(Session s);
}
