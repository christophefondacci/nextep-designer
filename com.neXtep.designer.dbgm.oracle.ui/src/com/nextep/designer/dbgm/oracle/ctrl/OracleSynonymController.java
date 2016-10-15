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
package com.nextep.designer.dbgm.oracle.ctrl;

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleSynonymEditorGUI;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleSynonymNavigator;
import com.nextep.designer.dbgm.ui.controllers.SynonymUIController;

/**
 * @author Bruno Gautier
 */
public class OracleSynonymController extends SynonymUIController {

    @Override
    public IDisplayConnector initializeEditor(Object content) {
        return new OracleSynonymEditorGUI((IOracleSynonym)content, this);
    }

    @Override
    public INavigatorConnector initializeNavigator(Object model) {
        return new OracleSynonymNavigator((IOracleSynonym)model, this);
    }

}
