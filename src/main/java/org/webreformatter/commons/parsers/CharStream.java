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
public final class CharStream {

    public interface ICharLoader {
        int readNext();
    }

    public class Marker {

        private Pointer fMarker = fPointer;

        public void close(boolean reset) {
            if (reset) {
                fPointer = fMarker;
            }
            fMarkCounter--;
            if (fMarkCounter == 0) {
                fFirstMark = fPointer;
            }
        }

        public Pointer getPointer() {
            return fMarker;
        }

        public String getSubstring() {
            return getSubstring(fMarker, fPointer.pos - fMarker.pos);
        }

        public String getSubstring(int len) {
            return getSubstring(fMarker, len);
        }

        public String getSubstring(int pos, int len) {
            if (len < 0) {
                throw new IllegalArgumentException("Length is negative");
            }
            if (pos < fMarker.pos) {
                throw new IllegalArgumentException(
                    "Pointer is before the marker");
            }
            if (pos > fPointer.pos) {
                throw new IllegalArgumentException(
                    "Pointer is after the end of the stream");
            }
            len = Math.min(fPointer.pos - pos, len);
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < len; i++) {
                char ch = fBuf[(pos + i) % fBuf.length];
                buf.append(ch);
            }
            return buf.toString();
        }

        public String getSubstring(Pointer pos, int len) {
            return getSubstring(pos.pos, len);
        }

        public String getSubstring(Pointer begin, Pointer end) {
            return getSubstring(begin.pos, end.pos - begin.pos);
        }

        @Override
        public String toString() {
            int len = fPointer.pos - fMarker.pos;
            return "Marker(" + fMarker + ":'" + getSubstring(len) + "')";
        }
    }

    public static class Pointer implements Comparable<Pointer> {

        public static final Pointer START = new Pointer(0, 0, 0);

        public final int column;

        public final int line;

        public final int pos;

        public Pointer(int pos, int line, int linePos) {
            this.pos = pos;
            this.line = line;
            this.column = linePos;
        }

        public int compareTo(Pointer o) {
            int result = pos - o.pos;
            if (result != 0) {
                return result;
            }
            result = line - o.line;
            if (result != 0) {
                return result;
            }
            result = column - o.column;
            if (result != 0) {
                return result;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Pointer)) {
                return false;
            }
            Pointer p = (Pointer) obj;
            return pos == p.pos;
            // return pos == p.pos && line == p.line && column == p.column;
        }

        @Override
        public int hashCode() {
            return pos;
        }

        public Pointer inc(boolean newLine, boolean incLine) {
            int c = newLine ? 0 : column + 1;
            int l = incLine ? line + 1 : line;
            return new Pointer(pos + 1, l, c);
        }

        public int len(Pointer pointer) {
            return pos - pointer.pos;
        }

        @Override
        public String toString() {
            return pos + "[" + line + ":" + column + "]";
        }

    }

    private static final int DELTA = 2;

    private static boolean equals(Object first, Object second) {
        return first == null || second == null ? first == second : first
            .equals(second);
    }

    private Pointer fBeginContext = Pointer.START;

    private char[] fBuf = new char[10];

    private Pointer fEnd;

    private Pointer fFirstMark;

    private ICharLoader fLoader;

    private int fMarkCounter;

    private Pointer fPointer = null;

    private Pointer fTop = Pointer.START;

    public CharStream(ICharLoader loader) {
        fLoader = loader;
    }

    public CharStream(String str) {
        this(new SimpleCharLoader(str));
    }

    /**
     * 
     */
    private void checkPointer() {
        if (fPointer == null) {
            incPos();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CharStream)) {
            return false;
        }
        CharStream o = (CharStream) obj;
        return equals(fLoader, o.fLoader)
            && equals(fPointer, o.fPointer)
            && equals(fTop, o.fTop);
    }

    public Pointer getBeginContext() {
        return fBeginContext;
    }

    public char getChar() {
        checkPointer();
        return (char) (fBuf[fPointer.pos % fBuf.length] & 0xFFFF);
    }

    public Pointer getPointer() {
        checkPointer();
        return fPointer;
    }

    /**
     * @param p
     * @param newLine
     * @return
     */
    private Pointer incPointer(Pointer p) {
        if (p == null) {
            return Pointer.START;
        }
        char ch = fBuf[p.pos % fBuf.length];
        boolean newLine = false;
        boolean incLine = false;
        if (ch == '\r') {
            newLine = true;
            incLine = true;
        } else if (ch == '\n') {
            newLine = true;
            char prev = p.pos > 0 ? fBuf[(p.pos - 1) % fBuf.length] : '\0';
            incLine = prev != '\r';
        }
        p = p.inc(newLine, incLine);
        return p;
    }

    public boolean incPos() {
        fPointer = incPointer(fPointer);
        if (fPointer.pos >= fTop.pos - 1) {
            if (fEnd != null) {
                fPointer = fEnd;
                return false;
            }
            int val = fLoader.readNext();
            if (fMarkCounter > 0
                && fTop.pos - fFirstMark.pos + DELTA >= fBuf.length) {
                int len = fBuf.length * 3 / 2;
                char[] buf = new char[len];
                for (int pos = fFirstMark.pos; pos <= fTop.pos + DELTA; pos++) {
                    buf[pos % buf.length] = fBuf[pos % fBuf.length];
                }
                fBuf = buf;
            }
            char ch;
            if (val >= 0) {
                ch = (char) (val & 0xFFFF);
            } else {
                fEnd = fTop;
                ch = '\0';
            }
            fBuf[fTop.pos % fBuf.length] = ch;
            fTop = incPointer(fTop);
        }
        if (fMarkCounter == 0) {
            fFirstMark = fPointer;
        }
        return true;
    }

    public boolean isBeginContext() {
        return fBeginContext.equals(getPointer());
    }

    public boolean isNewContext() {
        return isBeginContext();
    }

    public boolean isTerminated() {
        return fEnd != null && fEnd.equals(fPointer);
    }

    public void markBeginContext() {
        setBeginContext(getPointer());
    }

    public Marker markPosition() {
        checkPointer();
        if (fMarkCounter == 0) {
            fFirstMark = fPointer;
        }
        fMarkCounter++;
        return new Marker();
    }

    public void setBeginContext(Pointer beginContext) {
        fBeginContext = beginContext;
    }

    @Override
    public String toString() {
        return "CharStream("
            + fTop
            + ":"
            + fPointer
            + "){"
            + fLoader.toString()
            + "}";
    }

}