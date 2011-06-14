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
package org.webreformatter.commons.parsers;

/**
 * @author kotelnikov
 */
public class AbstractStringParser {

    protected static final char NULL = '\0';

    protected int appendChar(StringBuilder buf, char ch, int spaceCounter) {
        if (isSpace(ch)) {
            spaceCounter++;
        } else {
            if (spaceCounter > 0 && buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(ch);
            spaceCounter = 0;
        }
        return spaceCounter;
    }

    protected char getEscapedSymbol(char ch) {
        switch (ch) {
            case 'n':
                ch = '\n';
                break;
            case 't':
                ch = '\t';
                break;
            case 'r':
                ch = '\r';
                break;
            default:
                ;
        }
        return ch;
    }

    /**
     * @param ch the character to check
     * @return <code>true</code> if the specified character is a quotation
     *         character
     */
    protected char getQuot(char ch) {
        return isQuot(ch) ? ch : NULL;
    }

    /**
     * @param ch the character to check
     * @return <code>true</code> if the specified character is a delimiter
     *         character
     */
    protected boolean isDelimiter(char ch) {
        return ch == ',' || ch == ';';
    }

    protected boolean isQuot(char ch) {
        return ch == '\'' || ch == '"';
    }

    /**
     * @param ch the character to check
     * @return <code>true</code> if the specified character is a space character
     */
    protected boolean isSpace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    protected String readQuot(CharStream stream) {
        return readQuot(stream, '\\');
    }

    protected String readQuot(CharStream stream, char escapeChar) {
        String result = null;
        char ch = stream.getChar();
        if (ch == '"' || ch == '\'') {
            stream.incPos();
            StringBuilder builder = new StringBuilder();
            char quot = ch;
            boolean escaped = false;
            for (; !stream.isTerminated(); stream.incPos()) {
                ch = stream.getChar();
                if (escaped) {
                    builder.append(ch);
                    escaped = false;
                } else {
                    if (ch == quot) {
                        stream.incPos();
                        break;
                    }
                    if (escaped = (ch == escapeChar)) {
                        continue;
                    }
                    builder.append(ch);
                }
            }
            result = builder.toString();
        }
        return result;
    }

    protected int skipDelimiter(char[] array, int pos) {
        for (; pos < array.length && isDelimiter(array[pos]); pos++) {
        }
        return pos;
    }

    protected boolean skipDelimiter(CharStream stream) {
        boolean result = false;
        for (char ch = stream.getChar(); !stream.isTerminated()
            && isDelimiter(ch); stream.incPos()) {
            ch = stream.getChar();
            result = true;
        }
        return result;
    }

    protected int skipQuot(char[] array, int pos) {
        if (pos >= array.length) {
            return pos;
        }
        char quot = getQuot(array[pos]);
        if (quot == NULL) {
            return pos;
        }
        for (pos++; quot != NULL && pos < array.length; pos++) {
            char ch = array[pos];
            if (quot == ch) {
                quot = NULL;
                break;
            }
        }
        return pos;
    }

    protected int skipQuot(char[] array, int pos, StringBuffer buf) {
        if (pos >= array.length) {
            return pos;
        }
        char quot = getQuot(array[pos]);
        if (quot == NULL) {
            return pos;
        }
        boolean escaped = false;
        for (pos++; quot != NULL && pos < array.length; pos++) {
            char ch = array[pos];
            if (escaped) {
                ch = getEscapedSymbol(ch);
                buf.append(ch);
                escaped = false;
                continue;
            }
            escaped = ch == '\\';
            if (escaped) {
                continue;
            }
            if (quot == ch) {
                quot = NULL;
                break;
            }
            buf.append(ch);
        }
        return pos;
    }

    protected void skipQuotNormalizingSpaces(CharStream array, StringBuilder buf) {
        if (array.isTerminated()) {
            return;
        }
        char ch = array.getChar();
        char quot = getQuot(ch);
        int spaceCounter = 0;
        boolean escaped = false;
        for (; quot != NULL && !array.isTerminated(); array.incPos()) {
            ch = array.getChar();
            if (escaped) {
                ch = getEscapedSymbol(ch);
                spaceCounter = appendChar(buf, ch, spaceCounter);
                escaped = false;
                continue;
            }
            escaped = ch == '\\';
            if (escaped) {
                continue;
            }
            if (quot == ch) {
                quot = NULL;
                break;
            }
            spaceCounter = appendChar(buf, ch, spaceCounter);
        }
    }

    protected int skipSpaces(char[] array, int pos) {
        for (; pos < array.length && isSpace(array[pos]); pos++) {
        }
        return pos;
    }

    protected boolean skipSpaces(CharStream stream) {
        boolean result = false;
        char ch = stream.getChar();
        while (isSpace(ch) && stream.incPos()) {
            ch = stream.getChar();
            result = true;
        }
        return result;
    }

}