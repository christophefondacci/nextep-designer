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
package com.nextep.datadesigner.gui.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import com.nextep.designer.ui.CoreUiPlugin;

/**
 * Image registry that keeps its images on the local file system.
 *
 * @since 1.0.3
 */
public class ImageService {

	private static final String IMAGE_DIR= "core-ui-images"; //$NON-NLS-1$

	private Map<ImageDescriptor,URL> fURLMap;
	private final File fTempDir;
	private int fImageCount;
	private static final Log log = LogFactory.getLog(ImageService.class);
	private static ImageService instance = null;
	
	public static final ImageService getInstance() {
		if(instance==null) {
			instance = new ImageService();
		}
		return instance;
	}
	
	private ImageService() {
		fURLMap= new HashMap<ImageDescriptor, URL>();
		fTempDir= getTempDir();
		fImageCount= 0;
	}

	private File getTempDir() {
		try {
			File imageDir= CoreUiPlugin.getDefault().getStateLocation().append(IMAGE_DIR).toFile();
			if (imageDir.exists()) {
				// has not been deleted on previous shutdown
				delete(imageDir);
			}
			if (!imageDir.exists()) {
				imageDir.mkdir();
			}
			if (!imageDir.isDirectory()) {
				log.error("Failed to create image directory " + imageDir.toString()); //$NON-NLS-1$
				return null;
			}
			return imageDir;
		} catch (IllegalStateException e) {
			// no state location
			return null;
		}
	}

	private void delete(File file) {
		if (file.isDirectory()) {
			File[] listFiles= file.listFiles();
			for (int i= 0; i < listFiles.length; i++) {
				delete(listFiles[i]);
			}
		}
		file.delete();
	}

	public URL getImageURL(Image img) {
		ImageDescriptor descriptor= ImageDescriptor.createFromImage(img);
		if (descriptor == null)
			return null;
		return getImageURL(descriptor);
	}

	public URL getImageURL(ImageDescriptor descriptor) {
		if (fTempDir == null)
			return null;

		URL url= (URL) fURLMap.get(descriptor);
		if (url != null)
			return url;

		File imageFile= getNewFile();
		ImageData imageData= descriptor.getImageData();
		if (imageData == null) {
			return null;
		}

		ImageLoader loader= new ImageLoader();
		loader.data= new ImageData[] { imageData };
		loader.save(imageFile.getAbsolutePath(), SWT.IMAGE_PNG);

		try {
			url= imageFile.toURI().toURL();
			fURLMap.put(descriptor, url);
			return url;
		} catch (MalformedURLException e) {
			log.error("Incorrect external image URL",e);
		}
		return null;
	}

	private File getNewFile() {
		File file;
		do {
			file= new File(fTempDir, String.valueOf(getImageCount()) + ".png"); //$NON-NLS-1$
		} while (file.exists());
		return file;
	}

	private synchronized int getImageCount() {
		return fImageCount++;
	}

	public void dispose() {
		if (fTempDir != null) {
			delete(fTempDir);
		}
		fURLMap= null;
	}
}
