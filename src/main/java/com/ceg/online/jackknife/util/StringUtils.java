package com.ceg.online.jackknife.util;

import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Utility class for dealing with JCR path fun.
 * Stripping and adding leading slashes,etc.
 * 
 * @author jstanton
 */
public class StringUtils {
	
	/**
	 * Does what the name implies.  makes /some/thing/ be /some/thing
	 * @param s
	 * @return
	 */
	public static String stripLastSlash(String s) {
		if(s.endsWith("/")) return s.substring(0, s.length() - 1);
		return s;
	}
	
	/**
	 * Does what the name implies.  makes /some/thing/ be /some/thing
	 * @param s
	 * @return
	 */
	public static String stripLeadingSlash(String s) {
		if(s.startsWith("/")) return s.substring(1);
		return s;
	}
	
	/**
	 * Does what the name implies.  makes /some/thing/ be /some/thing
	 * @param s
	 * @return
	 */
	public static String stripLastPathChunk(String s) {
		s = stripLastSlash(s);
		if(s.contains("/")) return s.substring(0, s.lastIndexOf("/"));
		return s;
	}
	
	/**
	 * If given /some/thing/ it will give back "thing".
	 */
	public static String getLastPathChunk(String s) {
		s = stripLastSlash(s);
		if(s.contains("/")) return s.substring(s.lastIndexOf("/"));
		return s;
	}

	public static boolean endsWithArrayIndex(String s) {
		if(Pattern.matches(".*\\[\\d+\\]", s)) return true;
		return false;
	}

	public static boolean isChildOf(Node fromNode, Node toNode) throws RepositoryException {
		return isChildOf(fromNode.getPath(), fromNode.getDepth(), toNode.getPath(), toNode.getDepth());
	}

	public static boolean isChildOf(String fromPath, int fromDepth, String toPath, int toDepth) {
		return /*fromDepth > toDepth &&*/ fromPath.indexOf(toPath) >= 0;
	}

	public static int getFinalIndexFromPath(String path) {
		path = stripLastSlash(path);
		if(endsWithArrayIndex(path)) {
			int lastOpenBracket = path.lastIndexOf('[');
			return Integer.parseInt(path.substring(lastOpenBracket+1, path.length()-1)) - 1;
		}
		return 0;
	}

	public static String stripArrayIndex(String path) {
		path = stripLastSlash(path);
		if(endsWithArrayIndex(path)) {
			int lastOpenBracket = path.lastIndexOf('[');
			return path.substring(0, lastOpenBracket);
		}
		return path;
	}

	public static String stripLeadingWhiteSpace(String param) {
		return param.startsWith(" ")?param.substring(1):param;
	}
	
	public static boolean isNullOrEmpty(String s) {
		return (s != null && !s.equals(""));
	}
}
