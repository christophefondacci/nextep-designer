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

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;
import com.nextep.datadesigner.exception.ErrorException;

/**
 * Makes writing XML much much easier.
 * 
 * @author <a href="mailto:bayard@generationjava.com">Henri Yandell</a>
 * @version 0.1
 */
public class XmlWriter {

    private Writer writer;      // underlying writer
    private Stack<String> stack;        // of xml entity names
    /** Depth of opened entities */
    private int entityDepth = 0;
    private StringBuffer attrs; // current attribute string
    private boolean empty;      // is the current node empty
    private boolean closed;     // is the current node closed...

    /**
     * Create an XmlWriter on top of an existing java.io.Writer.
     */
    public XmlWriter(Writer writer) {
        this.writer = writer;
        this.closed = true;
        this.stack = new Stack<String>();
        try {
        	writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
        } catch( IOException e ) {
        	throw new ErrorException(e);
        }
    }

    /**
     * Begin to output an entity. 
     *
     * @param String name of entity.
     */
    public XmlWriter writeEntity(String name) throws WritingException {
        try {
            closeOpeningTag();
            this.closed = false;
            writer.write("\r\n"); //$NON-NLS-1$
            // Indentation
            writeEntityTabs();
            this.writer.write("<"); //$NON-NLS-1$
            this.writer.write(name);
            stack.add(name);
            this.empty = true;
            entityDepth++;
            return this;
        } catch (IOException ioe) {
            throw new XmlWritingException(ioe);
        }
    }
    private void writeEntityTabs() throws IOException {
        for(int i=0 ; i< entityDepth ; i++) {
        	this.writer.write('\t');
        }
    }
    // close off the opening tag
    private void closeOpeningTag() throws IOException {
        if (!this.closed) {
            writeAttributes();
            this.closed = true;
            this.writer.write(">"); //$NON-NLS-1$
        }
    }

    // write out all current attributes
    private void writeAttributes() throws IOException {
        if (this.attrs != null) {
            this.writer.write(this.attrs.toString());
            this.attrs.setLength(0);
            this.empty = false;
        }
    }

    /**
     * Write an attribute out for the current entity. 
     * Any xml characters in the value are escaped.
     * Currently it does not actually throw the exception, but 
     * the api is set that way for future changes.
     *
     * @param String name of attribute.
     * @param String value of attribute.
     */
    public XmlWriter writeAttribute(String attr, String value) throws WritingException {

        // maintain api
        if (false) throw new XmlWritingException();

        if (this.attrs == null) {
            this.attrs = new StringBuffer();
        }
        this.attrs.append(" "); //$NON-NLS-1$
        this.attrs.append(attr);
        this.attrs.append("=\""); //$NON-NLS-1$
        this.attrs.append(XmlW.escapeXml(value));
        this.attrs.append("\""); //$NON-NLS-1$
        return this;
    }

    /**
     * End the current entity. This will throw an exception 
     * if it is called when there is not a currently open 
     * entity.
     */
    public XmlWriter endEntity() throws WritingException {
        try {
            if(this.stack.empty()) {
                throw new XmlWritingException("Called endEntity too many times. "); //$NON-NLS-1$
            }
            String name = stack.pop();
            // Handling depth decrease
            entityDepth--;
            if (name != null) {
                if (this.empty) {
                    writeAttributes();
                    this.writer.write("/>"); //$NON-NLS-1$
                } else {
                	writer.write("\r\n"); //$NON-NLS-1$
                	writeEntityTabs();
                    this.writer.write("</"); //$NON-NLS-1$
                    this.writer.write(name);
                    this.writer.write(">"); //$NON-NLS-1$
                }
                this.empty = false;
                this.closed = true;
            }
            return this;
        } catch (IOException ioe) {
            throw new XmlWritingException(ioe);
        }
    }

    /**
     * Close this writer. It does not close the underlying 
     * writer, but does throw an exception if there are 
     * as yet unclosed tags.
     */
    public void close() throws WritingException {
        if(!this.stack.empty()) {
            throw new XmlWritingException("Tags are not all closed. "+ //$NON-NLS-1$
                "Possibly, "+stack.pop()+" is unclosed. "); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(String text) throws WritingException {
        try {
            closeOpeningTag();
            this.empty = false;
            this.writer.write(XmlW.escapeXml(text));
            return this;
        } catch (IOException ioe) {
            throw new XmlWritingException(ioe);
        }
    }

    // Static functions lifted from generationjava helper classes
    // to make the jar smaller.
    
    // from XmlW
    /*
    static public String escapeXml(String str) {
        str = replaceString(str,"&","&amp;");
        str = replaceString(str,"<","&lt;");
        str = replaceString(str,">","&gt;");
        str = replaceString(str,"\"","&quot;");
        str = replaceString(str,"'","&apos;");
        return str;
    }  
    */

    // from StringW
    /*
    static public String replaceString(String text, String repl, String with) {
        return replaceString(text, repl, with, -1);
    }  
    */
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
     /*
    static public String replaceString(String text, String repl, String with, int max) {
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
    */

    // Two example methods. They should output the same XML:
    // <person name="fred" age="12"><phone>425343</phone><bob/></person>
    static public void main(String[] args) throws WritingException {
        test1();
        test2();
    }
    static public void test1() throws WritingException {
        Writer writer = new java.io.StringWriter();
        XmlWriter xmlwriter = new XmlWriter(writer);
        xmlwriter.writeEntity("person").writeAttribute("name", "fred").writeAttribute("age", "12").writeEntity("phone").writeText("4254343").endEntity().writeEntity("bob").endEntity().endEntity(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        xmlwriter.close();
        System.err.println(writer.toString());
    }
    static public void test2() throws WritingException {
        Writer writer = new java.io.StringWriter();
        XmlWriter xmlwriter = new XmlWriter(writer);
        xmlwriter.writeEntity("person"); //$NON-NLS-1$
        xmlwriter.writeAttribute("name", "fred"); //$NON-NLS-1$ //$NON-NLS-2$
        xmlwriter.writeAttribute("age", "12"); //$NON-NLS-1$ //$NON-NLS-2$
        xmlwriter.writeEntity("phone"); //$NON-NLS-1$
        xmlwriter.writeText("4254343"); //$NON-NLS-1$
        xmlwriter.endEntity();
        xmlwriter.writeEntity("bob"); //$NON-NLS-1$
        xmlwriter.endEntity();
        xmlwriter.endEntity();
        xmlwriter.close();
        System.err.println(writer.toString());
    }

}
