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
package com.nextep.designer.synch.policies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.designer.vcs.exception.ImportFailedException;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A special import policy which boosts the import by not propagating events and not linking any
 * element during import.<br>
 * This special policy is used by the reverse synchronization service when executed for initial view
 * creation.
 * 
 * @author Christophe Fondacci
 */
public class ImportPolicyEmptyView implements IImportPolicy {

	private final static Log log = LogFactory.getLog(ImportPolicyEmptyView.class);

	private Transaction transaction = null;

	@Override
	public boolean importVersionable(IVersionable<?> importing, IVersionContainer targetContainer,
			IActivity activity) {
		try {
			final Session session = HibernateUtil.getInstance().getSession();
			if (transaction == null) {
				transaction = session.beginTransaction();
			}
			// CorePlugin.getIdentifiableDao().save(importing, true);
			targetContainer.getContents().add(importing);
			importing.setContainer(targetContainer);
			session.save(importing);
			return true;
		} catch (Exception e) {
			throw new ImportFailedException(importing, e);
		}
	}

	@Override
	public void finalizeImport() {
		transaction.commit();
		transaction = null;
	}

}
