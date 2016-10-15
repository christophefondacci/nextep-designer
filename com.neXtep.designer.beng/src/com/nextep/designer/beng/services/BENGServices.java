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
package com.nextep.designer.beng.services;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.impl.FileUtils;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class BENGServices {

	// private static final long SYSTEM_CONTAINER_ID = 111;
	private static Map<IVersionContainer, IDeliveryModule> deliveriesMap = new HashMap<IVersionContainer, IDeliveryModule>();
	private static final int FILE_BUFFER = 10240;
	private static Log log = LogFactory.getLog(BENGServices.class);

	public static void setDeliveriesMap(Map<IVersionContainer, IDeliveryModule> deliveries) {
		deliveriesMap = deliveries;
	}

	public static void addDelivery(IDeliveryModule m) {
		deliveriesMap.put((IVersionContainer) VersionHelper.getReferencedItem(m.getModuleRef()), m);
	}

	/**
	 * This method retrieves the delivery associated with the specified container. If no delivery
	 * exists for the given container, <code>null</code> will be returned.
	 * 
	 * @param c container for delivery search
	 * @return the delivery module of the specified container
	 */
	public static IDeliveryModule getCurrentDelivery(IVersionContainer c) {
		IDeliveryModule m = deliveriesMap.get(c);
		if (m == null || !m.getTargetRelease().equals(VersionHelper.getVersionInfo(c))) {
			IVersionable<IVersionContainer> v = VersionHelper.getVersionable(c);
			m = BengPlugin.getService(IDeliveryService.class).loadDelivery(v);
			deliveriesMap.put(c, m);
		}
		if (m != null && m.getTargetRelease().equals(VersionHelper.getVersionInfo(c))) {
			return m;
		}
		return null;
	}

	/**
	 * Retrieves the container (modules) on which depend the specified one. Used by the build engine
	 * to determine dependent deliveries.
	 * 
	 * @param c the container for which the dependencies will be retrieved
	 * @return the containers on which the specified container depend
	 */
	@SuppressWarnings("unchecked")
	public static List<IVersionContainer> getContainerDependencies(IVersionContainer c) {
		ListOrderedSet dependencies = new ListOrderedSet();
		// Retrieving the reference map of our current container
		Map<IReference, IReferenceable> containerRefMap = VersionHelper.getVersionable(c)
				.getReferenceMap();
		// We browse all versionable of our container
		for (IVersionable<?> v : c.getContents()) {
			// For each of them we retrieve the loose dependencies
			Collection<IReference> references = v.getReferenceDependencies();
			for (IReference r : references) {
				// If the remote dependency is not from our container we save it
				if (!containerRefMap.containsKey(r)) {
					IReferenceable instance = VersionHelper.getReferencedItem(r);
					if (instance instanceof IVersionable) {
						final IVersionable<?> versionedDependency = (IVersionable<?>) instance;
						if (!versionedDependency.getContainer().equals(c)) {
							dependencies.add(versionedDependency.getContainer());
						}

					}
				}
			}
		}
		return (List<IVersionContainer>) dependencies.asList();
	}

	/**
	 * Retrieves all delivery modules generated for the specified container.
	 * 
	 * @param c container to look for existing deliveries
	 * @return a list of all generated delivery modules
	 */
	@SuppressWarnings("unchecked")
	public static List<IDeliveryModule> getAllDeliveries(IReference containerRef) {
		List<IDeliveryModule> deliveries = (List<IDeliveryModule>) CorePlugin.getIdentifiableDao()
				.loadForeignKey(IDeliveryModule.class, containerRef.getUID(), "moduleRef", false); //$NON-NLS-1$
		Collections.sort(deliveries);
		return deliveries;
	}

	/**
	 * Generates the artefact without any of its dependency
	 * 
	 * @see com.nextep.designer.beng.model.IDeliveryItem#generateArtefact(java.lang.String)
	 */
	public static void generateArtefact(IDeliveryModule module, String directoryTarget) {
		// Generating outputs
		String exportLoc = directoryTarget + File.separator + module.getName();
		File f = new File(exportLoc);
		f.mkdir();
		// Resetting descriptor as it should not be possible
		// to deploy a partially generated release
		FileUtils.writeToFile(exportLoc + File.separator + "delivery.xml", ""); //$NON-NLS-1$ //$NON-NLS-2$
		// Generating folders
		for (DeliveryType t : DeliveryType.values()) {
			File typeFolder = new File(exportLoc + File.separator + t.getFolderName());
			typeFolder.mkdir();
		}
		// Generating delivery items
		for (IDeliveryItem<?> i : module.getDeliveryItems()) {
			i.generateArtefact(exportLoc);
		}
		// Copying external files
		// for(IExternalFile ext : module.getExternalFiles()) {
		// final String originalFilePath = ext.getDirectory(); // + File.separator + ext.getName();
		// File exportedFile = new File(exportLoc + File.separator +
		// DeliveryType.CUSTOM.getFolderName() + File.separator + ext.getName());
		// File originalFile = new File(originalFilePath);
		// if(!originalFile.exists() || !originalFile.isFile()) {
		// throw new ErrorException("The external file '" + originalFilePath + "' no more exists.");
		// }
		// try {
		// copyFile(originalFile, exportedFile);
		// } catch( IOException e) {
		// throw new ErrorException("Problems while copying external file '" + originalFilePath +
		// "' into delivery module. Exception message was: " + e.getMessage(),e);
		// }
		// }
		// Generating delivery descriptor
		FileUtils.writeToFile(
				exportLoc + File.separator + "delivery.xml", module.generateDescriptor()); //$NON-NLS-1$
	}

	public static IVersionInfo getPreviousDependencyRelease(IDeliveryModule module,
			IVersionInfo dependencyTargetRelease, IVersionInfo fromRel) {
		if (fromRel != null) {
			final IDeliveryModule fromModule = BengPlugin.getService(IDeliveryService.class)
					.loadDelivery(fromRel);
			if (fromModule != null) {
				for (IVersionInfo dependency : fromModule.getDependencies()) {
					if (dependency.getReference().getUID()
							.equals(dependencyTargetRelease.getReference().getUID())) {
						return dependency;
					}
				}
			}
		}
		return null;
	}
}
