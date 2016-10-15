/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.beng.xml;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * A set of String library static methods. While extending String or 
 * StringBuffer would have been the nicest solution, that is not 
 * possible, so a simple set of static methods seems the most workable.
 *
 * Method ideas have so far been taken from the PHP4, Ruby and .NET languages.
 */
final public class StringW {

    static public String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

    static public String implode(Object[] objs, String sep, String pre, String post) {
        String ret = implode(objs, sep);
        if( (ret != null) && (ret != "")) { //$NON-NLS-1$
            return pre + ret + post;
        } else {
            return ret;
        }
    }
        

    /**
     * Merge an array of Objects into a list of delimited 
     * Strings. No delimiter is added before or after the list.
     *
     * @param objs Object[] to put into String list form
     * @param sep String delimiter
     *
     * @return String delimited list of the passed in object's toStrings.
     */
    static public String implode(Object[] objs, String sep) {
        if(objs == null) {
            return null;
        }
        int sz = objs.length;
        if(sz == 0) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer buffer = new StringBuffer((objs[0].toString().length()+sep.length())*sz);
        for(int i = 0; i<sz; i++) {
            buffer.append(objs[i]);
            if(i != sz - 1) {
                buffer.append(sep);
            }
        }
        return buffer.toString();
    }


    /**
     * Merge an Iterator of Objects into a list of delimited 
     * Strings. No delimiter is added before or after the list.
     *
     * @param objs Object[] to put into String list form
     * @param sep String delimiter
     *
     * @return String delimited list of the passed in object's toStrings.
     */
    static public String implode(Iterator iterator, String sep) {
        StringBuffer buffer = new StringBuffer();
        while(iterator.hasNext()) {
            buffer.append(iterator.next());
            if(iterator.hasNext()) {
                buffer.append(sep);
            }
        }
        return buffer.toString();
    }
    static public String implode(Enumeration enumParam, String sep) {
        return implode(new EnumerationIterator(enumParam), sep);
    }
    
    /**
     * Turn a space-separated string into an array of Strings.
     *
     * @param str String to separate
     *
     * @return String[] of separated Strings
     */
    static public String[] explode(String str) {
        return explode(str, " ", -1); //$NON-NLS-1$
    }
    /**
     * Turn a separated string into an array of Strings.
     *
     * @param str String to separate
     * @param sep String separator
     *
     * @return String[] of separated Strings
     */
    static public String[] explode(String str, String sep) {
        return explode(str, sep, -1);
    }

    static public String[] explode(String str, String sep, String n) {
        return explode(str,sep,NumberW.stringToInt(n, -1));
    }

    /**
     * Turn a separated string into an array of Strings.
     * A maximum length for the returned array may be passed in.
     * A max length of -1 implies that there need be no limit.
     *
     * @param str String to separate
     * @param sep String separator
     * @param n   int    max length of the array
     *
     * @return String[] of separated Strings
     */
    static public String[] explode(String str, String sep, int n) {
        if(str == null) {
            return null;
        }

        ArrayList list = null;
        if( n == -1) {
            list = new ArrayList();
        } else {
            list = new ArrayList(n);
        }

        int idx = 0;
        int prev = 0;
        int sz = sep.length();
        while( (idx = str.indexOf(sep, idx)) != -1) {
            list.add( str.substring(prev, idx) );
            idx += sz;
            prev = idx;
            n--;
            if(n == 0) {
                break;
            }
        }

        if(n != 0) {
            list.add( str.substring(prev) );
        }

        return (String[])list.toArray(new String[0]);

        /* StringTokenizer doesn't do empty string if two seps in a row
         * is aloso known to be a slow implementation
        StringTokenizer st = new StringTokenizer(str,sep);
        int sz = st.countTokens();
        int count = sz;
        if( (n != -1) && (sz > n) ) {
            sz = n;
        }
        String[] tmp = new String[sz];
        int i = 0;
        while(st.hasMoreTokens()) {
            if(i == sz - 1) {
                StringBuffer buffer = new StringBuffer(2*str.length()*(count-n)/count);
                while(st.hasMoreTokens()) {
                    buffer.append(st.nextToken());
                }
                tmp[i] = buffer.toString();
                break;
            }
            tmp[i] = st.nextToken();
            i++;
        }
        return tmp;
        */
    }


    /**
     * Uncapitalise a string. That is, convert the first character into 
     * lower-case.
     *
     * @param str String to uncapitalise
     *
     * @return String uncapitalised
     */
    static public String uncapitalise(String str) {
        return str.substring(0,1).toLowerCase() + str.substring(1);
    }

