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
package org.ubimix.commons.parsers.query;

import org.ubimix.commons.parsers.AbstractStringParser;
import org.ubimix.commons.parsers.CharStream;

/**
 * @author kotelnikov
 */
public class QueryParser extends AbstractStringParser implements IQueryParser {

    /**
     * 
     */
    public QueryParser() {
    }

    protected char getTokenDelimiter() {
        return '=';
    }

    private void parse(
        CharStream stream,
        IQueryParserListener listener,
        boolean top) {
        char tokenDelimiter = getTokenDelimiter();
        while (!stream.isTerminated()) {
            skipSpaces(stream);
            if (stream.isTerminated()) {
                break;
            }

            char ch = stream.getChar();
            if (ch == '(') {
                stream.incPos();
                listener.beginQuery();
                parse(stream, listener, false);
                listener.endQuery();
            } else if (ch == ')') {
                stream.incPos();
                if (!top) {
                    break;
                }
            } else {
                String prefix = skipToken(stream);
                String token = null;
                ch = stream.getChar();
                if (ch == tokenDelimiter) {
                    stream.incPos();
                    skipSpaces(stream);
                    ch = stream.getChar();
                    if (ch == '(') {
                        stream.incPos();
                        listener.beginToken(prefix);
                        parse(stream, listener, false);
                        listener.endToken(prefix);
                    } else {
                        token = skipToken(stream);
                        listener.onToken(prefix, token);
                    }
                } else {
                    token = prefix;
                    prefix = null;
                    listener.onToken(prefix, token);
                }
            }
        }
    }

    /**
     * @see org.ubimix.commons.parsers.query.IQueryParser#parse(java.lang.String,
     *      org.ubimix.commons.parsers.query.IQueryParserListener)
     */
    public void parse(String str, IQueryParserListener listener) {
        CharStream stream = new CharStream(str);
        while (!stream.isTerminated()) {
            listener.beginQuery();
            parse(stream, listener, true);
            listener.endQuery();
        }
    }

    private String readWord(CharStream stream) {
        boolean escaped = false;
        StringBuilder buf = new StringBuilder();
        char tokenDelimiter = getTokenDelimiter();
        for (char ch = stream.getChar(); !stream.isTerminated(); stream
            .incPos(), ch = stream.getChar()) {
            if (escaped) {
                buf.append(ch);
                escaped = false;
            } else {
                if (escaped = (ch == '\\')) {
                    continue;
                }
                if (isSpace(ch)
                    || ch == '('
                    || ch == ')'
                    || ch == tokenDelimiter) {
                    break;
                }
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    private String skipToken(CharStream stream) {
        skipSpaces(stream);
        String result = null;
        char ch = stream.getChar();
        if (isQuot(ch)) {
            result = readQuot(stream);
        } else {
            result = readWord(stream);
        }
        skipSpaces(stream);
        return result;
    }

}
