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
package com.nextep.designer.dbgm.oracle.factories;

import java.util.Map;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.factories.TableFactory;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.OracleTable;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.oracle.impl.external.OracleIndexPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.OracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * An extension of the table basic factory to handle Oracle specific data and to provide the correct
 * implementor.
 * 
 * @author Christophe Fondacci
 */
public class OracleTableFactory extends TableFactory {

	/**
	 * @see com.nextep.designer.dbgm.factories.TableFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return new OracleTable();
	}

	/**
	 * @see com.nextep.designer.dbgm.factories.TableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionable)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		// Copying common content
		super.rawCopy(source, destination);
		// Handling Oracle specific content
		IOracleTable src = (IOracleTable) source.getVersionnedObject().getModel();
		IOracleTable tgt = (IOracleTable) destination.getVersionnedObject().getModel();
		// Copying physical attributes
		if (src.getPhysicalProperties() != null) {
			IOracleTablePhysicalProperties tgtProps = new OracleTablePhysicalProperties();
			tgt.setPhysicalProperties(tgtProps);
			OracleFactoryHelper.copyPhysicalProperties(src, tgt);
			tgtProps.setParent(tgt);
			tgtProps.setPhysicalOrganisation(((IOracleTablePhysicalProperties) src
					.getPhysicalProperties()).getPhysicalOrganisation());
			for (IReference r : ((IOracleTablePhysicalProperties) src.getPhysicalProperties())
					.getPartitionedColumnsRef()) {
				tgtProps.addPartitionedColumnRef(r);
			}
		}

		// Key physical properties
		Map<IReference, IReferenceable> refMap = VersionHelper.getVersionable(tgt)
				.getReferenceMap();
		for (IKeyConstraint c : src.getConstraints()) {
			if (!(c instanceof OracleUniqueConstraint)) {
				continue;
			}
			OracleUniqueConstraint copiedConstraint = (OracleUniqueConstraint) refMap.get(c
					.getReference());
			IPhysicalProperties props = ((OracleUniqueConstraint) c).getPhysicalProperties();
			if (props != null) {
				IPhysicalProperties tgtProps = new OracleIndexPhysicalProperties();
				copiedConstraint.setPhysicalProperties(tgtProps);
				OracleFactoryHelper.copyPhysicalProperties((OracleUniqueConstraint) c,
						copiedConstraint);
				tgtProps.setParent(copiedConstraint);
			}
		}

		// Check constraints
		for (ICheckConstraint c : src.getCheckConstraints()) {
			ICheckConstraint newCheck = CorePlugin.getTypedObjectFactory().create(
					ICheckConstraint.class);
			newCheck.setName(c.getName());
			newCheck.setConstrainedTable(tgt);
			newCheck.setCondition(c.getCondition());
			newCheck.setDescription(c.getDescription());
			newCheck.setReference(c.getReference());
			tgt.addCheckConstraint(newCheck);
		}
	}

	@Override
	protected UniqueKeyConstraint createUniqueKey(String name, String description,
			IBasicTable parent) {
		OracleUniqueConstraint uk = new OracleUniqueConstraint();
		uk.setName(name);
		uk.setDescription(description);
		uk.setConstrainedTable(parent);
		return uk;
	}
}
