package org.ubimix.commons.parsers;

import org.ubimix.commons.parsers.CharStream.ICharLoader;

public class SimpleCharLoader implements ICharLoader {

    private char[] fArray;

    private int fArrayPos;

    private String fString;

    public SimpleCharLoader(String str) {
        fString = str;
        fArray = fString.toCharArray();
        fArrayPos = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleCharLoader)) {
            return false;
        }
        SimpleCharLoader o = (SimpleCharLoader) obj;
        return fString.equals(o.fString);
    }

    @Override
    public int hashCode() {
        return fString.hashCode();
    }

    /**
     * @see org.statewalker.tokenizer.CharStream.ICharLoader#readNext()
     */
    public int readNext() {
        return fArrayPos < fArray.length ? fArray[fArrayPos++] : -1;
    }

    @Override
    public String toString() {
        return fString;
    }

}