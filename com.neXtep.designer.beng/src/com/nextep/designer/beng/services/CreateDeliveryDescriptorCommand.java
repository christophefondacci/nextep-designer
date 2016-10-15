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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.neXtep.shared.model.ITagNames;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengMessages;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.impl.ModuleDeliveryIncrement;
import com.nextep.designer.beng.xml.WritingException;
import com.nextep.designer.beng.xml.XmlWriter;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class CreateDeliveryDescriptorCommand implements ICommand {

	private IDeliveryModule module;

	public CreateDeliveryDescriptorCommand(IDeliveryModule module) {
		this.module = module;
	}

	/**
	 * @see com.nextep.datadesigner.model.ICommand#execute(java.lang.Object[])
	 */
	@Override
	public Object execute(Object... parameters) {
		final IDeliveryService deliveryService = BengPlugin.getService(IDeliveryService.class);
		StringWriter strWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(strWriter);
		try {
			// Tricking to have a container name
			IVersionContainer container = null;
			final boolean refVol = module.getModuleRef().isVolatile();
			if (!refVol) {
				try {
					container = (IVersionContainer) VersionHelper.getReferencedItem(module
							.getModuleRef());
				} catch (UnresolvedItemException e) {
					container = null;
				}
			}
			if (container == null) {
				container = (IVersionContainer) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, module.getTargetRelease().getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), true);
			}
			// try {
			// // We assume our module reference is also in the view
			// module.getModuleRef().setVolatile(false);
			// container =
			// (IVersionContainer)VersionHelper.getReferencedItem(module.getModuleRef());
			// } finally {
			// module.getModuleRef().setVolatile(refVol);
			// }
			writer.writeEntity(ITagNames.DELIVERY)
					.writeAttribute(ITagNames.ATTR_NAME, container.getName())
					.writeAttribute(ITagNames.DELIVERY_REF_UID,
							module.getModuleRef().getUID().toString())
					.writeAttribute(ITagNames.DELV_ATTR_ADMIN, String.valueOf(module.isAdmin()))
					.writeAttribute(ITagNames.DELV_ATTR_FIRST,
							String.valueOf(module.isFirstRelease()))
					.writeAttribute(
							ITagNames.DELV_DBVENDOR,
							container.getDBVendor() == null ? DBGMHelper.getCurrentVendor().name()
									: container.getDBVendor().name());
			// Writing release section
			writer.writeEntity(ITagNames.RELEASE).writeAttribute(
					ITagNames.REL_ATTR_MODE,
					parameters.length == 0 ? ITagNames.REL_VAL_MODE_RANGE
							: ITagNames.REL_VAL_MODE_STRICT);
			// From release only appears if defined
			if (module.getFromRelease() != null) {
				writer.writeEntity(ITagNames.FROM_RELEASE)
						.writeAttribute(ITagNames.REL_ATTR_MAJOR,
								String.valueOf(module.getFromRelease().getMajorRelease()))
						.writeAttribute(ITagNames.REL_ATTR_MINOR,
								String.valueOf(module.getFromRelease().getMinorRelease()))
						.writeAttribute(ITagNames.REL_ATTR_ITERATION,
								String.valueOf(module.getFromRelease().getIteration()))
						.writeAttribute(ITagNames.REL_ATTR_PATCH,
								String.valueOf(module.getFromRelease().getPatch()))
						.writeAttribute(ITagNames.REL_ATTR_REVISION,
								String.valueOf(module.getFromRelease().getRevision()))
						.writeAttribute(ITagNames.CREATE_RELEASE, "false") //$NON-NLS-1$
						.endEntity();
			}
			// Target release tag, always present
			writer.writeEntity(ITagNames.TARGET_RELEASE)
					.writeAttribute(ITagNames.REL_ATTR_MAJOR,
							String.valueOf(module.getTargetRelease().getMajorRelease()))
					.writeAttribute(ITagNames.REL_ATTR_MINOR,
							String.valueOf(module.getTargetRelease().getMinorRelease()))
					.writeAttribute(ITagNames.REL_ATTR_ITERATION,
							String.valueOf(module.getTargetRelease().getIteration()))
					.writeAttribute(ITagNames.REL_ATTR_PATCH,
							String.valueOf(module.getTargetRelease().getPatch()))
					.writeAttribute(ITagNames.REL_ATTR_REVISION,
							String.valueOf(module.getTargetRelease().getRevision())).endEntity();
			writer.endEntity();
			// Dependencies
			Collection<IVersionInfo> dependencies = deliveryService.buildDependencies(
					new ArrayList<IVersionInfo>(), module);
			if (dependencies != null && !dependencies.isEmpty()) {
				writer.writeEntity(ITagNames.DELV_DEPENDENCIES);
				for (IVersionInfo vc : dependencies) {
					if (vc.equals(module.getTargetRelease())) {
						continue;
					}
					// Taking name from current workspace container
					String depName = "Undefined";
					boolean oldVolatile = vc.getReference().isVolatile();
					try {
						// Tricking
						vc.getReference().setVolatile(false);
						IVersionContainer depContainer = (IVersionContainer) VersionHelper
								.getReferencedItem(vc.getReference());
						depName = depContainer.getName();
					} catch (UnresolvedItemException e) {

					} finally {
						vc.getReference().setVolatile(oldVolatile);
					}
					writer.writeEntity(ITagNames.DELV_DEP_MODULE)
							.writeAttribute(ITagNames.ATTR_NAME, depName)
							.writeAttribute(ITagNames.DELIVERY_REF_UID,
									vc.getReference().getUID().toString())
							.writeAttribute(ITagNames.REL_ATTR_MAJOR,
									String.valueOf(vc.getMajorRelease()))
							.writeAttribute(ITagNames.REL_ATTR_MINOR,
									String.valueOf(vc.getMinorRelease()))
							.writeAttribute(ITagNames.REL_ATTR_ITERATION,
									String.valueOf(vc.getIteration()))
							.writeAttribute(ITagNames.REL_ATTR_PATCH, String.valueOf(vc.getPatch()))
							.writeAttribute(ITagNames.REL_ATTR_REVISION,
									String.valueOf(vc.getRevision()));
					IDeliveryModule m = deliveryService.loadDelivery(vc);
					if (m != null && m != module) {
						writer.writeAttribute(ITagNames.DELV_ATTR_ADMIN,
								String.valueOf(m.isAdmin()));
						writeDeliveryItem(writer, m);
					}
					// Ending <module> section
					writer.endEntity();
				}
				// Ending <dependencies> section
				writer.endEntity();
			}
			if (module.isUniversal() || (dependencies != null && !dependencies.isEmpty())) {
				// Writing requirements
				writer.writeEntity(ITagNames.DELV_REQUIREMENTS);
				final List<IDeliveryInfo> processedDeliveries = new ArrayList<IDeliveryInfo>();

				// Processing universal delivery requirements
				if (module.isUniversal()) {
					try {
						IVersionContainer moduleContainer = null;
						try {
							moduleContainer = (IVersionContainer) VersionHelper
									.getReferencedItem(module.getModuleRef());
						} catch (UnresolvedItemException e) {
							moduleContainer = null;
						}
						List<IDeliveryInfo> dlvInfo = deliveryService
								.getDeliveries(new ModuleDeliveryIncrement(moduleContainer, null,
										module.getFromRelease()));
						for (IDeliveryInfo i : dlvInfo) {
							if (!processedDeliveries.contains(i)) {
								writer.writeEntity(ITagNames.DELIVERY)
										.writeAttribute(ITagNames.ATTR_NAME, i.getName())
										.endEntity();
								processedDeliveries.add(i);
							}
						}
					} catch (UndeliverableIncrementException e) {
						throw new ErrorException(
								"Unable to generate requirements for universal delivery.", e); //$NON-NLS-1$
					}
				}
				for (IVersionInfo vc : dependencies) {
					if (vc.equals(module.getTargetRelease())) {
						continue;
					}
					final IDeliveryIncrement inc = deliveryService.computeIncrement(module, vc);
					try {
						final List<IDeliveryInfo> dlvs = deliveryService.getDeliveries(inc);
						for (IDeliveryInfo i : dlvs) {
							// There might be some duplicates here (and there might be a better way
							// to filter them)
							if (!processedDeliveries.contains(i)) {
								writer.writeEntity(ITagNames.DELIVERY)
										.writeAttribute(ITagNames.ATTR_NAME, i.getName())
										.endEntity();
								processedDeliveries.add(i);
							}
						}
					} catch (UndeliverableIncrementException e) {

					}
				}
				// Ending requirements
				writer.endEntity();
			}

			// Writing contents
			for (DeliveryType type : DeliveryType.values()) {
				List<IDeliveryItem<?>> items = module.getDeliveries(type);
				if (items != null) {
					writer.writeEntity(ITagNames.CATEGORY)
							.writeAttribute(ITagNames.ATTR_NAME, type.getTagName())
							.writeAttribute(ITagNames.PATH, type.getFolderName());
					final List<String> processedNames = new ArrayList<String>();
					for (IDeliveryItem<?> item : items) {
						// FIXME: Ugly quickfix for tvtrip export, need to understand why there can
						// be duplicate DeliveryItem instances here
						if (!processedNames.contains(item.getArtefactName())) {
							processedNames.add(item.getArtefactName());
							writeDeliveryItem(writer, item);
						}
					}
					writer.endEntity();
				}
			}

			// Writing checks
			writer.writeEntity(ITagNames.CHECK_RELEASE);
			for (IVersionable<?> v : container.getContents()) {
				if (v.getVersionnedObject().getModel() instanceof IDatabaseObject) {
					IDatabaseObject<?> dbObject = (IDatabaseObject<?>) v.getVersionnedObject()
							.getModel();
					writer.writeEntity(ITagNames.CHECK_OBJ)
							.writeAttribute(
									ITagNames.ATTR_NAME,
									dbObject instanceof IIndex ? ((IIndex) dbObject).getIndexName()
											: dbObject.getName())
							.writeAttribute(
									ITagNames.ATTR_TYPE,
									dbObject.getType().getDatabaseType(
											VersionHelper.getCurrentView().getDBVendor()))
							.endEntity();
				}
			}
			// Writing table column checks
			List<IVersionable<?>> tablesV = VersionHelper.getAllVersionables(container,
					IElementType.getInstance(IBasicTable.TYPE_ID));
			for (IVersionable<?> tableV : tablesV) {
				IBasicTable t = (IBasicTable) tableV.getVersionnedObject().getModel();
				for (IBasicColumn c : t.getColumns()) {
					writer.writeEntity(ITagNames.CHECK_OBJ)
							.writeAttribute(ITagNames.ATTR_NAME, t.getName() + "." + c.getName()) //$NON-NLS-1$
							.writeAttribute(ITagNames.ATTR_TYPE, "COLUMN") //$NON-NLS-1$
							.endEntity();
				}
			}
			writer.endEntity();
			writer.endEntity();
			writer.close();
		} catch (WritingException e) {
			throw new ErrorException(e);
		}
		// System.out.print(strWriter.toString());
		return strWriter.toString();
	}

	private void writeDeliveryItem(XmlWriter writer, IDeliveryItem<?> item) throws WritingException {
		writer.writeEntity(ITagNames.DELIVERY_ITEM)
				.writeAttribute(ITagNames.ATTR_NAME, item.getName())
				.writeAttribute(ITagNames.ATTR_ARTEFACT, item.getArtefactName())
				.writeAttribute(ITagNames.ATTR_ARTEFACT_TYPE, item.getArtefactType().name());
		if (item.getDBVendor() != null) {
			writer.writeAttribute(ITagNames.ATTR_ARTEFACT_VENDOR, item.getDBVendor().name());
		}
		writer.endEntity();
	}

	/**
	 * @see com.nextep.datadesigner.model.ICommand#getName()
	 */
	@Override
	public String getName() {
		return BengMessages.getString("createDeliveryDescriptorCommand.command.name"); //$NON-NLS-1$
	}

}
