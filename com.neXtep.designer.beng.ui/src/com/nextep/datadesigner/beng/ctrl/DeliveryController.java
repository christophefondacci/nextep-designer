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
package com.nextep.datadesigner.beng.ctrl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.beng.gui.DeliveryEditorGUI;
import com.nextep.datadesigner.beng.gui.DeliveryModuleNavigator;
import com.nextep.datadesigner.beng.gui.service.BengUIService;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.dao.IDeliveryDao;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.BENGServices;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.BengUIPlugin;
import com.nextep.designer.beng.ui.services.IDeliveryUIService;
import com.nextep.designer.beng.ui.views.DeliveriesView;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Controller of delivery related features.
 * 
 * @author Christophe Fondacci
 */
public class DeliveryController extends AbstractUIController implements ITypedObjectUIController {

	private static final Log log = LogFactory.getLog(DeliveryController.class);

	public DeliveryController() {
		addSaveEvent(ChangeEvent.VERSIONABLE_ADDED);
		addSaveEvent(ChangeEvent.VERSIONABLE_REMOVED);
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.ITEM_ADDED);
		addSaveEvent(ChangeEvent.ITEM_REMOVED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	public IDisplayConnector initializeEditor(Object content) {
		return new DeliveryEditorGUI((IDeliveryModule) content, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	public INavigatorConnector initializeNavigator(Object model) {
		return new DeliveryModuleNavigator((IDeliveryModule) model, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method returns the current delivery module if an attempt is made to create a module on a
	 * container which already has a delivery module created.
	 * 
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Object newInstance(Object parent) {
		if (VersionHelper.getVersionable(parent) == null) {
			throw new ErrorException(BengUIMessages.getString("pkgNonVersionableException")); //$NON-NLS-1$
		}
		IVersionable<IVersionContainer> container = (IVersionable<IVersionContainer>) parent;
		// Looking if a module already exists for this container
		IDeliveryModule module = BENGServices.getCurrentDelivery(container.getVersionnedObject()
				.getModel());
		if (module != null) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(DeliveriesView.VIEW_ID);
			} catch (PartInitException e) {
				log.error("Problems while showing the deliveries view."); //$NON-NLS-1$
			}
			// throw new
			// ErrorException("A delivery module has already been created for this container release. You cannot create 2 deliveries for the same release of a container, please edit the existing delivery from the deliveries explorer.");
			return module;
		}
		// Creating the new delivery
		module = CorePlugin.getTypedObjectFactory().create(IDeliveryModule.class);
		module.setModuleRef(container.getReference());
		module.setFromRelease(null);
		module.setTargetRelease(container.getVersion());
		module.setDBVendor(container.getVersionnedObject().getModel().getDBVendor());
		NamingService.getInstance().adjustName(module);
		prepareDelivery(module, container);
		newWizardEdition(
				BengUIMessages.getString("delivery.wizard.title"), initializeEditor(module)); //$NON-NLS-1$

		save(module);
		// Pre-building artefacts
		BengUIService.buildArtefacts(module);
		getDeliveryUIService().build(module);
		BENGServices.addDelivery(module);
		log.info("Delivery module '" + module.getName() + "' has been successfully created."); //$NON-NLS-1$ //$NON-NLS-2$
		// Refreshing deliveries view
		try {
			BengUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(DeliveriesView.VIEW_ID);
		} catch (PartInitException e) {
			log.error("Unable to show deliveries view.", e); //$NON-NLS-1$
		}
		// Refreshing deliveries view
		DeliveriesView view = (DeliveriesView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(DeliveriesView.VIEW_ID);
		if (view != null) {
			view.refresh();
		}
		return module;
	}

	@SuppressWarnings("unchecked")
	private void prepareDelivery(IDeliveryModule module, IVersionable<IVersionContainer> container) {
		final IDeliveryDao deliveryDao = BengPlugin.getService(IDeliveryDao.class);
		List<IDeliveryInfo> deliveries = deliveryDao.getAvailableDeliveries(container.getVersion()
				.getReference());
		if (deliveries == null || deliveries.isEmpty())
			return;
		// hashing by target release
		Map<String, IDeliveryInfo> dlvRelMap = new HashMap<String, IDeliveryInfo>();
		for (IDeliveryInfo i : deliveries) {
			dlvRelMap.put(i.getTargetRelease().getLabel(), i);
		}
		// retrieving last delivery
		IVersionInfo rel = container.getVersion().getPreviousVersion();
		IDeliveryInfo lastDlv = null;
		while (rel != null && lastDlv == null) {
			if (dlvRelMap.get(rel.getLabel()) != null) {
				lastDlv = dlvRelMap.get(rel.getLabel());
			}
			rel = rel.getPreviousVersion();
		}
		// Returning if no previous delivery
		if (lastDlv == null)
			return;
		// Else injecting some information
		// Current from release is last delivery's target release
		module.setFromRelease(lastDlv.getTargetRelease());
		// Computing dependencies from dependen references
		IDeliveryModule m = deliveryDao.loadModule(lastDlv);
		if (m != null) {
			for (IVersionInfo depRel : m.getDependencies()) {
				// Retrieving current in-view reference
				try {
					depRel.getReference().setVolatile(false);
					final IReferenceable r = VersionHelper.getReferencedItem(depRel.getReference());
					final IVersionable<?> v = VersionHelper.getVersionable(r);
					if (v != null) {
						module.addDependency(v.getVersion());
					}
				} catch (ErrorException e) {
					// Doing nothing
				}

			}
		}

	}

	// /**
	// * Adds a new delivery sub module to a given delivery module
	// * and saves it properly
	// *
	// * @param name name of the child delivery module
	// * @param parent parent delivery module
	// */
	// private void addModule(String name, IDeliveryModule parent) {
	// IVersionable<IDeliveryModule> m =
	// VersionableFactory.createVersionable(IDeliveryModule.class);
	// m.setContainer(parent);
	// m.setName(name);
	// save(m);
	// parent.addVersionable(m,new ImportPolicyAddOnly());
	// }
	/**
	 * A specific extension of the handle event listener which will alter the save behaviour of the
	 * VERSIONABLE_ADDED event to save the added versionable first, and only then fires the
	 * container save. TODO: mutualize with ContainerController
	 * 
	 * @see com.nextep.designer.ui.model.base.AbstractUIController#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case ITEM_ADDED:
			save((IdentifiedObject) data);
			save((IdentifiedObject) source);
			break;
		case ITEM_REMOVED:
			final IDeliveryItem<?> removedItem = (IDeliveryItem<?>) data;
			CorePlugin.getIdentifiableDao().save((IDeliveryModule) source);
			CorePlugin.getIdentifiableDao().save(removedItem);
			CorePlugin.getIdentifiableDao().delete(removedItem);
			removedItem.setUID(null);
			break;
		// if(source instanceof IDeliveryItem) {
		// List<IDeliveryItem<?>> items = ((IDeliveryModule)source).getDeliveryItems();
		// for(IDeliveryItem<?> i : items) {
		// try {
		// IdentifiableDAO.getInstance().delete(i);
		// } catch(HibernateException e) {
		// // Should be ok
		// }
		// }
		// }
		case MODEL_CHANGED:
			save((IdentifiedObject) source);
			break;
		}
	}

	private IDeliveryUIService getDeliveryUIService() {
		return BengUIPlugin.getService(IDeliveryUIService.class);
	}
}
