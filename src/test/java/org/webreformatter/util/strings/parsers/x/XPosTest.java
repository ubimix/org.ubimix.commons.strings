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
import org.webreformatter.util.strings.parsers.x.XPos.IMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.NOTMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.NewLineMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.RangeMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.RepeatMatcher;
import org.webreformatter.util.strings.parsers.x.XPos.StringMatcher;

import junit.framework.TestCase;


/**
 * @author kotelnikov
 */
public class XPosTest extends TestCase {

    /**
     * @param name
     */
    public XPosTest(String name) {
        super(name);
    }

    public IMatcher[] m(IMatcher... matchers) {
        return matchers;
    }

    /**
     * @param str
     * @return
     */
    private XPos newPos(final String str) {
        ICharProvider provider = new StringCharProvider(str);
        XPos pos = new XPos(provider);
        return pos;
    }

    public void test() {
        String str = "01234<abc>ABCDE";
        XPos pos = newPos(str);
        assertEquals(0, pos.getPos());
        new XPos.NOTMatcher(new StringMatcher("<"), false).skip(pos);
        assertEquals(0x5, pos.getPos());
        XPos from = pos.newClone();
        new RangeMatcher(new StringMatcher("<"), new StringMatcher(">"))
            .skip(pos);
        XPos to = pos.newClone();
        assertEquals(0xA, pos.getPos());
        assertEquals("<abc>", from.substring(to));
        new NOTMatcher(new NewLineMatcher(), true).skip(pos);
        assertEquals(0xF, pos.getPos());
    }

    public void testComposite(
        String str,
        IMatcher[] matchers,
        String... controls) throws Exception {
        XPos pos = newPos(str);
        int x = 0;
        for (int i = 0; i < matchers.length; i++) {
            XPos start = pos.newClone();
            String control = controls[i];
            assertTrue(matchers[i].skip(pos));
            assertEquals(x + control.length(), pos.getPos());
            x = pos.getPos();
            String test = pos.substring(start);
            assertEquals(control, test);
        }

        pos = newPos(str);
        XPos start = pos.newClone();
        ANDMatcher sequence = new ANDMatcher(matchers);
        assertTrue(sequence.skip(pos));
        assertEquals(str, pos.substring(start));

    }

    public void testComposites() throws Exception {
        IMatcher[] m;

        {
            m = m(
                new NOTMatcher(new StringMatcher("x"), false),
                new StringMatcher("x"));
            testComposite(" x", m, " ", "x");
        }
        {
            StringMatcher quot = new StringMatcher("\"\"\"");
            m = m(
                new NOTMatcher(quot, false),
                new RangeMatcher(quot, quot),
                new NOTMatcher(new NewLineMatcher(), false));
            testComposite(
                "  \"\"\"first\nsecond\"\"\"  ",
                m,
                "  ",
                "\"\"\"first\nsecond\"\"\"",
                "  ");
        }
        {
            IMatcher newLine = new NewLineMatcher();
            IMatcher newLines = new RepeatMatcher(newLine);
            IMatcher lineContent = new NOTMatcher(newLine, false);
            IMatcher line = new ANDMatcher(lineContent, newLines);
            m = m(newLines, line, line, line);
            testComposite("abc\ncde\nefg", m, "", "abc\n", "cde\n", "efg");

            m = m(newLines, new RepeatMatcher(line));
            testComposite("abc\ncde\nefg", m, "", "abc\ncde\nefg");
        }
        {
            StringMatcher from = new StringMatcher("<");
            StringMatcher to = new StringMatcher(">");
            m = m(
                new NOTMatcher(from, false),
                new RangeMatcher(from, to),
                new NOTMatcher(new NewLineMatcher(), false));
            testComposite("01234<abc>ABCDE", m, "01234", "<abc>", "ABCDE");
        }

    }

    public void testQuot() throws Exception {
        testQuot("abc", "<", ">", null);
        testQuot("<abc>", "<", ">", "<abc>");
    }

    /**
     * @param str
     * @param from
     * @param to
     * @param control
     */
    private void testQuot(
        final String str,
        final String from,
        final String to,
        final String control) {
        XPos pos = newPos(str);
        XPos start = pos.newClone();
        boolean skip = new RangeMatcher(
            new StringMatcher("<"),
            new StringMatcher(">")).skip(pos);

        if (control != null) {
            assertTrue(skip);
            assertEquals(control, pos.substring(start));
        } else {
            assertFalse(skip);
            assertEquals("", pos.substring(start));
        }
    }
}