    /**
     * Capitalise a string. That is, convert the first character into 
     * title-case.
     *
     * @param str String to capitalise
     *
     * @return String capitalised
     */
    static public String capitalise(String str) {
        return "" + Character.toTitleCase(str.charAt(0)) + str.substring(1); //$NON-NLS-1$
    }

    /**
     * Replace a string with another string inside a larger string, once.
     *
     * @param text String to do search and replace in
     * @param repl String to search for
     * @param with String to replace with
     *
     * @return String with once value replaced
     */
    static public String replaceStringOnce(String text, String repl, String with) {
        return replaceString(text, repl, with, 1);
    }

    /**
     * Replace a string with another string inside a larger string, for
     * all of the search string.
     *
     * @param text String to do search and replace in
     * @param repl String to search for
     * @param with String to replace with
     *
     * @return String with all values replaced
     */
    static public String replaceString(String text, String repl, String with) {
        return replaceString(text, repl, with, -1);
    }
    static public String replaceString(String text, String repl, String with, String n) {
        return replaceString(text, repl, with, NumberW.stringToInt(n,-1));
    }

    /**
     * Replace a string with another string inside a larger string, for
     * the first n values of the search string.
     *
     * @param text String to do search and replace in
     * @param repl String to search for
     * @param with String to replace with
     * @param n    int    values to replace
     *
     * @return String with n values replacEd
     */
    static public String replaceString(String text, String repl, String with, int max) {
        /**
        int idx = 0;
        while( (idx = text.indexOf(repl)) != -1) {
            text = text.substring(0,idx) + with + text.substring(idx+repl.length() );
            idx += with.length();    // jump beyond replacement
            max--;
            if(max == 0) {
                break;
            }
        }
        return text;
        **/
        if(text == null) {
            return null;
        }
 
        StringBuffer buffer = new StringBuffer(text.length());
        int start = 0;
        int end = 0;
        while( (end = text.indexOf(repl, start)) != -1 ) {
            buffer.append(text.substring(start, end)).append(with);
            start = end + repl.length();
 
            if(--max == 0) {
                break;
            }
        }
        buffer.append(text.substring(start));
 
        return buffer.toString();      
    }

    static public String overlayString(String text, String overlay, String start, String end) {
        return overlayString(text,overlay,NumberW.stringToInt(start), NumberW.stringToInt(end));
    }
    /**
     * Overlay a part of a string with another string.
     *
     * @param text String to do overlaying in
     * @param overlay String to overlay
     * @param start int to start overlaying at
     * @param end   int to stop overlaying before
     *
     * @return String with overlayed text
     */
    static public String overlayString(String text, String overlay, int start, int end) {
        String pre = text.substring(0, start);
        String post = text.substring(end);
        return pre+overlay+post;
    }

    static public String repeat(String str, String n) {
        return repeat(str, NumberW.stringToInt(n,1));
    }

    /**
     * Repeat a string n times to form a new string.
     *
     * @param str String to repeat
     * @param n   int    number of times to repeat
     *
     * @return String with repeated string
     */
    static public String repeat(String str, int n) {
        StringBuffer buffer = new StringBuffer(n*str.length());
        for(int i=0; i<n; i++) {
            buffer.append(str);
        }
        return buffer.toString();
    }

// these are not really of use in the Java world. Only if you're a C afficionado
//    static public String sprintf(String format, Object[] list);
//    static public Object[] sscanf(String str, String format);
//    static public String pack(String[] strs, String format);
//    static public String[] unpack(String str, String format);


    /**
     * Center a string in a larger string of size n.
     * Uses spaces as the value to buffer the string with..
     *
     * @param str String to center
     * @param n   int    size of new String
     *
     * @return String containing centered String
     */
    static public String center(String str, int n) {
        return center(str, n, " "); //$NON-NLS-1$
    }

    static public String center(String str, String n, String delim) {
        return center(str,NumberW.stringToInt(n), delim);
    }

    /**
     * Center a string in a larger string of size n.
     * Uses a supplied String as the value to buffer the string with..
     *
     * @param str String to center
     * @param n   int    size of new String
     * @param delim String to buffer the new String with
     *
     * @return String containing centered String
     */
    static public String center(String str, int n, String delim) {
        int sz = str.length();
        int p = n-sz;
        if(p < 1) {
            return str;
        }
        str = leftPad(str,sz+p/2, delim);
        str = rightPad(str, n, delim);
        return str;
    }

    /** 
     * Remove the last newline, and everything after it from a String.
     *
     * @param str String to chomp the newline from
     *
     * @return String without chomped newline
     */
    static public String chomp(String str) {
        return chomp(str, "\n"); //$NON-NLS-1$
    }
    
