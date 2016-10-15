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
import com.neXtep.shared.model.SharedDeliveryServices;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VCSFiles;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.model.IRepositoryFile;

public class DeliveryFile extends IDNamedObservable implements
		IDeliveryItem<IRepositoryFile> {

	/** Underlying file */
	private IRepositoryFile file;
	/** Delivery type */
	private DeliveryType dlvType;
	private ArtefactType artefactType;
	private String loader;
	private IDeliveryModule parent;
	private DBVendor vendor; 
	public DeliveryFile(IRepositoryFile file, DeliveryType type, ArtefactType artefactType, String loader, DBVendor vendor) {
		this.file=file;
		this.dlvType=type;
		this.artefactType=artefactType;
		setFileLoader(loader);
		setDBVendor(vendor);
	}
	protected DeliveryFile() {}
	@Override
	public void generateArtefact(String directoryTarget) {
		// Generating file to file system
		VCSFiles.getInstance().generateFile(getContent(), directoryTarget + File.separator + getDeliveryType().getFolderName() + File.separator + getName());
		// Generating any control file
		if(getFileLoader()!=null && !getFileLoader().trim().isEmpty()) { //getArtefactType()==ArtefactType.SQLLOAD && DBGMHelper.getCurrentVendor()==DBVendor.ORACLE) {
			final String controlFilename = SharedDeliveryServices.getControlFileName(getName());
			FileUtils.writeToFile(directoryTarget + File.separator + getDeliveryType().getFolderName()+ File.separator + controlFilename, getFileLoader() );
		}
	}

	@Override
	public String getArtefactName() {
		return file.getName();
	}

	@Override
	public ArtefactType getArtefactType() {
		return artefactType;
	}
	/**
	 * Hibernate setter
	 * @param type
	 */
	protected void setArtefactType(ArtefactType type) {
		this.artefactType = type;
	}
	@Override
	public IRepositoryFile getContent() {
		return file;
	}

	/**
	 * Hibernate content setter
	 * @param file
	 */
	protected void setContent(IRepositoryFile file) {
		this.file = file;
	}
	@Override
	public DeliveryType getDeliveryType() {
		return dlvType;
	}

	@Override
	public void setDeliveryType(DeliveryType type) {
		this.dlvType=type;
	}

	@Override
	public IElementType getType() {
		return file.getType();
	}
	public void setFileLoader(String loader) {
		this.loader = loader;
	}
	public String getFileLoader() {
		return loader;
	}
	@Override
	public String getName() {
		return getContent().getName();
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
