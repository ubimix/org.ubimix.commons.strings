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

import junit.framework.TestCase;

/**
 * @author kotelnikov
 */
public class XTripleParserTest extends TestCase {

    /**
     * @param name
     */
    public XTripleParserTest(String name) {
        super(name);
    }

    private String doParseSerialize(String str) {
        final StringBuffer buf = new StringBuffer();
        XTriplesParser parser = new XTriplesParser();
        parse(parser, str);
        String test = buf.toString();
        return test;
    }

    /**
     * @param parser
     * @param str
     */
    private void parse(XTriplesParser parser, String str) {
        StringCharProvider provider = new StringCharProvider(str);
        parser.parse("", provider);
    }

    public void test() throws Exception {
        test("@prefix a <x>.\n", "");
        test("@  a y", "");
        test("" + "<A><B>\"C\"@en.\n" + "<A><B>\"C\"^^<T>.\n" + "", ""
            + "[A] [B] 'C'@en.\n"
            + "[A] [B] 'C'^^[T].\n"
            + "");

        test("\n\n", "");
        test("# This is a comment", "");
        test(
            "# This is a comment\n"
                + "<http://example.org/resource1> <http://example.org/property> <http://example.org/resource2> .",
            "");

        test(
            "<r12> <p> \"\"\"first\nsecond\nthird\"\"\"",
            "[r12] [p] 'return\t '.\n");
        test(
            "<r12> <p> \"\"\"first\nsecond\nthird\"\"\"@fr",
            "[r12] [p] 'return\t '.\n");

        test("<s1><p1>\"a\\nb\"\n", "[s1] [p1] 'a\nb'.\n");
        test("_:s _:p _:o", "[_:s] [_:p] [_:o].\n");
        test("    <a>        <b>           'c'^^<t>.", "[a] [b] 'c'^^[t].\n");
        test("<a><b>'c'^^<t>.", "[a] [b] 'c'^^[t].\n");
        test("<a><b>'c'@en.", "[a] [b] 'c'@en.\n");
        test("<a><b>'c'.", "[a] [b] 'c'.\n");
        test("", "");
        test(""
            + "<a> <b> <c>.\n"
            + "<x> <y> 'Z'.\n"
            + "<A><B>\"C\"@en.\n"
            + "<A><B>\"C\"^^<T>.\n"
            + "", ""
            + "[a] [b] [c].\n"
            + "[x] [y] 'Z'.\n"
            + "[A] [B] 'C'@en.\n"
            + "[A] [B] 'C'^^[T].\n"
            + "");
        test(
            "<http://example.org/resource1> <http://example.org/property> <http://example.org/resource2> .",
            "[http://example.org/resource1] [http://example.org/property] [http://example.org/resource2].\n");
        test(
            "_:anon <http://example.org/property> <http://example.org/resource2> .",
            "[_:anon] [http://example.org/property] [http://example.org/resource2].\n");

        test("<r7> <p> \"simple literal\" .\n", "[r7] [p] 'simple literal'.\n");
        test("<r8> <p> \"backslash:\\\\\" .\n", "[r8] [p] 'backslash:\\'.\n");
        test("<r9> <p> \"dquote:\\\"\" .\n", "[r9] [p] 'dquote:\"'.\n");
        test("<r10> <p> \"newline:\\n\" .\n", "[r10] [p] 'newline:\n'.\n");
        test("<r11> <p> \"return\\r\" .\n", "[r11] [p] 'return\r'.\n");
        test("<r12> <p> \"return\\t\" .\n", "[r12] [p] 'return\t'.\n");

    }

    private void test(String str, String control) {
        final StringBuffer buf = new StringBuffer();
        XTriplesParser parser = new XTriplesParser();
        parse(parser, str);
        // assertEquals(control, buf.toString());
    }

    public void testParseSerialize() {
        testParseSerialize("<s1><p1>\"a\\nb\"\n", "<s1> <p1> \"a\\nb\" .\n");

        testParseSerialize("", "");
        testParseSerialize("<s><p><o>", "<s> <p> <o> .\n");
        testParseSerialize("<s><p>'abc'", "<s> <p> \"abc\" .\n");
        testParseSerialize("<s><p>''", "<s> <p> \"\" .\n");
        testParseSerialize("<s><p>''@en", "<s> <p> \"\"@en .\n");
        testParseSerialize("<s><p>''^^<t>", "<s> <p> \"\"^^<t> .\n");
        testParseSerialize("<s><p>\"abc\"", "<s> <p> \"abc\" .\n");
        testParseSerialize("<s><p>'abc'@fr", "<s> <p> \"abc\"@fr .\n");
        testParseSerialize(
            "      <s>    <p>    'abc'     @fr",
            "<s> <p> \"abc\"@fr .\n");
        testParseSerialize("<s><p>'abc'^^<type>", "<s> <p> \"abc\"^^<type> .\n");
        testParseSerialize(
            "      <s>    <p>    \"abc\"     ^^<type>",
            "<s> <p> \"abc\"^^<type> .\n");
        testParseSerialize("_:s _:p _:o", "_:s _:p _:o .\n");
        testParseSerialize(""
            + "_:s _:p _:o\n"
            + "<s><p><o>\n"
            + "<s1><p1>\"Literal \\nStatement\"\n"
            + "<s2><p2>\"Literal Statement\"@en\n"
            + "<s3><p3>\"123\"^^<xsd:int>", ""
            + "_:s _:p _:o .\n"
            + "<s> <p> <o> .\n"
            + "<s1> <p1> \"Literal \\nStatement\" .\n"
            + "<s2> <p2> \"Literal Statement\"@en .\n"
            + "<s3> <p3> \"123\"^^<xsd:int> .\n");
    }

    private void testParseSerialize(String str, String control) {
        String test = doParseSerialize(str);
        // assertEquals(control, test);
        // test = doParseSerialize(test);
        // assertEquals(control, test);
    }

}
