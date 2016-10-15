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
package com.nextep.designer.vcs.preferences;

/**
 * Defines static constants pointing to preference keys for versioning.
 * 
 * @author Christophe Fondacci
 */
public final class VersioningPreferenceConstants {

	private VersioningPreferenceConstants() {
	}

	public static String PREF_VCS_PROMPT_LIST_CHECKOUT_OFF = "com.neXtep.designer.vcs.container.listCheckouts.prompt.off"; //$NON-NLS-1$
	public static String DEFAULT_RELEASE_INCREMENT = "com.neXtep.designer.vcs.defaultReleaseIncrement"; //$NON-NLS-1$
	public static String VALIDATE_PREFIX = "com.neXtep.designer.vcs.validation."; //$NON-NLS-1$
	public static String RECENT_ACTIVITIES_PREFIX = "com.neXtep.designer.vcs.activities.recent."; //$NON-NLS-1$
}
