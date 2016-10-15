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
package com.nextep.datadesigner.gui.model;

import java.util.Collection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Widget;
import com.nextep.datadesigner.model.IModelOriented;


/**
 * @author Christophe Fondacci
 *
 */
public interface IConnector<T extends Widget,U extends IConnector<?,?>> extends IModelOriented<Object> {
	/**
	 * Performs a graphical refresh of this element
	 */
	public abstract void refreshConnector();
	/**
	 * Releases this connector
	 */
	public abstract void releaseConnector();
	/**
	 * Retrieves the title of this connector that might be
	 * displayed or formatted by the parent connector.
	 * It could also be used as an index for connector ordering.
	 * @return the connector title
	 */
	public abstract String getTitle();
	/**
	 *
	 * @return the SWT control associated to this connector
	 */
	public abstract T getSWTConnector();
	/**
	 * Adds a child connector to this connector.
	 * Depending on the connector, calling this function
	 * may have no effect.
	 *
	 * @param child child connector to add
	 */
	public abstract void addConnector(U child);
	/**
	 * Removes a child connector from this connector. Depending
	 * on the connector type, calling this function may
	 * have no effect.
	 *
	 * @param child child connector to remove
	 */
	public abstract void removeConnector(U child);
	/**
	 * @return the list of child connectors
	 */
	public abstract Collection<U> getConnectors();
	/**
	 * Retrieves the icon of this connector that might be displayed
	 * or formatted by the parent connector
	 *
	 * @return the Image object representing the connector's icon
	 */
	public abstract Image getConnectorIcon();
	/**
	 * Performs initialization stuff immediatly after the SWT
	 * control creation.<br>
	 * Implementors must ensure that after calling this method
	 * the method {@link IConnector#isInitialized()} will return
	 * <code>true</code>
	 */
	public abstract void initialize();
	/**
	 * This method indicates if the connector has been initialized.
	 * An initialized connector means that: <br>
	 *   - It contains a valid model<br>
	 *   - All graphical controls have been created<br>
	 *   - Control is ready to accept any notification event<br>
	 *  
	 * @return <code>true</code> if initialized, else <code>false</code>
	 */
	public abstract boolean isInitialized();
}
