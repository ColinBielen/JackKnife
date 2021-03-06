package com.ceg.online.jackknife.util;


import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

	@Test public void endsWithIndex() {
		Assert.assertTrue(StringUtils.endsWithArrayIndex("blah/blah[3]"));
		Assert.assertTrue(StringUtils.endsWithArrayIndex("/blah/blah[323424]"));
		Assert.assertTrue(StringUtils.endsWithArrayIndex("blah/blah[3]/blah[3]"));
		Assert.assertTrue(StringUtils.endsWithArrayIndex("/blah/blah[3]/blah[3]"));
		
		Assert.assertFalse(StringUtils.endsWithArrayIndex("/blah/blah[3]/blah"));
		Assert.assertFalse(StringUtils.endsWithArrayIndex("blah/blah[3]/blah"));
		Assert.assertFalse(StringUtils.endsWithArrayIndex("/blah/blah"));
		Assert.assertFalse(StringUtils.endsWithArrayIndex("blah/blah"));
		Assert.assertFalse(StringUtils.endsWithArrayIndex("blah/blah[3]/blah[3]/"));
	}
	
	@Test public void isChildOf() {
		Assert.assertTrue(StringUtils.isChildOf("EOL/stuff/things", 2, "EOL/stuff", 1));
//		Assert.assertFalse(StringUtils.isChildOf("EOL/stuff", 1, "EOL/stuff", 1));
		Assert.assertTrue(StringUtils.isChildOf("EOL/stuff/things", 2, "stuff", 1));
		Assert.assertFalse(StringUtils.isChildOf("EOL/things/stuff", 2, "EOL/stuff", 1));
		Assert.assertTrue(StringUtils.isChildOf("EOL/stuff/things/stuff", 12, "stuff/things", 2));
//		Assert.assertFalse(StringUtils.isChildOf("EOL/stuff/things/stuff", 2, "stuff/things", 12));
	}

}
