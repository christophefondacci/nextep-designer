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

/**
 * @author Christophe Fondacci
 */
final public class XmlW {

    static public String escapeXml(String str) {
        str = StringW.replaceString(str,"&","&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"<","&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,">","&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"\"","&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"'","&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
        return str;
    }

    static public String unescapeXml(String str) {
        str = StringW.replaceString(str,"&amp;","&"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"&lt;","<"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"&gt;",">"); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"&quot;","\""); //$NON-NLS-1$ //$NON-NLS-2$
        str = StringW.replaceString(str,"&apos;","'"); //$NON-NLS-1$ //$NON-NLS-2$
        return str;
    }

    /**
     * Remove any xml tags from a String.
     * Same as HtmlW's method.
     */
    static public String removeXml(String str) {
        int sz = str.length();
        StringBuffer buffer = new StringBuffer(sz);
        boolean inString = false;
        boolean inTag = false;
        for(int i=0; i<sz; i++) {
            char ch = str.charAt(i);
            if(ch == '<') {
                inTag = true;
            } else
            if(ch == '>') {
                inTag = false;
                continue;
            }
            if(!inTag) {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    static public String getContent(String tag, String text) {
        int idx = XmlW.getIndexOpeningTag(tag, text);
        if(idx == -1) {
            return ""; //$NON-NLS-1$
        }
        text = text.substring(idx);
        int end = XmlW.getIndexClosingTag(tag, text);
        idx = text.indexOf('>');
        if(idx == -1) {
            return ""; //$NON-NLS-1$
        }
        return text.substring(idx+1, end);
    }

    static public int getIndexOpeningTag(String tag, String text) {
        return getIndexOpeningTag(tag, text, 0);
    }
    static private int getIndexOpeningTag(String tag, String text, int start) {
        // consider whitespace?
        int idx = text.indexOf("<"+tag, start); //$NON-NLS-1$
        if(idx == -1) {
            return -1;
        }
        char next = text.charAt(idx+1+tag.length());
        if( (next == '>') || Character.isWhitespace(next) ) {
            return idx;
        } else {
            return getIndexOpeningTag(tag, text, idx+1);
        }
    }

    // Pass in "para" and a string that starts with 
    // <para> and it will return the index of the matching </para>
    // It assumes well-formed xml. Or well enough.
    static public int getIndexClosingTag(String tag, String text) {
        return getIndexClosingTag(tag, text, 0);
    }
    static public int getIndexClosingTag(String tag, String text, int start) {
        String open = "<"+tag; //$NON-NLS-1$
        String close = "</"+tag+">"; //$NON-NLS-1$ //$NON-NLS-2$
//        System.err.println("OPEN: "+open);
//        System.err.println("CLOSE: "+close);
        int closeSz = close.length();
        int nextCloseIdx = text.indexOf(close, start);
//        System.err.println("first close: "+nextCloseIdx);
        if(nextCloseIdx == -1) {
            return -1;
        }
        int count = StringW.countMatches(text.substring(start, nextCloseIdx), open);
//        System.err.println("count: "+count);
        if(count == 0) {
            return -1;  // tag is never opened
        }
        int expected = 1;
        while(count != expected) {
            nextCloseIdx = text.indexOf(close, nextCloseIdx+closeSz);
            if(nextCloseIdx == -1) {
                return -1;
            }
            count = StringW.countMatches(text.substring(start, nextCloseIdx), open);
            expected++;
        }
        return nextCloseIdx;
    }

    static public String getAttribute(String attribute, String text) {
        return getAttribute(attribute, text, 0);
    }
    static public String getAttribute(String attribute, String text, int idx) {
         int close = text.indexOf(">", idx); //$NON-NLS-1$
         int attrIdx = text.indexOf(attribute+"=\"", idx); //$NON-NLS-1$
         if(attrIdx == -1) {
             return null;
         }
         if(attrIdx > close) {
             return null;
         }
         int attrStartIdx = attrIdx + attribute.length() + 2;
         int attrCloseIdx = text.indexOf("\"", attrStartIdx); //$NON-NLS-1$
         if(attrCloseIdx > close) {
             return null;
         }
         return unescapeXml(text.substring(attrStartIdx, attrCloseIdx));
    }

}
