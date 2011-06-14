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

public class XPos {

    public static class ANDMatcher implements IMatcher {

        private IMatcher[] fMatchers;

        public ANDMatcher(IMatcher... matchers) {
            fMatchers = matchers;
        }

        public boolean skip(XPos pos) {
            if (fMatchers.length == 0)
                return false;
            boolean result = true;
            XPos clone = fMatchers.length > 0 ? pos.newClone() : null;
            for (int i = 0; result && i < fMatchers.length; i++) {
                result = fMatchers[i].skip(pos);
            }
            if (!result && clone != null) {
                pos.restore(clone);
            }
            return result;
        }

    }

    public static class CharSetMatcher implements IMatcher {

        private char[] fCharSet;

        public CharSetMatcher(char quot) {
            fCharSet = new char[] { quot };
        }

        public CharSetMatcher(char[] set) {
            fCharSet = set;
        }

        public CharSetMatcher(String set) {
            this(set.toCharArray());
        }

        public boolean skip(XPos pos) {
            boolean result = false;
            for (int i = 0; !result && i < fCharSet.length; i++) {
                char ch = pos.getChar();
                result = ch == fCharSet[i];
            }
            if (result)
                pos.inc();
            return result;
        }

    }

    public interface IMatcher {
        boolean skip(XPos pos);
    }

    public static class NewLineMatcher implements IMatcher {

        public boolean skip(XPos pos) {
            char ch = pos.getChar();
            boolean result = ch == '\n' || ch == '\r';
            if (ch == '\r') {
                XPos clone = pos.newClone();
                pos.inc();
                if (pos.getChar() != '\n')
                    pos.restore(clone);
            }
            if (result)
                pos.inc();
            return result;
        }

    }

    public final static class NOTMatcher implements IMatcher {

        private IMatcher fMatcher;

        private boolean fSkip;

        public NOTMatcher(IMatcher matcher, boolean skipMatcher) {
            fMatcher = matcher;
            fSkip = skipMatcher;
        }

        /**
         * @param pos
         * @return
         */
        protected boolean inc(XPos pos) {
            return pos.inc();
        }

        public boolean skip(XPos pos) {
            boolean result = false;
            int counter = -1;
            if (fSkip) {
                do {
                    if (fMatcher.skip(pos)) {
                        result = true;
                        break;
                    } else {
                        counter++;
                    }
                } while (inc(pos));
            } else {
                do {
                    XPos clone = pos.newClone();
                    if (fMatcher.skip(pos)) {
                        result = true;
                        pos.restore(clone);
                        break;
                    } else {
                        counter++;
                        clone = pos.newClone();
                    }
                } while (inc(pos));
            }
            result |= counter > 0;
            return result;
        }

    }

    public final static class ORMatcher implements IMatcher {
        private IMatcher[] fMatchers;

        public ORMatcher(IMatcher... matchers) {
            fMatchers = matchers;
        }

        public boolean skip(XPos pos) {
            if (fMatchers.length == 0)
                return true;
            boolean result = false;
            if (fMatchers.length == 1) {
                return fMatchers[0].skip(pos);
            } else {
                for (int i = 0; !result && i < fMatchers.length; i++) {
                    XPos clone = pos.newClone();
                    if (fMatchers[i].skip(pos)) {
                        result = true;
                    } else {
                        pos.restore(clone);
                    }
                }
            }
            return result;
        }

    }

    public static class QuotMatcher implements IMatcher {

        private CharSetMatcher fMatcher;

        public QuotMatcher(CharSetMatcher matcher) {
            fMatcher = matcher;
        }

        public QuotMatcher(String sequence) {
            this(new CharSetMatcher(sequence));
        }

        public boolean skip(XPos pos) {
            boolean result = false;
            char quot = pos.getChar();
            XPos clone = pos.newClone();
            if (fMatcher.skip(pos)) {
                CharSetMatcher x = new CharSetMatcher(quot);
                NOTMatcher m = new NOTMatcher(x, true);
                result = m.skip(pos);
            }
            if (!result) {
                pos.restore(clone);
            }
            return result;
        }

    }

