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
package org.webreformatter.commons.parsers.json;

import java.util.Stack;

import org.webreformatter.commons.parsers.json.IJSONListener;

/**
 * @author kotelnikov
 */
public class JSONHelper {

    private abstract class Action {

        private int fType;

        public Action(int type) {
            fType = type;
        }

        public abstract void begin();

        public boolean doCheck(int type) {
            return (type & fType) != 0;
        }

        public abstract void end();
    }

    private static final int TYPE_ARRAY = 1;

    private static final int TYPE_OBJECT = 2;

    private static final int TYPE_OBJECT_PROPERTY = 4;

    private IJSONListener fListener;

    private Action fPrevAction;

    private Stack<Action> fStack = new Stack<Action>();

    /**
     * 
     */
    public JSONHelper(IJSONListener listener) {
        fListener = listener;
    }

    public void addArrayValue(String... values) {
        if (!check(TYPE_ARRAY))
            throw new IllegalStateException(
                "Value should be defined as an array element.");
        for (String str : values) {
            fListener.beginArrayElement();
            fListener.onValue(str);
            fListener.endArrayElement();
        }
    }

    public void addProperty(String property, String value) {
        if (!check(TYPE_OBJECT))
            throw new IllegalStateException(
                "Object property can be defined only for an object.");
        fListener.beginObjectProperty(property);
        fListener.onValue(value);
        fListener.endObjectProperty(property);
    }

    public void beginArray() {
        if (!isRoot() && !check(TYPE_OBJECT_PROPERTY | TYPE_ARRAY))
            throw new IllegalStateException(
                "Array can be defined only as a value of a property "
                    + "or as a value of an another array.");
        push(new Action(TYPE_ARRAY) {
            @Override
            public void begin() {
                if (check(TYPE_ARRAY))
                    fListener.beginArrayElement();
                fListener.beginArray();
            }

            @Override
            public void end() {
                fListener.endArray();
                if (check(TYPE_ARRAY))
                    fListener.endArrayElement();
            }
        });
    }

    public void beginObject() {
        if (!isRoot() && !check(TYPE_OBJECT_PROPERTY | TYPE_ARRAY))
            throw new IllegalStateException(
                "Object can be defined only as a value of a property "
                    + "or as a value of an array.");
        push(new Action(TYPE_OBJECT) {
            @Override
            public void begin() {
                if (check(TYPE_ARRAY))
                    fListener.beginArrayElement();
                fListener.beginObject();
            }

            @Override
            public void end() {
                fListener.endObject();
                if (check(TYPE_ARRAY))
                    fListener.endArrayElement();
            }
        });
    }

    public void beginProperty(final String property) {
        if (!check(TYPE_OBJECT))
            throw new IllegalStateException(
                "Object property can be defined only for an object.");
        push(new Action(TYPE_OBJECT_PROPERTY) {
            @Override
            public void begin() {
                fListener.beginObjectProperty(property);
            }

            @Override
            public void end() {
                if (!checkPrevious(TYPE_OBJECT | TYPE_ARRAY))
                    fListener.onValue(null);
                fListener.endObjectProperty(property);
            }
        });
    }

    private boolean check(int type) {
        if (isRoot())
            return false;
        Action info = fStack.peek();
        return info.doCheck(type);
    }

    private boolean checkPrevious(int type) {
        if (isRoot())
            return false;
        if (fPrevAction == null)
            return false;
        return fPrevAction.doCheck(type);
    }

    public void end() {
        pop();
    }

    /**
     * @return
     */
    private boolean isRoot() {
        return fStack.isEmpty();
    }

    private void pop() {
        Action info = fStack.pop();
        info.end();
        fPrevAction = info;
    }

    private Action push(Action info) {
        info.begin();
        fStack.push(info);
        fPrevAction = null;
        return info;
    }

}
