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
/**
 *
 */
package com.nextep.designer.dbgm.oracle.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;



/**
 * @author Christophe Fondacci
 *
 */
public class DBOMImages {
	private static final Log log = LogFactory.getLog(DBOMImages.class);
	public static final Image ICON_ADD_PARTITION 		= Activator.getImageDescriptor("/resources/NewPartitionTiny.ico").createImage();
	public static final Image ICON_DEL_PARTITION 		= Activator.getImageDescriptor("/resources/DelPartitionTiny.ico").createImage();
	public static final Image ICON_TAB_PARTITION 		= Activator.getImageDescriptor("/resources/TablePartitionSmall.ico").createImage();
	public static final Image ICON_TAB_PARTITION_TINY	= Activator.getImageDescriptor("/resources/TablePartitionTiny.ico").createImage();
	public static final Image ICON_IDX_PARTITION 		= Activator.getImageDescriptor("/resources/IndexPartitionSmall.ico").createImage();
	public static final Image ICON_IDX_PARTITION_TINY	= Activator.getImageDescriptor("/resources/IndexPartitionTiny.ico").createImage();
	public static final Image ICON_ADD_CLUSTER_TABLE	= Activator.getImageDescriptor("/resources/AddClusteredTableTiny.ico").createImage();
	public static final Image ICON_DEL_CLUSTER_TABLE	= Activator.getImageDescriptor("/resources/DelClusteredTableTiny.ico").createImage();

	public static void dispose() {
		log.debug("Disposing DBOM Oracle image resources...");
		ICON_ADD_PARTITION.dispose();
		ICON_DEL_PARTITION.dispose();
		ICON_TAB_PARTITION.dispose();
		ICON_TAB_PARTITION_TINY.dispose();
		ICON_IDX_PARTITION.dispose();
		ICON_IDX_PARTITION_TINY.dispose();
		ICON_ADD_CLUSTER_TABLE.dispose();
		ICON_DEL_CLUSTER_TABLE.dispose();
	}
}