    /** 
     * Remove the last value of a supplied String, and everything after it 
     * from a String.
     *
     * @param str String to chomp from
     * @param sep String to chomp
     *
     * @return String without chomped ending
     */
    static public String chomp(String str, String sep) {
        int idx = str.lastIndexOf(sep);
        if(idx != -1) {
            return str.substring(0,idx);
        } else {
            return str;
        }
    }
    
    /**
     * Remove a newline if and only if it is at the end 
     * of the supplied string.
     */
    static public String chompLast(String str) {
        return chompLast(str, "\n"); //$NON-NLS-1$
    }
    static public String chompLast(String str, String sep) {
        if(str.length() == 0) {
            return str;
        }
        String sub = str.substring(str.length() - sep.length());
        if(sep.equals(sub)) {
            return str.substring(0,str.length()-sep.length());
        } else {
            return str;
        }
    }

    /** 
     * Remove everything and return the last value of a supplied String, and 
     * everything after it from a String.
     *
     * @param str String to chomp from
     * @param sep String to chomp
     *
     * @return String chomped
     */
    static public String getChomp(String str, String sep) {
        int idx = str.lastIndexOf(sep);
        if(idx == str.length()-sep.length()) {
            return sep;
        } else
        if(idx != -1) {
            return str.substring(idx);
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /** 
     * Remove the first value of a supplied String, and everything before it 
     * from a String.
     *
     * @param str String to chomp from
     * @param sep String to chomp
     *
     * @return String without chomped beginning
     */
    static public String prechomp(String str, String sep) {
        int idx = str.indexOf(sep);
        if(idx != -1) {
            return str.substring(idx+sep.length());
        } else {
            return str;
        }
    }

    /** 
     * Remove and return everything before the first value of a 
     * supplied String from another String.
     *
     * @param str String to chomp from
     * @param sep String to chomp
     *
     * @return String prechomped
     */
    static public String getPrechomp(String str, String sep) {
        int idx = str.indexOf(sep);
        if(idx != -1) {
            return str.substring(0,idx+sep.length());
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Remove the last character from a String. If the String 
     * ends in \r\n, then remove both of them.
     *
     * @param str String to chop last character from
     *
     * @return String without last character
     */
    static public String chop(String str) {
        if("".equals(str)) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        if(str.length() == 1) {
            return ""; //$NON-NLS-1$
        }
        int lastIdx = str.length()-1;
        String ret = str.substring(0,lastIdx);
        char last = str.charAt(lastIdx);
        if(last == '\n') {
            if(ret.charAt(lastIdx-1) == '\r') {
                return ret.substring(0, lastIdx-1);
            }
        }
        return ret;
    }

    /**
     * Remove \n from end of a String if it's there.
     * If a \r precedes it, then remove that too.
     *
     * @param str String to chop a newline from
     *
     * @param String without newline on end
     */
    static public String chopNewline(String str) {
        int lastIdx = str.length()-1;
        char last = str.charAt(lastIdx);
        if(last == '\n') {
            if(str.charAt(lastIdx-1) == '\r') {
                lastIdx --;
            }
        } else {
            lastIdx++;
        }
        return str.substring(0,lastIdx);
    }

//    /**
//     * Creates a CharSet object which allows a certain amount of 
//     * set logic to be performed upon the following syntax:
//     *
//     * "aeio" which implies 'a','e',..
//     * "^e" implies not e. However it only negates, it's not 
//     * a set in itself due to the size of that set in unicode.
//     * "ej-m" implies j->m. j,k,l,m.
//     */
//    static public CharSet evaluateSet(String[] set) {
//        return new CharSet(set); 
//    }

//    static public int count(String str, String set) {
//        String[] strs = new String[1];
//        strs[0] = set;
//        return count(str, strs);
//    }
//    /**
//     * Takes an argument in set-syntax, see evaluateSet,
//     * and returns the number of characters present in the specified string.
//     * An example would be:   count("hello", {"c-f","o"}) returns 2.
//     *
//     * @param str String target to count characters in
//     * @param str String[] set of characters to count
//     */
//    static public int count(String str, String[] set) {
//        CharSet chars = evaluateSet(set);
//        int count = 0;
//        char[] chrs = str.toCharArray();
//        int sz = chrs.length;
//        for(int i=0; i<sz; i++) {
//            if(chars.contains(chrs[i])) {
//                count++;
//            }
//        }
//        return count;
//    }
//
//    static public String delete(String str, String set) {
//        String[] strs = new String[1];
//        strs[0] = set;
//        return delete(str, strs);
//    }
//    /**
//     * Takes an argument in set-syntax, see evaluateSet,
//     * and deletes any of characters present in the specified string.
//     * An example would be:   delete("hello", {"c-f","o"}) returns "hll"
//     *
//     * @param str String target to delete characters from
//     * @param str String[] set of characters to delete
//     */
//    static public String delete(String str, String[] set) {
//        CharSet chars = evaluateSet(set);
//        StringBuffer buffer = new StringBuffer(str.length());
//        char[] chrs = str.toCharArray();
//        int sz = chrs.length;
//        for(int i=0; i<sz; i++) {
//            if(!chars.contains(chrs[i])) {
//                buffer.append(chrs[i]);
//            }
//        }
//        return buffer.toString();
//    }

//    static public String squeeze(String str, String set) {
//        String[] strs = new String[1];
//        strs[0] = set;
//        return squeeze(str, strs);
//    }
//    /**
//     * Squeezes any repititions of a character that is mentioned in the 
//     * supplied set. An example is:
//     *    squeeze("hello", {"el"})  => "helo"
//     * See evaluateSet for set-syntax.
//     */
//    static public String squeeze(String str, String[] set) {
//        CharSet chars = evaluateSet(set);
//        StringBuffer buffer = new StringBuffer(str.length());
//        char[] chrs = str.toCharArray();
//        int sz = chrs.length;
//        char lastChar = ' ';
//        char ch = ' ';
//        for(int i=0; i<sz; i++) {
//            ch = chrs[i];
//            if(chars.contains(ch)) {
//                if( (ch == lastChar) && (i != 0) ) {
//                    continue;
//                }
//            }
//            buffer.append(ch);
//            lastChar = ch;
//        }
//        return buffer.toString();
//    }

    /**
     * Translate characters in a String.
     * An example is:  translate("hello", "ho", "jy") => jelly
     * If the length of characters to search for is greater than the 
     * length of characters to replace, then the last character is 
     * used.
     *
     * @param target String to replace characters  in
     * @param repl String to find that will be replaced
     * @param with String to put into the target String
     */
    static public String translate(String target, String repl, String with) {
        StringBuffer buffer = new StringBuffer(target.length());
        char[] chrs = target.toCharArray();
        char[] withChrs = with.toCharArray();
        int sz = chrs.length;
        int withMax = with.length() - 1;
        for(int i=0; i<sz; i++) {
            int idx = repl.indexOf(chrs[i]);
            if(idx != -1) {
                if(idx > withMax) {
                    idx = withMax;
                }
                buffer.append(withChrs[idx]);
            } else {
                buffer.append(chrs[i]);
            }
        }
        return buffer.toString();
    }
    
    // spec 3.10.6
    /**
     * Escapes any values it finds into their String form.
     * So a tab becomes the characters '\\' and 't'.
     *
     * @param str String to escape values in
     *
     * @return String with escaped values
     */
    // improved with code from  cybertiger@cyberiantiger.org
    // unicode from him, and defaul for < 32's.
    static public String escape(String str) {
        int sz = str.length();
        StringBuffer buffer = new StringBuffer(2*sz);
        for(int i=0; i<sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if(ch > 0xfff) {
                buffer.append("\\u"+Integer.toHexString(ch)); //$NON-NLS-1$
            } else 
            if(ch > 0xff) {
                buffer.append("\\u0"+Integer.toHexString(ch)); //$NON-NLS-1$
            } else 
            if(ch > 0x7f) {
                buffer.append("\\u00"+Integer.toHexString(ch)); //$NON-NLS-1$
            } else 
            if(ch < 32) {
                switch(ch) {
                    case '\b' : 
                        buffer.append('\\');
                        buffer.append('b');
                        break;
                    case '\n' : 
                        buffer.append('\\');
                        buffer.append('n');
                        break;
                    case '\t' : 
                        buffer.append('\\');
                        buffer.append('t');
                        break;
                    case '\f' : 
                        buffer.append('\\');
                        buffer.append('f');
                        break;
                    case '\r' : 
                        buffer.append('\\');
                        buffer.append('r');
                        break;
                    default :
                        if( ch > 0xf ) {
                            buffer.append("\\u00"+Integer.toHexString(ch)); //$NON-NLS-1$
                        } else {
                            buffer.append("\\u000"+Integer.toHexString(ch)); //$NON-NLS-1$
                        }
                        break;
                }
            } else {
                switch(ch) {
                    case '\'' : 
                        buffer.append('\\');
                        buffer.append('\'');
                        break;
                    case '"' : 
                        buffer.append('\\');
                        buffer.append('"');
                        break;
                    case '\\' : 
                        buffer.append('\\');
                        buffer.append('\\');
                        break;
                    default :
                        buffer.append(ch);
                        break;
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Right pad a String with spaces. Pad to a size of n.
     */
    static public String rightPad(String str, int n) {
        return rightPad(str, n, " "); //$NON-NLS-1$
    }
    static public String rightPad(String str, String n, String delim) {
        return rightPad(str, NumberW.stringToInt(n), delim);
    }
    /**
     * Right pad a String with a specified string. Pad to a size of n.
     *
     * @param str   String to pad out
     * @param n     int    size to pad to
     * @param delim String to pad with
     */
    static public String rightPad(String str, int n, String delim) {
        int sz = str.length();
        n = (n-sz)/delim.length();
        if(n > 0) {
            str += repeat(delim,n);
        }
        return str;
    }

    /**
     * Left pad a String with spaces. Pad to a size of n.
     */
    static public String leftPad(String str, int n) {
        return leftPad(str, n, " "); //$NON-NLS-1$
    }
    static public String leftPad(String str, String n, String delim) {
        return leftPad(str, NumberW.stringToInt(n), delim);
    }
    /**
     * Left pad a String with a specified string. Pad to a size of n.
     *
     * @param str   String to pad out
     * @param n     int    size to pad to
     * @param delim String to pad with
     */
    static public String leftPad(String str, int n, String delim) {
        int sz = str.length();
        n = (n-sz)/delim.length();
        if(n > 0) {
            str = repeat(delim,n) + str;
        }
        return str;
    }

    // faster algorithm available. unsure if usable in Java
    /**
     * Reverse a String.
     */
    static public String reverse(String str) {
        /*
        int sz = str.length();
        StringBuffer buffer = new StringBuffer(sz);
        for(int i=sz; i>0; i--) {
            buffer.append(str.charAt(i-1));
        }
        return buffer.toString();
        */
        return new StringBuffer(str).reverse().toString();
    }

    /**
     * Remove whitespace from the front and back of a String.
     */
    static public String strip(String str) {
        return strip(str, null);
    }
    /**
     * Remove a specified String from the front and back of a 
     * String. If Whitespace is wanted to be removed, used the 
     * strip(String) method.
     */
    static public String strip(String str, String delim) {
        str = stripStart(str, delim);
        return stripEnd(str, delim);
    }

    /**
     * Swaps the case of String. Properly looks after 
     * making sure the start of words are Titlecase and not 
     * Uppercase.
     */
    static public String swapCase(String str) {
        int sz = str.length();
        StringBuffer buffer = new StringBuffer(sz);

        boolean whitespace = false;
        char ch = 0;
        char tmp = 0;

        for(int i=0; i<sz; i++) {
            ch = str.charAt(i);
            if(Character.isUpperCase(ch)) {
                tmp = Character.toLowerCase(ch);
            } else
            if(Character.isTitleCase(ch)) {
                tmp = Character.toLowerCase(ch);
            } else
            if(Character.isLowerCase(ch)) {
                if(whitespace) {
                    tmp = Character.toTitleCase(ch);
                } else {
                    tmp = Character.toUpperCase(ch);
                }
            } 
            buffer.append(tmp);
            whitespace = Character.isWhitespace(ch);
        }
        return buffer.toString();
    }


    // From .NET
    /**
     * Find the earlier index of any of a set of potential substrings.
     */
    static public int indexOfAny(String str, String[] strs) {
        int sz = strs.length;
        int ret = str.length();
        int tmp = 0;
        for(int i=0; i<sz; i++) {
            tmp = str.indexOf(strs[i]);
            if(tmp < ret) {
                ret = tmp;
            }
        }
        return (ret == str.length())?-1:ret;
    }

    /**
     * Find the latest index of any of a set of potential substrings.
     */
    static public int lastIndexOfAny(String str, String[] strs) {
        int sz = strs.length;
        int ret = -1;
        int tmp = 0;
        for(int i=0; i<sz; i++) {
            tmp = str.lastIndexOf(strs[i]);
            if(tmp > ret) {
                ret = tmp;
            }
        }
        return ret;
    }

    /**
     * Strip any of a supplied substring from the end of a String..
     */
    static public String stripEnd(String str, String ch) {
        int end = str.length();

        if(ch == null) {
            while( Character.isWhitespace( str.charAt(end-1) ) ) {
                end--;
            }
        } else {
            char chr = ch.charAt(0);
            while( str.charAt(end-1) == chr ) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /**
     * Strip any of a supplied substring from the start of a String..
     */
    static public String stripStart(String str, String ch) {
        int start = 0;

        if(ch == null) {
            while( Character.isWhitespace( str.charAt(start) ) ) {
                start++;
            }
        } else {
            char chr = ch.charAt(0);
            while( str.charAt(start) == chr ) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * Find the Levenshtein distance between two strings.
     * This is the number of changes needed to change one string into 
     * another. Where each change is a single character modification.
     *
     * This implemmentation of the levenshtein distance algorithm 
     * is from http://www.merriampark.com/ld.htm
     */
    static public int getLevenshteinDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1
        n = s.length ();
        m = t.length ();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n+1][m+1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt (i - 1);

            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt (j - 1);

                // Step 5
                if (s_i == t_j) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6
                d[i][j] = NumberW.minimum(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
            }
        }

        // Step 7
        return d[n][m];
    }



    /*
    // cciteseer.nj.nec.com/oliver93decision.html
    static public boolean isSimilarText(String st1, String str2) {
        return false;
    }
    */

    /**
     * Quote a string so that it may be used in a regular expression 
     * without any parts of the string being considered as a 
     * part of the regular expression's control characters.
     */
    static public String quoteRegularExpression(String str) {
        // replace ? + * / . ^ $ as long as they're not in character 
        // class. so must be done by hand
        char[] chrs = str.toCharArray();
        int sz = chrs.length;
        StringBuffer buffer = new StringBuffer(2*sz);
        for(int i=0; i<sz; i++) {
            switch(chrs[i]) {
              case '[' :
              case ']' :
              case '?' :
              case '+' :
              case '*' :
              case '/' :
              case '.' :
              case '^' :
              case '$' :
                buffer.append("\\"); //$NON-NLS-1$
              default : 
                buffer.append(chrs[i]);
            }
        }
        return buffer.toString();
    }

    /**
     * Capitalise all the words in a string. Uses Character.isWhitespace 
     * as a separator between words.
     */
    static public String capitaliseAllWords(String str) {
        int sz = str.length();
        StringBuffer buffer = new StringBuffer(sz);
        boolean space = true;
        for(int i=0; i<sz; i++) {
            char ch = str.charAt(i);
            if(Character.isWhitespace(ch)) {
                buffer.append(ch);
                space = true;
            } else
            if(space) {
                buffer.append(Character.toTitleCase(ch));
                space = false;
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    /**
     * Create a word-wrapped version of a String. Wrap at 80 characters and 
     * use newlines as the delimiter. If a word is over 80 characters long 
     * use a - sign to split it.
     */
    static public String wordWrap(String str) {
        return wordWrap(str, 80, "\n", "-"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    /**
     * Create a word-wrapped version of a String. Wrap at a specified width and 
     * use newlines as the delimiter. If a word is over the width in lenght 
     * use a - sign to split it.
     */
    static public String wordWrap(String str, int width) {
        return wordWrap(str, width, "\n", "-"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    static public String wordWrap(String str, String width, String delim, String split) {
        return wordWrap(str, NumberW.stringToInt(width), delim, split);
    }
    /**
     * Word-wrap a string.
     *
     * @param str   String to word-wrap
     * @param width int to wrap at
     * @param delim String to use to separate lines
     * @param split String to use to split a word greater than width long
     *
     * @return String that has been word wrapped
     */
    static public String wordWrap(String str, int width, String delim, String split) {
        int sz = str.length();

        /// shift width up one. mainly as it makes the logic easier
        width++;

        // our best guess as to an initial size
        StringBuffer buffer = new StringBuffer(sz/width*delim.length()+sz);

        // every line will include a delim on the end
        width = width - delim.length();

        int idx = -1;
        String substr = null;

        // beware: i is rolled-back inside the loop
        for(int i=0; i<sz; i+=width) {

            // on the last line
            if(i > sz - width) {
                buffer.append(str.substring(i));
//                System.err.print("LAST-LINE: "+str.substring(i));
                break;
            }

//            System.err.println("loop[i] is: "+i);
            // the current line
            substr = str.substring(i, i+width);

            // is the delim already on the line
            idx = substr.indexOf(delim);
            if(idx != -1) {
                buffer.append(substr.substring(0,idx));
//                System.err.println("Substr: '"+substr.substring(0,idx)+"'");
                buffer.append(delim);
                i -= width-idx-delim.length();
                
//                System.err.println("loop[i] is now: "+i);
//                System.err.println("found-whitespace: '"+substr.charAt(idx+1)+"'.");
                // Erase a space after a delim. Is this too obscure?
                if(substr.charAt(idx+1) != '\n') {
                    if(Character.isWhitespace(substr.charAt(idx+1))) {
                        i++;
                    }
                }
//                System.err.println("i -= "+width+"-"+idx);
                continue;
            }

            idx = -1;

            // figure out where the last space is
            char[] chrs = substr.toCharArray();
            for(int j=width; j>0; j--) {
                if(Character.isWhitespace(chrs[j-1])) {
                    idx = j;
//                    System.err.println("Found whitespace: "+idx);
                    break;
                }
            }

            // idx is the last whitespace on the line.
//            System.err.println("idx is "+idx);
            if(idx == -1) {
                for(int j=width; j>0; j--) {
                    if(chrs[j-1] == '-') {
                        idx = j;
//                        System.err.println("Found Dash: "+idx);
                        break;
                    }
                }
                if(idx == -1) {
                    buffer.append(substr);
                    buffer.append(delim);
//                    System.err.print(substr);
//                    System.err.print(delim);
                } else {
                    if(idx != width) {
                        idx++;
                    }
                    buffer.append(substr.substring(0,idx));
                    buffer.append(delim);
//                    System.err.print(substr.substring(0,idx));
//                    System.err.print(delim);
                    i -= width-idx;
                }
            } else {
                /*
                if(force) {
                    if(idx == width-1) {
                        buffer.append(substr);
                        buffer.append(delim);
                    } else {
                        // stick a split in.
                        int splitsz = split.length();
                        buffer.append(substr.substring(0,width-splitsz));
                        buffer.append(split);
                        buffer.append(delim);
                        i -= splitsz;
                    }
                } else {
                */
                    // insert spaces
                    buffer.append(substr.substring(0,idx));
                    buffer.append(repeat(" ",width-idx)); //$NON-NLS-1$
//                    System.err.print(substr.substring(0,idx));
//                    System.err.print(repeat(" ",width-idx));
                    buffer.append(delim);
//                    System.err.print(delim);
//                    System.err.println("i -= "+width+"-"+idx);
                    i -= width-idx;
//                }
            }
        }
//        System.err.println("\n*************");
        return buffer.toString();
    }


    /**
     * Get the String that is nested in between two instances of the 
     * same String.
     *
     * @param str   String containing nested-string
     * @param tag  String before and after nested-string
     *
     * @return String that was nested
     */
    static public String getNestedString(String str, String tag) {
        return getNestedString(str, tag, tag);
    }
    /**
     * Get the string that is nested in between two strings.
     *
     * @param str   String containing nested-string
     * @param open  String before nested-string
     * @param close String after nested-string
     *
     * @return String that was nested
     */
    static public String getNestedString(String str, String open, String close) {
        int start = str.indexOf(open);
        if(start != -1) {
            int end = str.indexOf(close, start+open.length());
            if(end != -1) {
                return str.substring(start+open.length(), end);
            }
        }
        return ""; //$NON-NLS-1$
    }


    /**
     * How mmany times is the substring in the larger string.
     */
    static public int countMatches(String str, String sub) {
        int count = 0;
        int idx = 0;
        while( (idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * Is a String a word. Contains only unicode letters.
     */
    static public boolean isWord(String str) {
        int sz = str.length();
        for(int i=0; i<sz; i++) {
            if(!Character.isLetter(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does a String contain only unicode letters or digits.
     */
    static public boolean isAlphanumeric(String str) {
        int sz = str.length();
        for(int i=0; i<sz; i++) {
            if(!Character.isLetterOrDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does a String contain only unicode digits.
     */
    static public boolean isNumeric(String str) {
        int sz = str.length();
        for(int i=0; i<sz; i++) {
            if(!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is a String a line, containing only letters, digits or 
     * whitespace, and ending with an optional newline.
     * NB: Punctuation not allowed.
     */
    static public boolean isLine(String str) {
        char ch = 0;
        char[] chrs = str.toCharArray();
        int sz = chrs.length-1;
        for(int i=0; i<sz-2; i++) {
            if(!Character.isLetterOrDigit(chrs[i])) {
                if(!Character.isWhitespace(chrs[i])) {
                    return false;
                }
            }
        }
        if(!Character.isLetterOrDigit(chrs[sz-1])) {
            if(!Character.isWhitespace(chrs[sz-1])) {
                if(chrs[sz-1] != '\r') {
                    return false;
                } else 
                if(chrs[sz] != '\n') {
                    return false;
                }
            }
        }
        if(!Character.isLetterOrDigit(chrs[sz])) {
            if(!Character.isWhitespace(chrs[sz])) {
                if(chrs[sz] != '\n') {
                    return false;
                }
            }
        }
        return true;
    }

    /*
    // needs to handle punctuation
    static public boolean isText(String str) {
        int sz = str.length();
        char ch = 0;
        for(int i=0; i<sz; i++) {
            ch = str.charAt(i);
            if(!Character.isLetterOrDigit(ch)) {
                if(!Character.isWhitespace(ch)) {
                    if( (ch != '\n') && (ch != '\r') ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    */

    /**
     * Return either the passed in String, or if it is null, 
     * then an empty String.
     */
    static public String defaultString(String str) {
        return defaultString(str,""); //$NON-NLS-1$
    }

    /**
     * Return either the passed in String, or if it is null, 
     * then a passed in default String.
     */
    static public String defaultString(String str, String def) {
        return (str == null)?def:str;
    }

    static public String upperCase(String str) {
        return str.toUpperCase();
    }

    static public String lowerCase(String str) {
        return str.toLowerCase();
    }

    static public String trim(String str) {
        return str.trim();
    }

    static public String substring(String str, String start) {
        return substring(str, NumberW.stringToInt(start));
    }
    static public String substring(String str, int start) {
        if(str == null) {
            return null;
        }

        // handle negatives
        if(start < 0) {
            start = str.length() + start;    // remember start is negative
        }

        if(start < 0) {
            start = 0;
        }

        return str.substring(start);
    }
    static public String substring(String str, String start, String end) {
        return substring(str, NumberW.stringToInt(start), NumberW.stringToInt(end));
    }
    static public String substring(String str, int start, int end) {
        if(str == null) {
            return null;
        }

        // handle negatives
        if(end < 0) {
            end = str.length() + end;    // remember end is negative
        }
        if(start < 0) {
            start = str.length() + start;    // remember start is negative
        }

        // check length next
        if(end > str.length()) {
            // check this works.
            end = str.length();
        }

        // what if start is greater than end??

        if(start < 0) {
            start = 0;
        }

        // a good default?
        if(end < 0) {
            end = 0;
        }

        return str.substring(start, end);

    }

    // random printable ascii
    static public String random(int count) {
        return random(count, false, false);
    }

    static public String randomAscii(int count) {
        return random(count, 32, 127, false, false);
    }
    static public String randomAlphabetic(int count) {
        return random(count, true, false);
    }
    static public String randomAlphanumeric(int count) {
        return random(count, true, true);
    }
    static public String randomNumeric(int count) {
        return random(count, false, true);
    }

    static public String random(int count, boolean letters, boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }
    static public String random(int count, int start, int end, boolean letters, boolean numbers) {
        return random(count, start, end, letters, numbers, null);
    }
    /**
     * Create a random string based on a variety of options.
     *
     * @param count int length of random string to create
     * @param start int position in set of chars to start at
     * @param end int position in set of chars to end before
     * @param letters boolean only allow letters?
     * @param numbers boolean only allow numbers?
     * @param set char[] set of chars to choose randoms from.
     *        If null, then it will use the set of all chars.
     *
     */
    static public String random(int count, int start, int end, boolean letters, boolean numbers, char[] set) {
        if( (start == 0) && (end == 0) ) {
            end = (int)'z';
            start = (int)' ';
            if(!letters && !numbers) {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }
        Random rnd = new Random();
        StringBuffer buffer = new StringBuffer();
        int gap = end - start;
        while(count-- != 0) {
            char ch;
            if(set == null) {
                ch = (char)(rnd.nextInt(gap) + start);
            } else {
                ch = set[rnd.nextInt(gap) + start];
            }
            if( (letters && numbers && Character.isLetterOrDigit(ch)) ||
                (letters && Character.isLetter(ch)) ||
                (numbers && Character.isDigit(ch)) ||
                (!letters && !numbers)
              ) 
            {
                buffer.append( ch );
            } else {
                count++;
            }
        }
        return buffer.toString();
    }
    static public String random(int count, String set) {
        return random(count, set.toCharArray());
    }
    static public String random(int count, char[] set) {
        return random(count,0,set.length-1,false,false,set);
    }

//    static public String reverseDottedName(String text) {
//        return reverseDelimitedString(text, ".");
//    }
//    static public String reverseDelimitedString(String text, String delimiter) {
//        // could implement manually, but simple way is to reuse other, 
//        // probably slower, methods.
//        String[] strs = explode(text, delimiter);
//
//        CollectionsW.reverseArray(strs);
//
//        return implode(strs, delimiter);
//    }

    /**
     * Interpolate variables into a String.
     */
    static public String interpolate(String text, Map map) {
        Iterator keys = map.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next().toString();
            String value = map.get(key).toString();
            text = replaceString(text, "${"+key+"}", value); //$NON-NLS-1$ //$NON-NLS-2$
            if(key.indexOf(" ") == -1) { //$NON-NLS-1$
                text = replaceString(text, "$"+key, value); //$NON-NLS-1$
            }
        }
        return text;
    }

}
