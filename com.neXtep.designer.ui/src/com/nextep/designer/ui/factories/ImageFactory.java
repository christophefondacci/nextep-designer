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
package com.nextep.designer.ui.factories;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.ui.CoreUiPlugin;

/**
 * This class provides all images / icon needed by the nextep designer application.
 * 
 * @author Christophe Fondacci
 */
public class ImageFactory {

	private static final Log log = LogFactory.getLog(ImageFactory.class);
	public static final int IMAGE_WIDTH = 26;
	public static final int IMAGE_HEIGHT = 26;

	public static final Image ICON_BLANK = getImageDescriptor("/resource/blank.ico").createImage();
	public static final Image ICON_DELETE = getImageDescriptor("/resource/delete.png")
			.createImage();
	public static final Image ICON_CONSOLE = getImageDescriptor("/resource/ConsoleIcon.bmp")
			.createImage();
	public static Image ICON_LOCK = getImageDescriptor("/resource/lock.ico").createImage();
	public static final Image ICON_LOCK_TINY = getImageDescriptor("/resource/LockTiny.ico")
			.createImage();
	public static Image ICON_USER_LOCK = getImageDescriptor("/resource/userLock.ico").createImage();
	public static final Image ICON_USER_LOCK_TINY = getImageDescriptor("/resource/userLockTiny.ico")
			.createImage();
	public static final Image ICON_SYNCH_TINY = getImageDescriptor("/resource/SynchTiny.ico")
			.createImage();
	public static final Image ICON_SYNCH = getImageDescriptor("/resource/SynchSmall.ico")
			.createImage();
	public static final Image ICON_UNSYNCH_TINY = getImageDescriptor("/resource/UnsynchTiny.ico")
			.createImage();
	public static final Image ICON_UNSYNCH = getImageDescriptor("/resource/UnsynchSmall.ico")
			.createImage();
	public static final Image ICON_UP = getImageDescriptor("/resource/UpSmall.ico").createImage();
	public static final Image ICON_UP_TINY = getImageDescriptor("/resource/up.png").createImage();
	public static final Image ICON_DOWN = getImageDescriptor("/resource/DownSmall.ico")
			.createImage();
	public static final Image ICON_DOWN_TINY = getImageDescriptor("/resource/down.png")
			.createImage();
	public static final Image ICON_RIGHT = getImageDescriptor("/resource/RightSmall.ico")
			.createImage();
	public static final Image ICON_RIGHT_TINY = getImageDescriptor("/resource/RightTiny.ico")
			.createImage();
	public static final Image ICON_LEFT = getImageDescriptor("/resource/LeftSmall.ico")
			.createImage();
	public static final Image ICON_LEFT_TINY = getImageDescriptor("/resource/LeftTiny.ico")
			.createImage();
	public static final Image ICON_ERROR = getImageDescriptor("/resource/ErrorIconSmall.ico")
			.createImage();
	public static final Image ICON_ATTRIBUTE = getImageDescriptor("/resource/AttribIconSmall.ico")
			.createImage();
	public static final Image ICON_ATTRIBUTE_TINY = getImageDescriptor(
			"/resource/AttribIconTiny.ico").createImage();
	public static final Image ICON_EDIT_TINY = getImageDescriptor("/resource/EditIconTiny.ico")
			.createImage();
	public static final String RESOURCE_ERROR = "/resource/ErrorTiny.ico"; //$NON-NLS-1$
	public static final Image ICON_ERROR_TINY = getImageDescriptor(RESOURCE_ERROR).createImage();
	public static final Image ICON_ADD_TINY = getImageDescriptor("/resource/plus.png")
			.createImage();
	public static final Image ICON_DESIGNER_TINY = getImageDescriptor(
			"/resource/DesignerIconTiny.ico").createImage();
	public static final Image ICON_ERROR_DECO_TINY = getImageDescriptor(
			"/resource/ErrorDecoratorTiny.ico").createImage();
	public static final Image ICON_WARNING_DECO_TINY = getImageDescriptor(
			"/resource/warning-decoration.png").createImage(); //$NON-NLS-1$

	public static final String RESOURCE_LOCK_LARGE = "/resource/lock-32.png"; //$NON-NLS-1$
	public static final Image LOCK_LARGE = getImageDescriptor(RESOURCE_LOCK_LARGE).createImage();

	private static Map<IResourceLocator, Image> imagesToDispose = new HashMap<IResourceLocator, Image>();

	public static void setSmall() {
		// ICON_VIEW = Activator.getImageDescriptor("/resource/ViewIconTiny.ico").createImage();
		// ICON_LOCK = Activator.getImageDescriptor("/resource/LockTiny.ico").createImage();
	}

	public static void dispose() {
		log.debug("Disposing core resources...");
		ICON_BLANK.dispose();
		ICON_DELETE.dispose();
		ICON_CONSOLE.dispose();
		ICON_LOCK.dispose();
		ICON_LOCK_TINY.dispose();
		ICON_USER_LOCK.dispose();
		ICON_USER_LOCK_TINY.dispose();
		ICON_SYNCH_TINY.dispose();
		ICON_SYNCH.dispose();
		ICON_UNSYNCH.dispose();
		ICON_UNSYNCH_TINY.dispose();

		ICON_UP.dispose();
		ICON_DOWN.dispose();
		ICON_UP_TINY.dispose();
		ICON_DOWN_TINY.dispose();
		ICON_RIGHT.dispose();
		ICON_LEFT.dispose();
		ICON_RIGHT_TINY.dispose();
		ICON_LEFT_TINY.dispose();

		ICON_ERROR.dispose();
		ICON_ATTRIBUTE.dispose();
		ICON_ATTRIBUTE_TINY.dispose();
		ICON_EDIT_TINY.dispose();

		ICON_ADD_TINY.dispose();
		ICON_DESIGNER_TINY.dispose();
		ICON_ERROR_TINY.dispose();
		ICON_ERROR_DECO_TINY.dispose();
		ICON_WARNING_DECO_TINY.dispose();
		LOCK_LARGE.dispose();

		for (Image img : imagesToDispose.values()) {
			if (img != null) {
				img.dispose();
			}
		}
	}

	/**
	 * TODO: Images need to be located in a UI plugin
	 * 
	 * @param file file to get descriptor for
	 * @return a {@link ImageDescriptor}
	 */
	public static ImageDescriptor getImageDescriptor(String file) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(CoreUiPlugin.PLUGIN_ID, file);
	}

	/**
	 * TODO: Images need to be located in a UI plugin
	 * 
	 * @param file file to get descriptor for
	 * @return a {@link ImageDescriptor}
	 */
	public static ImageDescriptor getImageDescriptor(String plugin, String file) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(plugin, file);
	}

	public static ImageDescriptor getImageDescriptor(IResourceLocator imageLocator) {
		if (imageLocator == null) {
			return null;
		} else {
			return AbstractUIPlugin.imageDescriptorFromPlugin(imageLocator.getPluginId(),
					imageLocator.getFile());
		}
	}

	public static Image getImage(IResourceLocator imageLocator) {
		Image img = imagesToDispose.get(imageLocator);
		if (img == null) {
			final ImageDescriptor descriptor = getImageDescriptor(imageLocator);
			if (descriptor != null) {
				img = descriptor.createImage();
			}
			imagesToDispose.put(imageLocator, img);
		}
		return img;
	}
}
