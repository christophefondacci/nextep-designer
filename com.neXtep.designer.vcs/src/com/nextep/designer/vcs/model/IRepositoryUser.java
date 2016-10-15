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
package com.nextep.designer.vcs.model;

import java.util.Set;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * Represents a repository user.
 * 
 * @author Christophe Fondacci
 */
public interface IRepositoryUser extends INamedObject, IObservable, IdentifiedObject, ITypedObject {

	String TYPE_ID = "USER"; //$NON-NLS-1$

	/**
	 * @return the login of this user
	 */
	String getLogin();

	/**
	 * Defines the new login of this user
	 * 
	 * @param login new user login
	 */
	void setLogin(String login);

	/**
	 * @return whether this user has admin privileges
	 */
	boolean isAdmin();

	/**
	 * Indicates whether this repository user account is enabled or not. Disabled users cannot log
	 * in.
	 * 
	 * @return <code>true</code> is this user is enabled, else <code>false</code>
	 */
	boolean isEnabled();

	/**
	 * Updates the enablement of this user.
	 * 
	 * @param enabled new enablement status of this user
	 */
	void setEnabled(boolean enabled);

	/**
	 * Retrieves the rights for this user
	 * 
	 * @return the collection of all rights of this user
	 */
	Set<UserRight> getUserRights();

	/**
	 * Adds the specified right to this user
	 * 
	 * @param right right to add to this user, no effect if already assigned
	 */
	void addUserRight(UserRight right);

	/**
	 * Revoke a right from a user
	 * 
	 * @param right right to remove
	 */
	void removeUserRight(UserRight right);

	/**
	 * Defines this user password
	 * 
	 * @param password new uncrypted password
	 */
	@Deprecated
	void setPassword(String password);

	/**
	 * @return the password of this user (uncrypted) TODO: secure the password management
	 */
	@Deprecated
	String getPassword();

	/**
	 * Defines the password validation string
	 * 
	 * @param password validation string
	 */
	void setPasswordBis(String password);

	/**
	 * Retrieves the password validation string
	 * 
	 * @return the password
	 */
	String getPasswordBis();

	/**
	 * Defines the encrypted password string for this user
	 * 
	 * @param securedPassword encrypted password
	 */
	void setSecuredPassword(String securedPassword);

	/**
	 * Retrieves the encrypted password string for this user
	 * 
	 * @return the secured password string
	 */
	String getSecuredPassword();
}
