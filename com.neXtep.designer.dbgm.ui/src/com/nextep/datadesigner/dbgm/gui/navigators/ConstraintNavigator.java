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
package com.nextep.datadesigner.dbgm.gui.navigators;



import java.util.Collections;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.navigators.UnversionedNavigator;

/**
 * @author Christophe Fondacci
 *
 */
public class ConstraintNavigator extends UnversionedNavigator {
//	private static final Log log = LogFactory.getLog(ConstraintNavigator.class);

	public ConstraintNavigator(IKeyConstraint constraint, ITypedObjectUIController controller) {
		super(VersionHelper.getVersionable(constraint.getConstrainedTable()),constraint,controller);
	}
	@Override
	public void initializeChildConnectors() {
		final IKeyConstraint constraint = (IKeyConstraint)getModel();
		for(IReference c : constraint.getConstrainedColumnsRef()) {
			addConnector(UIControllerFactory.getController(IElementType.getInstance("REFERENCE")).initializeNavigator(c));
		}		
	}
	/**
	 * Overriding default behaviour to avoid columns sort
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#addConnector(com.nextep.datadesigner.gui.model.INavigatorConnector)
	 */
	public void addConnector(INavigatorConnector c) {
		getConnectors().add(c);
		if(initialized) {
			createConnector(c, getConnectors().indexOf(c));
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		switch(((IKeyConstraint)getModel()).getConstraintType()) {
		case UNIQUE:
		case PRIMARY:
			return DBGMImages.ICON_PK;
		case FOREIGN:
			return DBGMImages.ICON_FK;
		}
		return null;
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IKeyConstraint constraint = (IKeyConstraint)getModel();
		switch (event) {
		case COLUMN_ADDED:
			IReference addedColRef = null;
			if(data instanceof IReference) {
				addedColRef = (IReference)data;
			} else {
				addedColRef = ((IBasicColumn)data).getReference();
			}
			addConnector(UIControllerFactory.getController(IElementType.getInstance(IReference.TYPE_ID)).initializeNavigator(addedColRef));
			break;
		case COLUMN_REMOVED:
			IReference removedColRef = null;
			if(data instanceof IReference) {
				removedColRef = (IReference)data;
			} else {
				removedColRef = ((IBasicColumn)data).getReference();
			}
			removeConnector(getConnector(removedColRef));
			break;
		case MODEL_CHANGED:
			if(data instanceof IBasicColumn) {
				IReference colRef = ((IBasicColumn)data).getReference();
				INavigatorConnector c = getConnector( colRef );
				int index = getConnectors().indexOf(c);
				int constraintIndex = constraint.getConstrainedColumnsRef().indexOf(colRef);
				if(index!=constraintIndex) {
					Collections.swap(getConnectors(),index,constraintIndex);
					c.getSWTConnector().dispose();
					createConnector(c, constraintIndex);

				}
			}
			break;
		}
		refreshConnector();

	}

}
