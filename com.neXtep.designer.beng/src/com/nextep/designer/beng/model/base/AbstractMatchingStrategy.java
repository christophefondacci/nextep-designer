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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.beng.model.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.model.IMatchingStrategy;

/**
 * A base class for {@link IMatchingStrategy} implementation providing basic release extraction
 * feature.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractMatchingStrategy implements IMatchingStrategy {

	protected long computeVersionBound(String versionPattern, boolean increment) {
		if (versionPattern == null) {
			return Long.MAX_VALUE;
		}
		// Computing upper version bound from pattern
		final Pattern p = Pattern.compile("([0-9]+)(\\.([0-9]+)(\\.([0-9]+)(\\.([0-9]+))?)?)?"); //$NON-NLS-1$
		final Matcher m = p.matcher(versionPattern);
		Integer major = null, minor = null, iter = null, patch = null;
		while (m.find()) {
			// Extracting release components
			final String majorStr = m.group(1);
			final String minorStr = m.group(3);
			final String iterStr = m.group(5);
			final String patchStr = m.group(7);
			// Converting to numeric
			major = majorStr == null ? null : Integer.valueOf(majorStr);
			minor = minorStr == null ? null : Integer.valueOf(minorStr);
			iter = iterStr == null ? null : Integer.valueOf(iterStr);
			patch = patchStr == null ? null : Integer.valueOf(patchStr);
			// Incrementing release bound if the flag asks so
			if (increment) {
				if (minor == null) {
					major++;
				} else if (iter == null) {
					minor++;
				} else if (patch == null) {
					iter++;
				} else {
					patch++;
				}
			}
		}
		// Building release number
		return VersionHelper.computeVersion(major, minor == null ? 0 : minor, iter == null ? 0
				: iter, patch == null ? 0 : patch, 0);
	}

}
