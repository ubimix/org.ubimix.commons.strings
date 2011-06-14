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

import org.webreformatter.util.strings.parsers.x.XPos.ANDMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.CharSetMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.IMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.NOTMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.NewLineMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.ORMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.QuotMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.RangeMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.RepeatMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.StringMatcher;

/**
 * @author kotelnikov
 */
public class XTriplesParser {

    public static class Base {

        public static IMatcher getChar() {
            return new IMatcher() {
                public boolean skip(XPos pos) {
                    if (pos.finished())
                        return false;
                    char ch = pos.getChar();
                    boolean result = Character.isLetterOrDigit(ch)
                        || ch == '!'
                        || ch == '_'
                        || ch == '@';
                    if (result) {
                        pos.inc();
                    }
                    return result;
                }
            };
        }

        public static IMatcher getComment() {
            return new RangeMatcher(new StringMatcher("#"), getNewLine());
        }

        public static IMatcher getEmptyChar() {
            return new ORMatcher(getSpace(), getNewLine());
        }

        public static IMatcher getEmptyChars() {
            return new RepeatMatcher(getEmptyChar(), 1, -1);
        }

        public static IMatcher getEndOfStatement() {
            return new StringMatcher(".");
        }

        public static IMatcher getLiteralLang() {
            return new ANDMatcher(new StringMatcher("@"), getWord());
        }

        public static IMatcher getLiteralType() {
            IMatcher ref = getReference();
            return new ANDMatcher(new StringMatcher("^^"), ref);
        }

        private static IMatcher getNewLine() {
            return new NewLineMatcher();
        }

        public static IMatcher getNewLines() {
            return new RepeatMatcher(getNewLine(), 1, -1);
        }

        public static IMatcher getPrefix() {
            return new ANDMatcher(new StringMatcher("@"), getWord());
        }

        public static IMatcher getQuotedText() {
            return new QuotMatcher("\"\'");
        }

        public static IMatcher getReference() {
            IMatcher w = getWord();
            IMatcher absoluteRef = new RangeMatcher(
                new StringMatcher("<"),
                new StringMatcher(">"));
            IMatcher prefixedRef = new ANDMatcher(w, new StringMatcher(":"), w);
            IMatcher relativeRef = getWord();
            return new ORMatcher(absoluteRef, prefixedRef, relativeRef);
        }

        private static IMatcher getSpace() {
            return new CharSetMatcher(" \t");
        }

        public static IMatcher getSpaces() {
            return new RepeatMatcher(getSpace(), 1, -1);
        }

        public static IMatcher getTripleQuotedText() {
            IMatcher q = getTripleQuotes();
            return new ANDMatcher(q, new NOTMatcher(q, false), q);
            // return new RangeMatcher(q, q);
        }

        public static IMatcher getTripleQuotes() {
            return new StringMatcher("\"\"\"");
        }

        protected static IMatcher getWord() {
            return new RepeatMatcher(getChar(), 1, -1);
        }

    }

    /**
     * 
     */
    public XTriplesParser() {
        super();
    }

    public void parse(String url, ICharProvider provider) {
        XPos pos = new XPos(provider);
        while (parseToken(pos) && !pos.finished()) {
        }
    }

    private boolean parseComment(XPos pos) {
        XPos mark = pos.newClone();
        if (!skipComment(pos)) {
            return false;
        }
        String comment = pos.substring(mark);
        // FIXME Report about the comment
        System.out.println("COMMENT: " + comment);
        return false;
    }

