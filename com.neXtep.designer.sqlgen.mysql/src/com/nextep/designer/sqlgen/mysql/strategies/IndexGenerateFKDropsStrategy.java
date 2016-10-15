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
package com.nextep.designer.sqlgen.mysql.strategies;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.exception.TooManyReferencesException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.strategies.DoDropStrategy;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.mysql.MySQLMessages;

public class IndexGenerateFKDropsStrategy extends DoDropStrategy {

	private static final Log log = LogFactory.getLog(IndexGenerateFKDropsStrategy.class);

	public IndexGenerateFKDropsStrategy() {
		setName(MySQLMessages.getString("dropStrategy_fk_name")); //$NON-NLS-1$
		setDescription(MySQLMessages.getString("dropStrategy_fk_desc")); //$NON-NLS-1$
	}

	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop) {
		final IIndex index = (IIndex) modelToDrop;
		IGenerationResult result = GenerationFactory.createGenerationResult();
		Collection<ForeignKeyConstraint> enforcedFkeys = DBGMHelper.getForeignKeysForIndex(index);
		Collection<ForeignKeyConstraint> fkeysToRecreate = new ArrayList<ForeignKeyConstraint>();

		// We need a FK generator to drop / recreate foreign keys
		final ISQLGenerator fkGenerator = getGenerator(IElementType.getInstance("FOREIGN_KEY")); //$NON-NLS-1$

		for (ForeignKeyConstraint fk : enforcedFkeys) {
			// Retrieving current repository foreign key (because here we have a database or
			// previous version
			// foreign key since the index no longer exists in the repository workspace.
			try {
				ForeignKeyConstraint repositoryFk = (ForeignKeyConstraint) CorePlugin.getService(
						IReferenceManager.class).findByTypeName(fk.getType(), fk.getName());
				final Collection<IDatabaseRawObject> enforcingIndexes = repositoryFk
						.getEnforcingIndex();
				if (!enforcingIndexes.isEmpty()) {
					fkeysToRecreate.add(fk);
				} else {
					logFKWarning(repositoryFk);
				}
			} catch (TooManyReferencesException e) {
				logFKWarning(fk);
				// Nothing to do, only here for debugging
				log.debug(e);
			} catch (ReferenceNotFoundException e) {
				// Nothing to do, only here for debugging
				log.debug(e);
			}

			// Dropping the foreign key which depends on this index
			result.integrate(fkGenerator.generateDrop(fk));
		}
		// Integrating standard drop
		result.integrate(super.generateDrop(generator, modelToDrop));

		// Now recreating foreign keys which are still enforced by other indexes
		for (ForeignKeyConstraint fk : fkeysToRecreate) {
			result.integrate(fkGenerator.generateFullSQL(fk));
		}
		return result;
	}

	private void logFKWarning(ForeignKeyConstraint fk) {
		log.warn(MessageFormat.format(
				MySQLMessages.getString("dropStrategy_fk_fkRecreationFailure"), //$NON-NLS-1$
				fk.getName()));
	}
}
