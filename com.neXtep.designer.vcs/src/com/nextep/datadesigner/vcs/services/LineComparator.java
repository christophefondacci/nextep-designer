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
package com.nextep.datadesigner.vcs.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.eclipse.compare.rangedifferencer.IRangeComparator;

public class LineComparator implements IRangeComparator {

	   private String[] fLines;

	    public LineComparator(String lines) throws IOException {
	        
	        BufferedReader br = new BufferedReader(new StringReader(lines == null ? "" : lines));
	        String line;
	        ArrayList<String> ar = new ArrayList<String>();
	        while ((line = br.readLine()) != null) {
	            ar.add(line);
	        }
	        // It is the responsibility of the caller to close the stream
	        fLines = (String[]) ar.toArray(new String[ar.size()]);
	    }

	    String getLine(int ix) {
	        return fLines[ix];
	    }

	    /* (non-Javadoc)
	     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
	     */
	    public int getRangeCount() {
	        return fLines.length;
	    }

	    /* (non-Javadoc)
	     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int, org.eclipse.compare.rangedifferencer.IRangeComparator, int)
	     */
	    public boolean rangesEqual(int thisIndex, IRangeComparator other,
	            int otherIndex) {
	        String s1 = fLines[thisIndex];
	        String s2 = ((LineComparator) other).fLines[otherIndex];
	        return s1.equals(s2);
	    }

	    /* (non-Javadoc)
	     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int, int, org.eclipse.compare.rangedifferencer.IRangeComparator)
	     */
	    public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
	        return false;
	    }

}
