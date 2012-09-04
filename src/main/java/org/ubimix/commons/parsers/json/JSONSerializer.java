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
package org.ubimix.commons.parsers.json;

import java.util.Stack;

import org.ubimix.commons.parsers.json.IJSONListener;

/**
 * @author kotelnikov
 */
public abstract class JSONSerializer implements IJSONListener {

    private StringBuffer fBuf = new StringBuffer();

    private Stack<Integer> fStack = new Stack<Integer>();

    /**
     * 
     */
    public JSONSerializer() {
    }

    public void beginArray() {
        print("[");
        fStack.push(0);
    }

    public void beginArrayElement() {
        if (inc() > 0)
            print(",");
    }

    public void beginObject() {
        print("{");
        fStack.push(0);
    }

    public void beginObjectProperty(String property) {
        if (inc() > 0)
            print(",");
        fBuf.delete(0, fBuf.length());
        if (escape(property, true, fBuf)) {
            print("'");
            print(fBuf.toString());
            print("'");
        } else {
            print(fBuf.toString());
        }
        print(":");
    }

    public void endArray() {
        print("]");
        fStack.pop();
    }

    public void endArrayElement() {
    }

    public void endObject() {
        print("}");
        fStack.pop();
    }

    public void endObjectProperty(String property) {
    }

    protected boolean escape(String str, boolean escapeSpace, StringBuffer buf) {
        boolean result = false;
        if (str == null)
            return result;
        int len = str.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        for (int x = 0; x < len; x++) {
            char aChar = str.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        buf.append("\\");
                    buf.append(" ");
                    result = true;
                    break;
                case '\'':
                    buf.append("\\'");
                    result = true;
                    break;
                case '\"':
                    buf.append("\\\"");
                    result = true;
                    break;
                case '\t':
                    buf.append("\\t");
                    result = true;
                    break;
                case '\n':
                    buf.append("\\n");
                    result = true;
                    break;
                case '\r':
                    buf.append("\\r");
                    result = true;
                    break;
                case '\f':
                    buf.append("\\f");
                    result = true;
                    break;
                case ':': // Fall through
                case ';': // Fall through
                case '+': // Fall through
                case '-': // Fall through
                case '/': // Fall through
                case '\\': // Fall through
                case '=': // Fall through
                case '!':
                    buf.append(aChar);
                    result = true;
                    break;
                default:
                    if ((aChar > 61) && (aChar < 127)) {
                        if (aChar == '\\') {
                            buf.append("\\\\");
                            result = true;
                            continue;
                        }
                        buf.append(aChar);
                        continue;
                    }
                    if (aChar < 0x0020) {
                        buf.append("\\u");
                        buf.append(Integer.toHexString((aChar >> 12) & 0xF));
                        buf.append(Integer.toHexString((aChar >> 8) & 0xF));
                        buf.append(Integer.toHexString((aChar >> 4) & 0xF));
                        buf.append(Integer.toHexString(aChar & 0xF));
                        result = true;
                    } else {
                        buf.append(aChar);
                    }
            }
        }
        return result;
    }

    private int inc() {
        int idx = fStack.size() - 1;
        int num = fStack.get(idx);
        fStack.set(idx, num + 1);
        return num;
    }

    public void onValue(String value) {
        if (value == null) {
            print("null");
        } else {
            print("'");
            fBuf.delete(0, fBuf.length());
            escape(value, false, fBuf);
            print(fBuf.toString());
            print("'");
        }
    }

    protected abstract void print(String string);

    public void printEscaped1(String str, boolean escapeSpace) {
        if (str == null)
            return;
        int len = str.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        for (int x = 0; x < len; x++) {
            char aChar = str.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        print("\\");
                    print(" ");
                    break;
                case '\'':
                    print("\\'");
                    break;
                case '\"':
                    print("\\\"");
                    break;
                case '\t':
                    print("\\t");
                    break;
                case '\n':
                    print("\\n");
                    break;
                case '\r':
                    print("\\r");
                    break;
                case '\f':
                    print("\\f");
                    break;
                // case ':': // Fall through
                // case ';': // Fall through
                // case '=': // Fall through
                // case '!':
                // print("\\" + aChar);
                // break;
                default:
                    if ((aChar > 61) && (aChar < 127)) {
                        if (aChar == '\\') {
                            print("\\\\");
                            continue;
                        }
                        print("" + aChar);
                        continue;
                    }
                    if (aChar < 0x0020) {
                        print("\\u");
                        print(""
                            + Integer.toHexString((aChar >> 12) & 0xF)
                            + Integer.toHexString((aChar >> 8) & 0xF)
                            + Integer.toHexString((aChar >> 4) & 0xF)
                            + Integer.toHexString(aChar & 0xF));
                    } else {
                        print("" + aChar);
                    }
            }
        }
    }

}
