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

import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * A view linker is an element which can contribute
 * to a view which is being loaded.
 * The neXtep framework will invoke all defined linkers
 * immediately after loading a view from the repository
 * and before releasing the view to the IDE.
 * Contributors can load external data into the view,
 * or simply link view items together (for dependencies
 * using soft reference (IReference) which need to
 * be materialized).<br>
 * For example : <br>
 * An index is a standalone versionable object which has
 * a soft reference to a table. When a view is loaded from
 * repository, the index is initialized with the table
 * reference and the table is loaded without any index.<br>
 * A view linker will be triggered after the view has been
 * loaded to link the index with the corresponding table.
 *
 * @author Christophe Fondacci
 *
 */
public interface IViewLinker {

	public static final String LINKER_EXTENSION_POINT_ID = "com.neXtep.designer.vcs.viewLinker";
	/**
	 * Performs links on the specified view.
	 *
	 * @param view the version view to link
	 */
	public abstract void link(IWorkspace view);
	/**
	 * A label which will be displayed in the console
	 * while linking this view.
	 *
	 * @return the label displayed to the user
	 */
	public abstract String getLabel();
	/**
	 * Atomically relinks a given object to the current view.
	 * It could be necessary to relink objects when they are
	 * reloaded from database and about to be inserted in the
	 * view (i.e. undo checkouts or versionable import)
	 * 
	 * @param o object to relink to the current view
	 * @param invRefMap reverse dependencies map (useful for linking
	 * 					but time-consuming to build for each link
	 */
	public abstract void relink(ITypedObject o, MultiValueMap invRefMap);

}
