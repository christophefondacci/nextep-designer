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
///**
// *
// */
//package com.nextep.datadesigner.sqlgen.ctrl;
//
//import com.nextep.datadesigner.gui.model.IDesignerGUI;
//import com.nextep.datadesigner.gui.model.InvokableController;
//import com.nextep.datadesigner.sqlgen.gui.CaptureGUI;
//import com.nextep.designer.vcs.model.IVersionContainer;
//
///**
// * Controller of the capture database schema dialog box.
// *
// * @author Christophe Fondacci
// *
// */
//public class CaptureController extends InvokableController {
//
//	private static CaptureController instance = null;
//	private CaptureController() {}
//	public static CaptureController getInstance() {
//		if(instance == null) {
//			instance = new CaptureController();
//		}
//		return instance;
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.InvokableController#invoke(java.lang.Object)
//	 */
//	@Override
//	public Object invoke(Object... model) {
//		//Initializing capture dialog window
//		IDesignerGUI gui = new CaptureGUI((IVersionContainer)model[0]);
//		invokeGUI(gui);
//		return null;
//	}
//
//}
