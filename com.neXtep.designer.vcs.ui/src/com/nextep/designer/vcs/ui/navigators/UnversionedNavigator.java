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
package com.nextep.designer.vcs.ui.navigators;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.ListeningConnector;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A navigator which handles unversioned elements. It manages the listener registering /
 * unregistering on object models.
 * 
 * @author Christophe Fondacci
 */
public abstract class UnversionedNavigator extends UntypedNavigator {

	/** Our logger */
	private static final Log log = LogFactory.getLog(UnversionedNavigator.class);
	/** The unversioned model */
	private IObservable unversionedModel = null;

	/** The unversioned model listener */
	// private IEvent modelListener;

	public UnversionedNavigator(IVersionable<?> parentVersionable, IObservable model,
			ITypedObjectUIController controller) {
		// Superconstructor on the versionable element
		super(parentVersionable, controller);
		// Member variables initialization
		this.unversionedModel = model;
		// Adding custom listener: our unversionned model
		addModelListener();
	}

	public Object getVersionedModel() {
		return super.getModel();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return unversionedModel;
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		if (model instanceof IVersionable) {
			super.setModel(model);
		}
		unversionedModel = (IObservable) model;
	}

	/**
	 * We override to provide our own model listener which will filter parent versionable events so
	 * they will not be propagated to the model.
	 * 
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#createModelListener(com.nextep.datadesigner.model.IObservable)
	 */
	@Override
	protected IEventListener createModelListener(IObservable model) {
		return new UnversionedModelListener(this);
	}

	public void addModelListener() {
		Designer.getListenerService().registerListener(this, unversionedModel, this);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ListeningControlledConnector#addListeners(com.nextep.datadesigner.model.IObservable,
	 *      boolean)
	 */
	@Override
	protected void addListeners(IObservable model) {
		super.addListeners(model);
		// addModelListener();
	}

	/**
	 * A model listener class which extends {@link ListeningConnector.ModelListener} only to filter
	 * parent versionable events from reaching our model.
	 * 
	 * @author Christophe Fondacci
	 */
	protected class UnversionedModelListener implements IEventListener, IModelOriented<IObservable> {

		ListeningConnector<?, ?> connector;

		public UnversionedModelListener(ListeningConnector<?, ?> connector) {
			this.connector = connector;
		}

		/**
		 * @see com.nextep.datadesigner.gui.impl.ListeningConnector.ModelListener#setModel(com.nextep.datadesigner.model.IObservable)
		 */
		@Override
		public void setModel(final IObservable model) {
			// Display.getDefault().syncExec(new Runnable() {
			//
			// @Override
			// public void run() {
			// setModelUiThreadSafe(model);
			// }
			// });
			// }
			//
			// private void setModelUiThreadSafe(IObservable model) {
			// This is our versionable listener, so we know we have a versionable
			IVersionable<?> v = (IVersionable<?>) model;
			Map<IReference, IReferenceable> refMap = v.getReferenceMap();
			// We locate our unversioned model in our new parent versionable
			IReference unversionedModelRef = ((IReferenceable) unversionedModel).getReference();
			// CorePlugin.getService(IReferenceManager.class).dereference((IReferenceable)unversionedModel);
			IReferenceable newUnversionnedModel = refMap.get(unversionedModelRef);
			// If the unversioned model reference does not exist we should remove the connector
			if (newUnversionnedModel == null) {
				log.debug("WARNING: Removing connector <" + connector.getClass().getName()
						+ "> which does no more exist in new parent model!");
				if (connector.getSWTConnector() != null
						&& !connector.getSWTConnector().isDisposed()) {
					connector.getSWTConnector().dispose();
				} else {
					connector.releaseConnector();
				}
			} else {
				if (connector.getModel() != newUnversionnedModel) {
					log.debug("Switching unversionned model from <" + connector.getModel()
							+ "> to <" + newUnversionnedModel + ">");
					Designer.getListenerService().switchListeners(
							(IObservable) connector.getModel(), (IObservable) newUnversionnedModel);
					connector.setModel(newUnversionnedModel);
				} else {
					log.debug("Model already set : " + connector.getModel());
				}
			}
		}

		/**
		 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
		 */
		@Override
		public IObservable getModel() {
			return null;
		}

		/**
		 * @see com.nextep.datadesigner.gui.impl.ListeningConnector.ModelListener#dispatchEvent(com.nextep.datadesigner.model.ChangeEvent,
		 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
		 */
		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		}
	}

}
