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
package com.nextep.datadesigner.model;

/**
 * @author Christophe Fondacci
 */
public enum IFormatter {

	UPPERCASE {

		public String format(String i) {
			if (i == null)
				return ""; //$NON-NLS-1$
			return i.trim().toUpperCase().replace(' ', '_');
		}
	},
	NOFORMAT {

		public String format(String i) {
			return i == null ? "" : i.trim(); //$NON-NLS-1$
		}
	},
	LOWERCASE {

		public String format(String i) {
			if (i == null)
				return ""; //$NON-NLS-1$
			return i.trim().toLowerCase().replace(' ', '_');
		}
	},
	PROPPER_LOWER {

		public String format(String i) {
			if (i == null || "".equals(i.trim())) //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			String str = i.trim();
			String result = ""; //$NON-NLS-1$
			while (str.indexOf(" ") > 0) { //$NON-NLS-1$
				if (!"".equals(result)) { //$NON-NLS-1$
					result += " "; //$NON-NLS-1$
				}
				result += wordIndent(str.substring(0, str.indexOf(" "))); //$NON-NLS-1$
				str = str.substring(str.indexOf(" ") + 1); //$NON-NLS-1$
			}
			if (!"".equals(result)) { //$NON-NLS-1$
				result += " "; //$NON-NLS-1$
			}
			result += wordIndent(str);
			return result;
		}

		private String wordIndent(String word) {
			return word.substring(0, 1).toUpperCase()
					+ (word.length() > 1 ? word.substring(1).toLowerCase() : ""); //$NON-NLS-1$
		}
	},
	UPPERSTRICT {

		public String format(String i) {
			if (i == null)
				return ""; //$NON-NLS-1$
			return i.trim().toUpperCase();
		}
	},
	UPPER_LEADING {

		@Override
		public String format(String i) {
			if (i == null) {
				return null;
			} else if (i.length() == 1) {
				return i.toUpperCase();
			} else {
				return i.substring(0, 1).toUpperCase() + i.substring(1).toLowerCase();
			}
		}
	};

	public abstract String format(String i);

}
