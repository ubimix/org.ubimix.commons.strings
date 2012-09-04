/* ************************************************************************** *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ************************************************************************** */
package org.ubimix.commons.messages;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

/**
 * This tool transforms java interfaces into a source of internationalized
 * messages. It create instances of a specified interface and each method call
 * returns an internationalized message with the key equals to the name of the
 * called method. The resulting values are loaded from standard "*.properties"
 * files. The full name of such files are created using the full name of the
 * initial interface. For example for the interface "foo.bar.MyMessages" the
 * property files like "/foo/bar/MyMessages.properties",
 * "/foo/bar/MyMessages_fr.properties", "/foo/bar/MyMessages_en.properties", ...
 * are used. These property files should be available using the classpath
 * specified while the instantiation.
 * <p>
 * This class automatically instantiate interfaces using the standard java proxy
 * mechanism (see {@link java.lang.reflect.Proxy}). All method calls for created
 * proxy objects are interpreted as requests for a message with the key equals
 * to the name of the method. If a method has parameters then the returned
 * string is interpreted as a message template and all {x} placeholders are
 * replaced by the parameters using the {@link java.text.MessageFormat} tool.
 * </p>
 * <p>
 * To manage the currently used locale the method {@link #setLocale(Locale)} can
 * be used. This method sets the specified locale in a thread local variables.
 * So all calls from the same thread of proxies created by this class will
 * return values in this locale.
 * </p>
 * Example:
 * 
 * <pre>
 * Interface: 
 * 
 * ----------------------
 * package foo.bar;
 * interface MyMessages {
 *    String sayHello();
 *    String error(Sting errorMessage);
 * }
 * ----------------------
 * 
 * Properties file /foo/bar/MyMessages.properties (available  in the classpath):
 * sayHello=Hello, world!
 * error=Error! Message: ''{0}'' Please try again!
 * ----------------------
 * 
 * Usage: 
 * MyMessages m = MessageUtil.getMessages(MyMessages.class);
 * assertEquals("Hello, world!",  m.sayHello());
 * assertEquals("Error! Message: 'Test' Please try again!", m.error("Test"));
 * 
 * </pre>
 * 
 * @author kotelnikov
 */
public class MessageUtil {

    /**
     * Counter of instances.
     */
    private static int fCounter;

    /**
     * A thread-local variable used to manage currently used locales. Each
     * thread can defined its own locale.
     */
    private final static ThreadLocal<Locale> fLocale = new ThreadLocal<Locale>();

    /**
     * Cache of message objects corresponding to each type. This is a weak map
     * and this cache don't prevent classes from the garbage collection. So if a
     * class is not used anymore the corresponding message proxy can be garbage
     * collected as well.
     */
    private static Map<Class<?>, Object> fMessages = new WeakHashMap<Class<?>, Object>();

    /**
     * This method returns a proxy implementing the specified interface. All
     * method calls for the proxy are interpreted as requests for
     * internationalized messages with keys equal to the names of called
     * methods. If there is already an object implementing the specified
     * interface then it will be re-used.
     * 
     * @param <T> the type of the object to implement
     * @param cls the class defining the interface
     * @return a proxy implementing the specified message interface
     */
    @SuppressWarnings("unchecked")
    public synchronized static <T> T getMessages(final Class<T> cls) {
        T messages = (T) fMessages.get(cls);
        if (messages == null) {
            messages = newMessages(cls);
            fMessages.put(cls, messages);
        }
        return messages;
    }

    /**
     * Creates and returns a new invocation handler for the specified class.
     * 
     * @param <T> the type of the message handled by the returned object
     * @param cls the interface to implement; the returned handler will handle
     *        calls for a proxy implementing this interface
     * @return a new proxy invocation handler
     */
    private static <T> InvocationHandler newHandler(final Class<T> cls) {
        return new InvocationHandler() {

            /**
             * The identifier of this instance
             */
            int fId = fCounter++;

            /**
             * Map of resource bundles for each locale
             */
            Map<Locale, ResourceBundle> fMap = new HashMap<Locale, ResourceBundle>();

            /**
             * @return a resource bundle used for the current locale; the
             *         current locale is defined by the
             *         {@link MessageUtil#fLocale} field.
             */
            private ResourceBundle getResourceBundle() {
                Locale locale = fLocale.get();
                if (locale == null) {
                    locale = Locale.getDefault();
                }
                ResourceBundle bundle = fMap.get(locale);
                if (bundle == null) {
                    ClassLoader loader = cls.getClassLoader();
                    bundle = ResourceBundle.getBundle(
                        cls.getName(),
                        locale,
                        loader);

                }
                return bundle;
            }

            /**
             * Handles all method calls and returns the corresponding string
             * value
             * 
             * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
             *      java.lang.reflect.Method, java.lang.Object[])
             */
            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
                ResourceBundle bundle = getResourceBundle();
                String key = method.getName();
                String value = null;
                if (bundle != null) {
                    value = bundle.getString(key);
                    if (value != null && args != null && args.length > 0) {
                        value = MessageFormat.format(value, args);
                    }
                }
                if (value == null) {
                    value = key;
                }
                return value;
            }

            @Override
            public String toString() {
                return "Proxy[" + cls.getName() + ":" + fId + "]";
            }
        };
    }

    /**
     * This method creates and returns a new proxy implementing the specified
     * interface. All method calls for the proxy are interpreted as requests for
     * internationalized messages with keys equal to the names of called
     * methods.
     * 
     * @param <T> the type of the object to implement
     * @param cls the class defining the interface
     * @return a proxy implementing the specified message interface
     */
    @SuppressWarnings("unchecked")
    private static <T> T newMessages(final Class<T> cls) {
        InvocationHandler handler = newHandler(cls);
        T impl = (T) Proxy.newProxyInstance(
            cls.getClassLoader(),
            new Class<?>[] { cls },
            handler);
        return impl;
    }

    /**
     * Sets a thread-specific locale. This locale will be used in the current
     * thread by all proxies created by this class.
     * 
     * @param locale a new locale to set
     */
    public static void setLocale(Locale locale) {
        fLocale.set(locale);
    }
}
