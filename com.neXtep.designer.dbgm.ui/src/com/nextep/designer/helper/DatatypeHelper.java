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
package com.nextep.designer.helper;

import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.designer.core.preferences.DesignerCoreConstants;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 */
public class DatatypeHelper {

	public static Image getDatatypeIcon(IDatatype d) {
		return getDatatypeIcon(d,
				Designer.getInstance().getPropertyBool(DesignerCoreConstants.ICON_TINY));
	}

	public static Image getDatatypeIcon(IDatatype d, boolean tiny) {
		if (d == null) {
			return ImageFactory.ICON_BLANK;
		} else if (d.getName().startsWith("VARCHAR") || d.getName().startsWith("CLOB")
				|| d.getName().startsWith("CHAR")) {
			if (!tiny)
				return DBGMImages.ICON_ALPHATYPE;
			else
				return DBGMImages.ICON_ALPHATYPE_TINY;
		} else if (d.getName().startsWith("NUM") || d.getName().startsWith("INT")) {
			if (!tiny)
				return DBGMImages.ICON_NUMTYPE;
			else
				return DBGMImages.ICON_NUMTYPE_TINY;
		} else if (d.getName().startsWith("DATE") || d.getName().startsWith("TIME")
				|| d.getName().startsWith("CALEND")) {
			if (!tiny)
				return DBGMImages.ICON_DATETYPE;
			else
				return DBGMImages.ICON_DATETYPE_TINY;
		} else {
			if (!tiny)
				return DBGMImages.ICON_COLUMN_TYPE;
			else
				return DBGMImages.ICON_COLUMN_TYPE_TINY;
		}
	}

	/**
	 * @deprecated Use {@link DBGMHelper#getDatatypeLabel(IDatatype)} instead
	 */
	public static String getDatatypeLabel(IDatatype d) {
		return DBGMHelper.getDatatypeLabel(d);
	}
}
