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
package org.webreformatter.util.strings.parsers.mailaddress;

import junit.framework.TestCase;

import org.webreformatter.commons.parsers.mailaddress.UserAddressParser;
import org.webreformatter.commons.parsers.mailaddress.UserAddressParser.IListener;

/**
 * @author kotelnikov
 */
public class UserAdressParserTest extends TestCase {

    public UserAdressParserTest(String name) {
        super(name);
    }

    public void test() {
        test("<titi>", "[][titi]");

        test("a,b,c", "[a][]", "[b][]", "[c][]");
        test("a;b;c", "[a][]", "[b][]", "[c][]");

        test(
            "a<x@foo.bar>;b<y@foo.bar>;c<z@foo.bar>",
            "[a][x@foo.bar]",
            "[b][y@foo.bar]",
            "[c][z@foo.bar]");

        test("Toto", "[Toto][]");
        test("Toto Tata", "[Toto Tata][]");
        test("   Toto        Tata      ", "[Toto        Tata][]");
        test("<titi>", "[][titi]");
        test("toto tata <titi>", "[toto tata][titi]");
        test(
            "  toto      tata   <  titi  >    xxxx ",
            "[toto      tata][titi]",
            "[xxxx][]");

        test(
            "\"toto tata\" xxx   <  titi @ xxxx ",
            "[toto tata][]",
            "[xxx][titi @ xxxx]");

        test(
            "\"toto tata\"xxx   <  titi @ xxxx ",
            "[toto tata][]",
            "[xxx][titi @ xxxx]");

        test(
            "  toto      tata   <  titi @ xxxx ",
            "[toto      tata][titi @ xxxx]");
        test("toto<tata>, titi<toto>", "[toto][tata]", "[titi][toto]");

        // Quoted names
        test(" \" titi    tutu  \"   <toto>   ", "[titi    tutu][toto]");
        test(
            "\"toto tata\" <tata>,  \" titi tutu \" <toto>",
            "[toto tata][tata]",
            "[titi tutu][toto]");

        test(
            " abc's cde \" titi    tutu  \"   <toto>   ",
            "[abc's cde \" titi    tutu  \"][toto]");

    }

    private void test(String str, String... controls) {
        UserAddressParser parser = new UserAddressParser();
        final StringBuilder test = new StringBuilder();
        parser.parseAdressList(str, new IListener() {
            public boolean onUserAddress(String email, String name) {
                if (test.length() > 0) {
                    test.append(",");
                }
                test
                    .append("[")
                    .append(name != null ? name : "")
                    .append("][")
                    .append(email != null ? email : "")
                    .append("]");
                return true;
            }
        });
        StringBuilder control = new StringBuilder();
        for (String controlStr : controls) {
            if (control.length() > 0) {
                control.append(",");
            }
            control.append(controlStr);
        }
        assertEquals(control.toString(), test.toString());
    }

}
