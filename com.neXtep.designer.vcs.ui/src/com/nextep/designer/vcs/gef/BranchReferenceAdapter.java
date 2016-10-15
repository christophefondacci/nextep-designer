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
/**
 *
 */
package com.nextep.designer.vcs.gef;

import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IVersionBranch;

/**
 * @author Christophe Fondacci
 *
 */
public class BranchReferenceAdapter implements IReferenceable {

    private IVersionBranch branch;
    private Reference ref;
    public BranchReferenceAdapter(IVersionBranch branch) {
    	this.branch=branch;
    	ref = new Reference(null,branch.getName(),this);
    }
    /**
     * @see com.nextep.datadesigner.model.IReferenceable#getReference()
     */
    @Override
    public IReference getReference() {
        // TODO Auto-generated method stub
        return ref;
    }

    /**
     * @see com.nextep.datadesigner.model.IReferenceable#setReference(com.nextep.datadesigner.impl.Reference)
     */
    @Override
    public void setReference(IReference ref) {
        // TODO Auto-generated method stub

    }


}
