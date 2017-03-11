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
package com.nextep.designer.synch.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class SynchUIImages {

	public static Image NOT_IN_TARGET;
	public static Image TO_REPO;
	public static Image DIFF;
	public static Image NOT_IN_SOURCE;
	public static Image UNCHECKED;
	public static Image CHECKED;
	public static Image CHECK_GRAYED;

	public static Image NOT_IN_TARGET_DIS;
	public static Image DIFF_DIS;
	public static Image NOT_IN_SOURCE_DIS;
	public static Image SELECT_ALL;
	public static Image UNSELECT_ALL;

	public static void dispose() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				NOT_IN_TARGET.dispose();
				TO_REPO.dispose();
				DIFF.dispose();
				NOT_IN_SOURCE.dispose();
				UNCHECKED.dispose();
				CHECKED.dispose();
				CHECK_GRAYED.dispose();

				NOT_IN_TARGET_DIS.dispose();
				DIFF_DIS.dispose();
				NOT_IN_SOURCE_DIS.dispose();
				SELECT_ALL.dispose();
				UNSELECT_ALL.dispose();
			}
		});
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return SynchUIPlugin.imageDescriptorFromPlugin(SynchUIPlugin.PLUGIN_ID, path);
	}

	public static void loadImages() {
		Display.getCurrent().syncExec(new Runnable() {
			@Override
			public void run() {
				NOT_IN_TARGET = getImageDescriptor("/resource/notInTarget.png").createImage();
				TO_REPO = getImageDescriptor("/resource/toRepoTiny.gif").createImage();
				DIFF = getImageDescriptor("/resource/differ.png").createImage();
				NOT_IN_SOURCE = getImageDescriptor("/resource/notInSource.png").createImage();
				UNCHECKED = getImageDescriptor("/resource/task-inactive.gif").createImage();
				CHECKED = getImageDescriptor("/resource/task-active.gif").createImage();
				CHECK_GRAYED = getImageDescriptor("/resource/task-gray.gif").createImage();

				NOT_IN_TARGET_DIS = getImageDescriptor("/resource/notInTarget_dis.png").createImage();
				DIFF_DIS = getImageDescriptor("/resource/differ_dis.png").createImage();
				NOT_IN_SOURCE_DIS = getImageDescriptor("/resource/notInSource_dis.png").createImage();
				SELECT_ALL = getImageDescriptor("/resource/selectAll.png").createImage();
				UNSELECT_ALL = getImageDescriptor("/resource/unselectAll.png").createImage();
			}
		});
	}
}
