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

import org.ubimix.commons.parsers.CharStream;

/**
 * @author kotelnikov
 */
public class JSONParser {

    private IJSONListener fListener;

    private CharStream fStream;

    protected char getQuot(char c) {
        return c == '\'' || c == '"' ? c : 0;
    }

    protected boolean isSpace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    public void parse(CharStream stream, IJSONListener listener) {
        fListener = listener;
        fStream = stream;
        for (char ch = fStream.getChar(); ch != 0
            && (ch = fStream.getChar()) != '{'; fStream.incPos()) {
        }
        skipObject();
        fListener = null;
    }

    public void parse(String str, IJSONListener listener) {
        CharStream stream = new CharStream(str);
        parse(stream, listener);
    }

    private boolean skipArray() {
        skipSpaces();
        char ch = fStream.getChar();
        if (ch <= 0) {
            return false;
        }
        if (ch != '[') {
            return false;
        }
        fStream.incPos();
        fListener.beginArray();
        while ((ch = fStream.getChar()) > 0) {
            if (fStream.getChar() == ']') {
                fStream.incPos();
                break;
            }
            fListener.beginArrayElement();
            skipToken();
            fListener.endArrayElement();
            skipSpaces();
            if (fStream.getChar() == ',') {
                fStream.incPos();
                skipSpaces();
            }
        }
        fListener.endArray();
        return true;
    }

    private boolean skipObject() {
        skipSpaces();
        char ch = fStream.getChar();
        if (ch != '{') {
            return false;
        }
        fStream.incPos();
        fListener.beginObject();
        while ((ch = fStream.getChar()) > 0) {
            if (ch == '}') {
                fStream.incPos();
                break;
            }
            skipSpaces();
            ch = fStream.getChar();
            if (ch <= 0) {
                break;
            }

            String property = skipValue();
            if (property == null || property.length() == 0) {
                break;
            }

            skipSpaces();
            fListener.beginObjectProperty(property);
            ch = fStream.getChar();
            if (ch == ':') {
                fStream.incPos();
                skipToken();
            } else {
                fListener.onValue(null);
            }
            fListener.endObjectProperty(property);
            skipSpaces();

            ch = fStream.getChar();
            if (ch == ',') {
                fStream.incPos();
                skipSpaces();
            }
        }
        fListener.endObject();
        return true;
    }

    private String skipQuot() {
        char ch = fStream.getChar();
        if (ch <= 0) {
            return null;
        }
        char quot = getQuot(ch);
        if (quot == 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        boolean escaped = false;
        for (fStream.incPos(); quot > 0 && (ch = fStream.getChar()) > 0; fStream
            .incPos()) {
            if (escaped) {
                switch (ch) {
                    case 'n':
                        buf.append('\n');
                        break;
                    case 'r':
                        buf.append('\r');
                        break;
                    case 't':
                        buf.append('\t');
                        break;
                    case 'f':
                        buf.append('\f');
                        break;
                    default:
                        buf.append(ch);
                        break;
                }
                escaped = false;
                continue;
            }
            escaped = ch == '\\';
            if (escaped) {
                continue;
            }
            if (quot == ch) {
                fStream.incPos();
                break;
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    private boolean skipSpaces() {
        int i;
        for (i = 0; isSpace(fStream.getChar()); fStream.incPos(), i++) {
        }
        return i > 0;
    }

    private boolean skipToken() {
        boolean result = false;
        skipSpaces();
        if (!(result = skipObject())) {
            if (!(result = skipArray())) {
                String value = skipValue();
                if (value != null) {
                    fListener.onValue(value);
                    result = true;
                }
            }
        }
        return result;
    }

    private String skipValue() {
        skipSpaces();
        String result = skipQuot();
        if (result != null) {
            return result;
        }
        StringBuffer buf = new StringBuffer();
        char ch;
        for (; (ch = fStream.getChar()) > 0; fStream.incPos()) {
            if (isSpace(ch)
                || ch == '{'
                || ch == '}'
                || ch == '['
                || ch == ']'
                || ch == ':'
                || ch == ',') {
                break;
            }
            buf.append(ch);
        }
        return buf.toString();
    }

}