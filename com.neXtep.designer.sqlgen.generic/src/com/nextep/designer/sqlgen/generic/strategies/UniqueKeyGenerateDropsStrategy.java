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
package com.nextep.designer.sqlgen.generic.strategies;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.exception.TooManyReferencesException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.strategies.DoDropStrategy;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.GenericMessages;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * A strategy which drops dependent foreign keys when this unique key needs to be dropped.
 * 
 * @author Christophe Fondacci
 */
public class UniqueKeyGenerateDropsStrategy extends DoDropStrategy {

	private static final Log log = LogFactory.getLog(UniqueKeyGenerateDropsStrategy.class);

	/**
	 *
	 */
	public UniqueKeyGenerateDropsStrategy() {
		setName("Generate dependent drops");
		setDescription("The generator will generate drops of all known dependent objects");
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.strategies.NoDropStrategy#getId()
	 */
	@Override
	public String getId() {
		return UniqueKeyGenerateDropsStrategy.class.getName();
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.strategies.NoDropStrategy#generateDrop(com.nextep.datadesigner.sqlgen.model.ISQLGenerator,
	 *      java.lang.Object)
	 */
	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop) {
		UniqueKeyConstraint uk = (UniqueKeyConstraint) modelToDrop;
		// Preparing result
		IGenerationResult r = GenerationFactory.createGenerationResult(uk.getName());
		// Retrieving foreign keys

		Collection<ForeignKeyConstraint> repoFks = null;
		Collection<ForeignKeyConstraint> dbFks = null;

		try {
			repoFks = DBGMHelper.getForeignKeys(uk, true);
			dbFks = DBGMHelper.getForeignKeys(uk, false);
		} catch (ReferenceNotFoundException e) {
			log.warn(MessageFormat.format(GenericMessages.getString("UniqueKeyNotFound"),
					uk.getName()));
			return super.generateDrop(generator, modelToDrop);
		} catch (TooManyReferencesException e) {
			log.warn(MessageFormat.format(GenericMessages.getString("ForeignKeyNotFound"), uk
					.getConstrainedTable().getName() + "." + uk.getName()));
			repoFks = new ArrayList<ForeignKeyConstraint>();
			dbFks = new ArrayList<ForeignKeyConstraint>();
		}

		// Generating foreign keys drops / recreation
		ISQLGenerator fkGenerator = getGenerator(IElementType.getInstance("FOREIGN_KEY"));
		for (ForeignKeyConstraint fk : dbFks) {
			// Dropping
			r.integrate(fkGenerator.generateDrop(fk));
		}
		for (ForeignKeyConstraint fk : repoFks) {
			// Recreating
			r.integrate(fkGenerator.generateFullSQL(fk));
		}

		// Using drop strategy
		r.integrate(super.generateDrop(generator, modelToDrop));

		return r;
	}
}
