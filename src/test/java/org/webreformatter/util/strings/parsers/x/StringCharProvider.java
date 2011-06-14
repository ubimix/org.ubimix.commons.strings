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
package org.webreformatter.util.strings.parsers.x;

/**
 * @author kotelnikov
 */
public class StringCharProvider
    implements
    ICharProvider,
    Comparable<StringCharProvider> {

    private char[] fArray;

    private int fHash;

    private int fLen;

    private int fPos;

    public StringCharProvider(char[] charArray, int pos, int len) {
        fArray = charArray;
        if (pos >= charArray.length)
            pos = charArray.length;
        if (len + pos >= charArray.length)
            len = charArray.length - pos;
        if (pos < 0)
            throw new IllegalArgumentException();
        if (len < 0)
            throw new IllegalArgumentException();
        fPos = pos;
        fLen = len;
    }

    /**
     * 
     */
    public StringCharProvider(String str) {
        this(str.toCharArray(), 0, str.length());
    }

    /**
     * @see org.webreformatter.util.strings.parsers.x.ICharProvider#charAt(int)
     */
    public char charAt(int pos) {
        if (pos - fPos >= fLen)
            return 0;
        return fArray[fPos + pos];
    }

    public int compareTo(StringCharProvider o) {
        int result = 0;
        for (int i = fPos, j = o.fPos; result == 0 && i < fLen && j < o.fLen; i++, j++) {
            result = fArray[fPos + i] - fArray[o.fPos + j];
        }
        if (result == 0) {
            result = fLen - o.fLen;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StringCharProvider))
            return false;
        return compareTo((StringCharProvider) obj) == 0;
    }

    @Override
    public int hashCode() {
        int h = fHash;
        if (h == 0) {
            int off = fPos;
            char val[] = fArray;
            int len = fLen;
            for (int i = 0; i < len; i++) {
                h = 31 * h + val[off++];
            }
            fHash = h;
        }
        return h;
    }

    /**
     * @see org.webreformatter.util.strings.parsers.x.ICharProvider#length()
     */
    public int length() {
        return fLen;
    }

    /**
     * @see org.webreformatter.util.strings.parsers.x.ICharProvider#subset(int,
     *      int)
     */
    public ICharProvider subset(int pos, int len) {
        if (pos > fLen)
            pos = fLen;
        if (len > fLen - pos)
            len = fLen - pos;
        return new StringCharProvider(fArray, fPos + pos, len);
    }

    @Override
    public String toString() {
        return fLen > 0 ? new String(fArray, fPos, fLen) : "";
    }
}