    public static class RangeMatcher implements IMatcher {

        private IMatcher fFrom;

        private IMatcher fTo;

        public RangeMatcher(IMatcher from, IMatcher to) {
            fFrom = from;
            fTo = new NOTMatcher(to, true);
        }

        public boolean skip(XPos pos) {
            boolean result = false;
            if (fFrom == null || fFrom.skip(pos)) {
                result = fTo.skip(pos);
            }
            return result;
        }

    }

    public static class RepeatMatcher implements IMatcher {

        private IMatcher fMatcher;

        private int fMax;

        private int fMin;

        public RepeatMatcher(IMatcher matcher) {
            this(matcher, -1, -1);
        }

        public RepeatMatcher(IMatcher matcher, int min, int max) {
            if (min < 0)
                max = -1;
            fMatcher = matcher;
            fMin = min;
            fMax = max;
        }

        public boolean skip(XPos pos) {
            int counter = 0;
            XPos clone = (fMin > 0) ? clone = pos.newClone() : null;
            while (!pos.finished() && (fMax < 0 || counter < fMax)) {
                if (!fMatcher.skip(pos)) {
                    break;
                }
                counter++;
            }
            if (clone != null && counter < fMin) {
                pos.restore(clone);
            }
            return counter >= fMin;
        }

    }

    public static class StringMatcher implements IMatcher {

        private char[] fSequence;

        public StringMatcher(char[] set) {
            fSequence = set;
        }

        public StringMatcher(String set) {
            this(set.toCharArray());
        }

        public boolean skip(XPos pos) {
            int len = fSequence != null ? fSequence.length : 0;
            if (len == 0)
                return false;

            char ch = fSequence[0];
            if (ch != pos.getChar())
                return false;
            boolean result = true;
            pos.inc();
            if (fSequence.length > 1) {
                XPos clone = pos.newClone();
                for (int i = 1; result && i < len; i++, pos.inc()) {
                    ch = fSequence[i];
                    char currentChar = pos.getChar();
                    result = currentChar == ch;
                }
                if (!result)
                    pos.restore(clone);
            }
            return result;
        }

    }

    private char fChar;

    private int fLine;

    private int fLinePos;

    int fPos = -1;

    private ICharProvider fProvider;

    public XPos(ICharProvider provider) {
        fProvider = provider;
    }

    protected XPos(XPos pos) {
        restore(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof XPos))
            return false;
        XPos pos = (XPos) obj;
        return fPos == pos.fPos
            && fLine == pos.fLine
            && fLinePos == pos.fLinePos
            && fChar == pos.fChar
            && fProvider.equals(pos.fProvider);
    }

    public boolean finished() {
        return fPos >= fProvider.length();
    }

    public char getChar() {
        if (fPos < 0)
            inc();
        return fChar;
    }

    /**
     * @return the line
     */
    public int getLine() {
        return fLine;
    }

    /**
     * @return the linePos
     */
    public int getLinePos() {
        return fLinePos;
    }

    public int getPos() {
        return fPos >= 0 ? fPos : 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean inc() {
        boolean result = false;
        if (fPos < fProvider.length()) {
            fPos++;
            char ch = fProvider.charAt(fPos);
            if (ch == '\r' || ch == '\n') {
                fLinePos = 0;
                if (ch != '\n' || fChar != '\r')
                    fLine++;
            }
            fChar = ch;
            result = true;
        }
        return result;
    }

    public XPos newClone() {
        XPos pos = new XPos(this);
        return pos;
    }

    public void restore(XPos pos) {
        fChar = pos.fChar;
        fLine = pos.fLine;
        fLinePos = pos.fLinePos;
        fPos = pos.fPos;
        fProvider = pos.fProvider;
    }

    public ICharProvider subset(XPos pos) {
        int a = getPos();
        int b = pos.getPos();
        int from = Math.min(a, b);
        int to = Math.max(a, b);
        return fProvider.subset(from, to - from);
    }

    public String substring(XPos pos) {
        return subset(pos).toString();
    }

    @Override
    public String toString() {
        char ch = getChar();
        return "[" + ch + ":" + fPos + "]";
    }

}