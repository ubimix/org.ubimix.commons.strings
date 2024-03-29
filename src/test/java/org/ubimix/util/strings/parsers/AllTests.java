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
package org.ubimix.util.strings.parsers;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ubimix.util.strings.parsers.mailaddress.UserAdressParserTest;
import org.ubimix.util.strings.parsers.query.QueryParserTest;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
            "Test for org.ubimix.commons.strings.parsers");
        // $JUnit-BEGIN$
        suite.addTestSuite(UserAdressParserTest.class);
        suite.addTestSuite(QueryParserTest.class);
        suite.addTestSuite(JSONParserTest.class);
        // $JUnit-END$
        return suite;
    }

}
