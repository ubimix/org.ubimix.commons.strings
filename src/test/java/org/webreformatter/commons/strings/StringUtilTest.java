package org.webreformatter.commons.strings;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.webreformatter.commons.strings.StringUtil.IVariableProvider;

public class StringUtilTest extends TestCase {

    public StringUtilTest(String name) {
        super(name);
    }

    /**
     * 
     */
    public void testProperyResolving() {
        // Dictionary of properties
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("firstName", "John");
        properties.put("lastName", "Smith");
        properties.put("displayName", "${firstName} ${lastName}");
        properties.put("age", "37");
        // This property provider is used to resolve variables in the string
        IVariableProvider propertyProvider = new StringUtil.IVariableProvider() {
            public String getValue(String name) {
                return properties.get(name);
            }
        };

        String result = StringUtil.resolveProperty(
            "Hello ${firstName}! Your age is ${age}.",
            propertyProvider);
        assertEquals("Hello John! Your age is 37.", result);

        // Recursive property resolving: the property "displayName" references
        // the "fistName" and "lastName".
        result = StringUtil.resolveProperty(
            "Hello ${displayName}! Your age is ${age}.",
            propertyProvider);
        assertEquals("Hello John Smith! Your age is 37.", result);
    }

    /**
     * 
     */
    public void testProperyResolvingByKey() {
        // Dictionary of properties
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("root.dir", "./mydir");
        properties.put("log", "${root.dir}/log");
        properties.put("log.sys", "${log}/sys");
        properties.put("log.mymodule", "${log.sys}/mymodule");

        // This property provider is used to resolve variables in the string
        IVariableProvider propertyProvider = new StringUtil.IVariableProvider() {
            public String getValue(String name) {
                return properties.get(name);
            }
        };

        // The template itself is searched in the property map
        String result = StringUtil.resolvePropertyByKey(
            "log.sys",
            propertyProvider);
        assertEquals("./mydir/log/sys", result);
        result = StringUtil.resolvePropertyByKey(
            "log.mymodule",
            propertyProvider);
        assertEquals("./mydir/log/sys/mymodule", result);
    }

}
