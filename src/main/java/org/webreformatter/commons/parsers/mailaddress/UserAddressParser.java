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
package org.webreformatter.commons.parsers.mailaddress;

import org.webreformatter.commons.parsers.AbstractStringParser;
import org.webreformatter.commons.parsers.CharStream;

/**
 * @author kotelnikov
 */
public class UserAddressParser extends AbstractStringParser {

    public interface IListener {
        boolean onUserAddress(String email, String name);
    }

    @Override
    protected boolean isQuot(char ch) {
        // return ch == '\'' || ch == '"';
        return ch == '"';
    }

    /**
     * Parses the given string and returns a list of {@link UserAdress} objects
     * 
     * @param str the string to parse
     * @return a list of {@link UserAdress} objects corresponding to the
     *         specified string
     */
    public void parseAdressList(String str, IListener listener) {
        CharStream stream = new CharStream(str);
        while (!stream.isTerminated()) {
            for (char ch = stream.getChar(); !stream.isTerminated()
                && (isSpace(ch) || isDelimiter(ch)); stream.incPos(), ch = stream
                .getChar()) {
            }
            if (stream.isTerminated()) {
                break;
            }
            String name = readQuot(stream);
            if (name == null) {
                name = readWords(stream);
            }
            skipSpaces(stream);
            String email = skipMail(stream);
            name = name.trim();
            if (email == null) {
                email = "";
            } else {
                email = email.trim();
            }
            if (name.length() > 0 || name.length() > 0) {
                if ("".equals(email) && name.indexOf('@') > 0) {
                    email = name;
                    name = "";
                }
            }
            if (!listener.onUserAddress(email, name)) {
                break;
            }
        }
    }

    private String readWords(CharStream stream) {
        StringBuilder builder = new StringBuilder();
        for (char ch = stream.getChar(); !stream.isTerminated()
            && !isDelimiter(ch)
            && ch != '<'; stream.incPos(), ch = stream.getChar()) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private String skipMail(CharStream stream) {
        char ch = stream.getChar();
        if (ch != '<') {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (stream.incPos(), ch = stream.getChar(); !stream.isTerminated(); stream
            .incPos(), ch = stream.getChar()) {
            if (ch == '>') {
                stream.incPos();
                break;
            }
            builder.append(ch);
        }
        return builder.toString();
    }

}