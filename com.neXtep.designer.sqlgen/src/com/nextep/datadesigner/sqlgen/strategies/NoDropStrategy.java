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

import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * A drop strategy which does nothing.
 * 
 * @author Christophe Fondacci
 */
public class NoDropStrategy extends NamedObservable implements IDropStrategy {

	private DBVendor vendor;
	private boolean isDefault = false;
	protected String NEWLINE;

	public NoDropStrategy() {
		setName("Do Nothing");
		setDescription("Will simply ignore any drop which might be performed by the generator.");
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IDropStrategy#generateDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop,
			DBVendor vendor) {
		setVendor(vendor);
		ISQLScript s = new SQLScript("drop", "", "-- Disabled DROP of "
				+ NameHelper.getQualifiedName(modelToDrop) + NEWLINE + NEWLINE, ScriptType.DROP);
		IGenerationResult r = GenerationFactory.createGenerationResult();

		IElementType type = null;
		String name = "<Unknown name>";
		if (modelToDrop instanceof ITypedObject) {
			type = ((ITypedObject) modelToDrop).getType();
		}
		if (modelToDrop instanceof INamedObject) {
			name = getName(modelToDrop);
		}
		r.addDropScript(new DatabaseReference(type, name), s);
		return r;
	}

	/**
	 * A method providing the name of our object. The default implementation will try to cast the
	 * model as a {@link INamedObject} and will return the result of calling
	 * {@link INamedObject#getName()}.<br>
	 * Extension should override to customize naming options.
	 * 
	 * @param model model to retrieve the name of
	 * @return the name string
	 */
	protected String getName(Object model) {
		return ((INamedObject) model).getName();
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IDropStrategy#getId()
	 */
	@Override
	public String getId() {
		return "com.neXtep.designer.sqlgen.containerNoDropStrategy"; //$NON-NLS-1$
	}

	/*
	 * @see com.nextep.datadesigner.sqlgen.model.IDropStrategy#isDefault()
	 */
	@Override
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IDropStrategy#setDefault(boolean)
	 */
	@Override
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IDropStrategy#isDropping()
	 */
	@Override
	public boolean isDropping() {
		return false;
	}

	@Override
	public final DBVendor getVendor() {
		return vendor;
	}

	@Override
	public final void setVendor(DBVendor vendor) {
		this.vendor = vendor;
	}

	protected ISQLGenerator getGenerator(IElementType type) {
		return GeneratorFactory.getGenerator(type, getVendor());
	}
}
