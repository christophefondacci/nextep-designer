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
package com.nextep.designer.beng.model.impl;

import java.io.File;
import com.neXtep.shared.model.ArtefactType;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.core.model.DBVendor;

/**
 * A SQL script delivery artefact.
 * 
 * @author Christophe Fondacci
 */
public class DeliverySQLScript extends IDNamedObservable implements IDeliveryItem<ISQLScript> {

	private ISQLScript script;
	private DeliveryType type;
	private IDeliveryModule parent;
	private DBVendor vendor;
	public DeliverySQLScript(ISQLScript script, DeliveryType type,DBVendor vendor) {
		setContent(script);
		setDeliveryType(type);
		setDBVendor(vendor);
	}
	protected DeliverySQLScript() {}
	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#generateArtefact(java.lang.String)
	 */
	@Override
	public void generateArtefact(String directoryTarget) {
		writeScript(directoryTarget,script);
	}

	private void writeScript(String directory, ISQLScript script) {
		if(script instanceof SQLWrapperScript) {
			for(ISQLScript s : ((SQLWrapperScript)script).getChildren()) {
				writeScript(directory, s);
			}
		}
		FileUtils.writeToFile(directory + File.separator + type.getFolderName() + File.separator + script.getFilename(),script.getSql());
	}
	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getContent()
	 */
	@Override
	public ISQLScript getContent() {
		return script;
	}
	protected void setContent(ISQLScript script) {
		this.script=script;
		setName(script.getName());
	}
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		if(o instanceof IDeliveryItem) {
			return getDeliveryType().compareTo(((IDeliveryItem<?>)o).getDeliveryType());
		}
		return 1;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getDeliveryType()
	 */
	@Override
	public DeliveryType getDeliveryType() {
		return type;
	}
	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("DELIVERY_MODULE"); //$NON-NLS-1$
	}
	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#getName()
	 */
	@Override
	public String getName() {
		return script.getName();
	}
	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#setDeliveryType(com.nextep.designer.beng.model.DeliveryType)
	 */
	@Override
	public void setDeliveryType(DeliveryType type) {
		this.type=type;
	}
	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getArtefactName()
	 */
	@Override
	public String getArtefactName() {
		return script.getFilename();
	}
	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getArtefactType()
	 */
	@Override
	public ArtefactType getArtefactType() {
		return getDeliveryType().getArtefactType();
	}
	@Override
	public IDeliveryModule getParentModule() {
		return parent;
	}
	@Override
	public void setParentModule(IDeliveryModule module) {
		this.parent=module;
	}
	@Override
	public DBVendor getDBVendor() {
		return vendor;
	}
	@Override
	public void setDBVendor(DBVendor vendor) {
		this.vendor=vendor;
	}

}
