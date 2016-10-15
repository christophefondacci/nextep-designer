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
package com.nextep.designer.dbgm.mysql.impl;

import com.nextep.designer.dbgm.mergers.TriggerMerger;
import com.nextep.designer.vcs.model.ComparisonScope;

public class MySQLTriggerMerger extends TriggerMerger {

	@Override
	protected String cleanSourceCode(String originalSource) {
		// Only processing non repository scopes
		if(getMergeStrategy().getComparisonScope()!=ComparisonScope.REPOSITORY) {
			String s = originalSource.replace("\r", "");
			s = s.replaceAll("-- (.)*\n","\n");
			
			// Removing multiline comments (regexp generate stack overflow
			int index = s.indexOf("/*");
			while(index!=-1) {
				int end = s.indexOf("*/",index+2);
				s = s.substring(0,index) + ((end == -1) ? "" : s.substring(end+2));
				
				index = s.indexOf("/*");
			}
			
	//		s = s.replaceAll("/\\*(.|\n)*\\*/", "");
			s = s.replaceAll("\n\\s+\n", "\n");
			s = s.replaceAll("(\\s)+\n","\n");
			s = s.replaceAll("\n\n", "\n");
			s = s.trim();
			if(s.charAt(s.length()-1) == ';') {
				s = s.substring(0,s.length()-1);
			}
			return s;
		} else {
			return originalSource;
		}
	}
}
