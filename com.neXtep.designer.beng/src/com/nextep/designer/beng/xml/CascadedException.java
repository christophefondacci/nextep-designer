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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An Exception which is being thrown on top of another Throwable. That is, some code has caught an
 * Exception and wishes to throw a different Exception upwards as a result. This class allows the
 * original Exception to still be accessible.
 * 
 * @author Christophe Fondacci
 */
public class CascadedException extends Exception {

    private Throwable wrappedThrowable;

    public CascadedException(String s) {
        super(s);
    }

    public CascadedException() {
        super();
    }
    
    public CascadedException(String s, Throwable t) {
        super(s);
        setWrappedThrowable(t);
    }
    public CascadedException(Throwable t) {
        super();
        setWrappedThrowable(t);
    }
    
    /**
     * Access the original exception.
     *
     * @return Throwable that was initially throw
     */
    public Throwable getWrappedThrowable() {
        return wrappedThrowable;
    }
    
    /**
     * Reset the original exception.
     *
     * @param t Throwable to be wrapped
     */
    public void setWrappedThrowable(Throwable t) {
        wrappedThrowable = t;   
    }

    public void printStackTrace() {
        super.printStackTrace();
        if(wrappedThrowable != null) {
            System.err.println("Cascaded Exception: "); // bad //$NON-NLS-1$
            wrappedThrowable.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if(wrappedThrowable != null) {
            ps.println("Cascaded Exception: "); //$NON-NLS-1$
            wrappedThrowable.printStackTrace(ps);
        }
    }

    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if(wrappedThrowable != null) {
            pw.println("Cascaded Exception: "); //$NON-NLS-1$
            wrappedThrowable.printStackTrace(pw);
        }
    }

}