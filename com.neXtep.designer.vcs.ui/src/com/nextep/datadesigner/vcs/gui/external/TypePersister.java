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
package com.nextep.datadesigner.vcs.gui.external;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.ITypePersister;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;

/**
 * A default implementation of the {@link ITypePersister} interface which also implements the
 * {@link IElementFactory} interface.<br>
 * This abstract class only checks the appropriate Class (given by implementations) on the setModel
 * call and delegates the load of this class to the DAO.<br>
 * It also only reload objects of the correct version view. <br>
 * Note that the typed object must also be Identifiable.
 * 
 * @author Christophe Fondacci
 */
public abstract class TypePersister implements ITypePersister, IElementFactory {

	private static final Log log = LogFactory.getLog(TypePersister.class);
	/** The typed object model */
	private ITypedObject model;

	/**
	 * Model getter allowing extensions to manipulate the model
	 * 
	 * @return the typed object model
	 */
	public ITypedObject getModel() {
		return model;
	}

	/**
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		if (getModel() != null) {
			IdentifiedObject idObj = (IdentifiedObject) getModel();
			if (idObj.getUID() != null) {
				memento.putString("VIEW_ID", VersionHelper.getCurrentView().getUID().toString());
				memento.putString("MODEL_ID", ((IdentifiedObject) getModel()).getUID().toString());
			}
		}

	}

	/**
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	@Override
	public IAdaptable createElement(IMemento memento) {
		String viewId = memento.getString("VIEW_ID");
		String modelId = memento.getString("MODEL_ID");
		try {
			if (VersionHelper.getCurrentView().getUID().equals(new UID(Long.valueOf(viewId)))) {
				model = (ITypedObject) CorePlugin.getIdentifiableDao().load(getPersistableClass(),
						new UID(Long.valueOf(modelId)));
				return new TypedEditorInput(model);
			}
		} catch (RuntimeException e) {
			// logging and returning null whenever any error occurs
			log.debug("WARN: Unable to create former editor: ", e);
			return null;
		}
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ITypePersister#setModel(com.nextep.datadesigner.model.ITypedObject)
	 */
	@Override
	public void setModel(ITypedObject model) {
		if (getPersistableClass().isInstance(model)) {
			this.model = (ITypedObject) model;
		} else {
			throw new ErrorException("Incorrect model to persist: expecting "
					+ getPersistableClass().getName() + ".");
		}
	}

	/**
	 * @return the class whose persistance is handled by this persistor
	 */
	protected abstract Class<?> getPersistableClass();

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// Does nothing
	}
}