    private boolean parsePrefix(XPos pos) {
        XPos mark = pos.newClone();
        if (!skipNSPrefix(pos))
            return false;

        String prefix = pos.substring(mark);
        skipSpaces(pos);

        mark = pos.newClone();
        if (!skipNSPrefixDeclaration(pos)) {
            // FIXME: report about the bad namespace prefix
            return false;
        }

        skipSpaces(pos);
        String key = pos.substring(mark);
        mark = pos.newClone();
        if (!skipNSValue(pos)) {
            // FIXME: report about the bad namespace value
            return false;
        }

        String value = pos.substring(mark);
        skipSpaces(pos);

        // FIXME: report about the result prefix
        System.out.println("NS:  " + prefix + " - " + key + " - " + value);

        if (!skipEndOfStatement(pos)) {
            // FIXME: report about the bad namespace finish
            return false;
        }
        return true;
    }

    private boolean parseStatement(XPos pos) {
        XPos mark = pos.newClone();
        if (!skipReference(pos)) {
            // FIXME: report about the bad statement subject
            return false;
        }
        String subject = pos.substring(mark);

        skipSpaces(pos);
        mark = pos.newClone();
        if (!skipReference(pos)) {
            // FIXME: report about the bad predicate
            return false;
        }
        String predicate = pos.substring(mark);

        skipSpaces(pos);
        mark = pos.newClone();
        if (skipReference(pos)) {
            String object = pos.substring(mark);
            // FIXME: report reference statement
            System.out.println("REFERENCE: "
                + subject
                + " - "
                + predicate
                + " - "
                + object);
            if (!skipEndOfStatement(pos)) {
                // FIXME: report about the bad end of the statement
                return false;
            }
            return true;
        } else if (skipLiteral(pos)) {
            String object = pos.substring(mark);
            String type = null;
            String lang = null;

            skipSpaces(pos);

            mark = pos.newClone();
            if (skipLiteralType(pos)) {
                type = pos.substring(mark);
                skipSpaces(pos);
                mark = pos.newClone();
            }

            if (skipLiteralLang(pos)) {
                lang = pos.substring(mark);
            }

            // FIXME: report literal statement
            System.out.println("LITERAL:  {s="
                + subject
                + "; p="
                + predicate
                + "; o="
                + object
                + "; type="
                + type
                + "; lang="
                + lang
                + "}");
            if (!skipEndOfStatement(pos)) {
                // FIXME: report about the bad end of the statement
                return false;
            }
            return true;
        } else {
            // FIXME: report about the bad statement
            return false;
        }
    }

    private boolean parseToken(XPos pos) {
        return skipSpaces(pos)
            || parsePrefix(pos)
            || parseComment(pos)
            || parseStatement(pos);
    }

    private boolean skipComment(XPos pos) {
        IMatcher matcher = Base.getComment();
        return matcher.skip(pos);
    }

    private boolean skipEndOfStatement(XPos pos) {
        IMatcher matcher = Base.getEndOfStatement();
        return matcher.skip(pos);
    }

    private boolean skipLiteral(XPos pos) {
        IMatcher matcher;
        matcher = Base.getTripleQuotedText();
        if (!matcher.skip(pos)) {
            matcher = Base.getQuotedText();
            if (!matcher.skip(pos))
                return false;
        }
        return true;
    }

    private boolean skipLiteralLang(XPos pos) {
        IMatcher matcher = Base.getLiteralLang();
        return matcher.skip(pos);
    }

    private boolean skipLiteralType(XPos pos) {
        IMatcher matcher = Base.getLiteralType();
        return matcher.skip(pos);
    }

    private boolean skipNSPrefix(XPos pos) {
        IMatcher matcher = Base.getPrefix();
        return matcher.skip(pos);
    }

    private boolean skipNSPrefixDeclaration(XPos pos) {
        IMatcher matcher = Base.getWord();
        return matcher.skip(pos);
    }

    private boolean skipNSValue(XPos pos) {
        IMatcher matcher = Base.getReference();
        return matcher.skip(pos);
    }

    private boolean skipReference(XPos pos) {
        IMatcher matcher = Base.getReference();
        return matcher.skip(pos);
    }

    private boolean skipSpaces(XPos pos) {
        IMatcher matcher = Base.getEmptyChars();
        return matcher.skip(pos);
    }

}
