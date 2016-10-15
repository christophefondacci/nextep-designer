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
package com.nextep.datadesigner.vcs.ui.compare;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.impl.StringAttribute;

public class PackageCompareEditorInput extends CompareEditorInput {

	private StringAttribute source,target;
	
	private PackageCompareEditorInput(CompareConfiguration conf,StringAttribute source, StringAttribute target) {
		super(conf);
		this.source = source;
		this.target = target;
	}
	public static PackageCompareEditorInput create(StringAttribute source, StringAttribute target, StringAttribute ancestor) {
		CompareConfiguration conf = new CompareConfiguration();
		conf.setLeftLabel("Source");
		conf.setRightLabel("Target");
		conf.setRightEditable(false);
		conf.setLeftEditable(false);
		return new PackageCompareEditorInput(conf,source,target);
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

}
