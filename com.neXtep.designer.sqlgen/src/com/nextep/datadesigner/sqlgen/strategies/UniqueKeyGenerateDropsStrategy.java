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
package com.nextep.datadesigner.sqlgen.strategies;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.exception.TooManyReferencesException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.impl.GenerationResult;

/**
 * This drop strategy handles unique key drops by dropping and recreating any foreign keys which
 * refers to this unique key constraint. The generator will perform the following actions :<br>
 * - Drop foreign keys found in <u>database</u> referencing the unique to drop<br>
 * - Drop the unique key constraint<br>
 * - Recreate the foreign keys of the <u>repository</u> referencing this constraint<br>
 * <br>
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
		IGenerationResult r = new GenerationResult(uk.getName());
		// Retrieving foreign keys

		Collection<ForeignKeyConstraint> repoFks = null;
		Collection<ForeignKeyConstraint> dbFks = null;

		try {
			repoFks = DBGMHelper.getForeignKeys(uk, true);
			dbFks = DBGMHelper.getForeignKeys(uk, false);
			augmentForeignKeys(uk, repoFks, dbFks);
		} catch (ReferenceNotFoundException e) {
			log.warn(MessageFormat.format(SQLGenMessages
					.getString("dropStrategy.UniqueKeyNotFound"), uk.getName()));
			return super.generateDrop(generator, modelToDrop);
		} catch (TooManyReferencesException e) {
			log.warn(MessageFormat.format(SQLGenMessages
					.getString("dropStrategy.ForeignKeyNotFound"), uk.getConstrainedTable()
					.getName()
					+ "." + uk.getName()));
			repoFks = new ArrayList<ForeignKeyConstraint>();
			dbFks = new ArrayList<ForeignKeyConstraint>();
		}

		// Generating foreign keys drops / recreation
		ISQLGenerator fkGenerator = GeneratorFactory.getGenerator(IElementType
				.getInstance("FOREIGN_KEY"), generator.getVendor());
		for (ForeignKeyConstraint fk : dbFks) {
			// Dropping
			r.integrate(fkGenerator.generateDrop(fk));
		}
		// Using native drop to drop the constraint
		r.integrate(super.generateDrop(generator, modelToDrop));
		// Recreating foreign keys
		for (ForeignKeyConstraint fk : repoFks) {
			// Recreating
			r.integrate(fkGenerator.generateFullSQL(fk));
		}

		return r;
	}

	/**
	 * Hook method to allow extensions to augment the foreign keys which should be considered in the
	 * drop / recreate actions of this strategy. For example, mysql need to consider the need of
	 * regenerating foreign keys which were previously enforced by this unique key index. Not doing
	 * so would cause the whole drop operation to fail.
	 * 
	 * @param droppedUk unique key being dropped
	 * @param repoFks repository foreign keys to generate
	 * @param dbFks database foreign keys to drop
	 */
	protected void augmentForeignKeys(IKeyConstraint droppedUk,
			Collection<ForeignKeyConstraint> repoFks, Collection<ForeignKeyConstraint> dbFks) {
		// Default behaviour is void
	}
}
