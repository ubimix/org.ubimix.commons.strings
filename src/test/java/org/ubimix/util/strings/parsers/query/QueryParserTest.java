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
package org.ubimix.util.strings.parsers.query;

import java.text.ParseException;

import junit.framework.TestCase;

import org.ubimix.commons.parsers.query.IQueryParser;
import org.ubimix.commons.parsers.query.IQueryParserListener;
import org.ubimix.commons.parsers.query.QueryParser;

/**
 * @author MikhailKotelnikov
 */
public class QueryParserTest extends TestCase {

    /**
     * @param name
     */
    public QueryParserTest(String name) {
        super(name);
    }

    protected IQueryParser newQueryParser() {
        return new QueryParser();
    }

    /**
     * @throws ParseException
     */
    public void test() {
        testQuery("'this is a \\'long\\' token'", "[{this is a 'long' token}]");

        testQuery("a(b)c", "[{a}[{b}]{c}]");

        testQuery(
            "x:livesIn/rdf:label  =  'Paris'",
            "[{x:livesIn/rdf:label=Paris}]");
        testQuery(
            "x:livesIn = ( rdf:type = x:City rdf:label = Paris )",
            "[{x:livesIn=[{rdf:type=x:City}{rdf:label=Paris}]}]");

        testQuery("a:b = c:d", "[{a:b=c:d}]");
        testQuery("a:b = c:d :x", "[{a:b=c:d}{:x}]");

        testQuery("a=b", "[{a=b}]");
        testQuery("a=b    ", "[{a=b}]");
        testQuery("      a=b    ", "[{a=b}]");
        testQuery("   a=b", "[{a=b}]");
        testQuery("a=     b", "[{a=b}]");

        testQuery(" a=b c=d ", "[{a=b}{c=d}]");
        testQuery("a(b)c", "[{a}[{b}]{c}]");

        testQuery(
            "from=(john.smith@example.com) OR to=(john.smith@example.com)",
            "[{from=[{john.smith@example.com}]}{OR}{to=[{john.smith@example.com}]}]");

        testQuery("", "[]");
        testQuery("toto", "[{toto}]");
        testQuery("toto titi", "[{toto}{titi}]");
        testQuery("toto (titi", "[{toto}[{titi}]]");
        testQuery("toto (titi) tata", "[{toto}[{titi}]{tata}]");
        testQuery("toto ) tata", "[{toto}{tata}]");
        // Escaping=
        testQuery("toto\\(titi", "[{toto(titi}]");

        testQuery("'this is a long token'", "[{this is a long token}]");
        testQuery("'this is a \"long\" token'", "[{this is a \"long\" token}]");
        testQuery("'this is a \\'long\\' token'", "[{this is a 'long' token}]");
        testQuery(
            "'this is a \\\"long\\\" token'",
            "[{this is a \"long\" token}]");
        testQuery("'this is a long token", "[{this is a long token}]");

        testQuery("\"this is a long token\"", "[{this is a long token}]");
        testQuery("\"this is a \'long\' token\"", "[{this is a 'long' token}]");
        testQuery(
            "\"this is a \\'long\\' token\"",
            "[{this is a 'long' token}]");
        testQuery(
            "\"this is a \\\"long\\\" token\"",
            "[{this is a \"long\" token}]");
        testQuery(
            "\"this is a \\\"long\\\" token\"",
            "[{this is a \"long\" token}]");
        testQuery("\"this is a long token", "[{this is a long token}]");

        testQuery("\\\"a\\\"", "[{\"a\"}]");

        testQuery(" subj = ( x AND y )", "[{subj=[{x}{AND}{y}]}]");
        testQuery(" subj = ( x AND y ", "[{subj=[{x}{AND}{y}]}]");

        testQuery("abc= d", "[{abc=d}]");
        testQuery("=b", "[{=b}]");
        testQuery("a=b", "[{a=b}]");
        testQuery("a='long token'", "[{a=long token}]");
        testQuery("a=\"long token\"", "[{a=long token}]");
        testQuery("a=\"long token\"", "[{a=long token}]");
        testQuery("=", "[{=}]");

        testQuery("a=b=c", "[{a=b}{=c}]");
        testQuery("a=b c=d e=f abc", "[{a=b}{c=d}{e=f}{abc}]");

        testQuery(
            "from='Alex Polonsky' 'Semantic Web'",
            "[{from=Alex Polonsky}{Semantic Web}]");
        testQuery(
            "from=Alex Polonsky Semantic Web",
            "[{from=Alex}{Polonsky}{Semantic}{Web}]");
        testQuery(
            "from=a to=b label='Hello world' Test Keywords",
            "[{from=a}{to=b}{label=Hello world}{Test}{Keywords}]");
        testQuery(
            "from=a (to=b label='Hello world' Test) Keywords",
            "[{from=a}[{to=b}{label=Hello world}{Test}]{Keywords}]");

        testQuery("a 'b", "[{a}{b}]");
        testQuery("a AND b", "[{a}{AND}{b}]");
        testQuery("a AND (b OR c)", "[{a}{AND}[{b}{OR}{c}]]");

        testQuery("a AND (b OR c", "[{a}{AND}[{b}{OR}{c}]]");
        testQuery("a AND ((((b OR c", "[{a}{AND}[[[[{b}{OR}{c}]]]]]");

        testQuery(
            "from=amy(dinner OR movie)",
            "[{from=amy}[{dinner}{OR}{movie}]]");
        testQuery("~fuzzy", "[{~fuzzy}]");
        testQuery("xx=~fuzzy", "[{xx=~fuzzy}]");
        testQuery("~xx=fuzzy", "[{~xx=fuzzy}]");
        testQuery(
            "  after=2004/04/16    before=2004/04/18  ",
            "[{after=2004/04/16}{before=2004/04/18}]");
        testQuery("from=(toto@foo.bar)", "[{from=[{toto@foo.bar}]}]");
        testQuery("from='abc cde efg'", "[{from=abc cde efg}]");
    }

    private void testQuery(String query, String control) {
        final StringBuffer buf = new StringBuffer();
        IQueryParser parser = newQueryParser();
        IQueryParserListener listener = new IQueryParserListener() {

            public void beginQuery() {
                buf.append("[");
            }

            public void beginToken(String prefix) {
                buf.append("{");
                buf.append(prefix);
                buf.append("=[");
            }

            public void endQuery() {
                buf.append("]");
            }

            public void endToken(String prefix) {
                buf.append("]}");
            }

            public void onToken(String prefix, String token) {
                buf.append("{");
                if (prefix != null) {
                    buf.append(prefix);
                    buf.append("=");
                }
                if (token != null) {
                    buf.append(token);
                }
                buf.append("}");
            }

        };
        parser.parse(query, listener);
        assertEquals(control, buf.toString());
    }

    public void testSpeed() {
        int count = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            testQuery("from=Alex to=Mikhail "
                + "subject= "
                + "(NOT 'Hello, world' AND 'this is a \\'long\\' token')"
                + "", ""
                + "["
                + "{from=Alex}"
                + "{to=Mikhail}"
                + "{subject="
                + "[{NOT}{Hello, world}{AND}{this is a 'long' token}]"
                + "}"
                + "]");
        }
        long stop = System.currentTimeMillis();
        long time = (stop - start);
        String score = time > 0 ? "" + count / time : "undefined";
        System.out.println(time + "ms (" + score + " per millisec)");
    }
}
