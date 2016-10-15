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
package com.nextep.datadesigner.vcs.impl;

import java.util.HashSet;
import java.util.Set;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.UserRight;

/**
 * @author Christophe Fondacci
 */
public class RepositoryUser extends NamedObservable implements IRepositoryUser {

	// private static final Log log = LogFactory.getLog(RepositoryUser.class);
	private String login;
	private String password, passwordBis, encryptedPassword;
	private boolean isAdmin = false;
	private boolean enabled = true;
	private UID id;
	private Set<UserRight> userRights = new HashSet<UserRight>();

	public RepositoryUser(String login, String password, String name, String description) {
		super();
		this.login = login;
		this.password = password;
		setName(name);
		setDescription(description);
		nameHelper.setFormatter(IFormatter.UPPERCASE);
	}

	public RepositoryUser() {
		super();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IRepositoryUser#getLogin()
	 */
	@Override
	public String getLogin() {
		return login;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IRepositoryUser#setLogin(java.lang.String)
	 */
	@Override
	public void setLogin(String login) {
		if (login != null && !login.equals(getLogin())) {
			this.login = login.toUpperCase();
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	@Override
	public UID getUID() {
		return id;
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	@Override
	public void setUID(UID id) {
		this.id = id;
	}

	protected long getId() {
		if (id == null) {
			return 0;
		}
		return id.rawId();
	}

	protected void setId(long id) {
		this.id = new UID(id);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IRepositoryUser#isAdmin()
	 */
	@Override
	public boolean isAdmin() {
		return getUserRights().contains(UserRight.ADMIN);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		if (login == null || (login != null && "".equals(login.trim()))) { //$NON-NLS-1$
			throw new InconsistentObjectException(VCSMessages.getString("userLoginMustExist")); //$NON-NLS-1$
		}
		if (getSecuredPassword() == null || "".equals(getSecuredPassword().trim())) { //$NON-NLS-1$
			throw new InconsistentObjectException(VCSMessages.getString("userPasswordEmpty")); //$NON-NLS-1$
		}
		if (getSecuredPassword() != null && !getSecuredPassword().equals(getPasswordBis())) {
			throw new InconsistentObjectException(VCSMessages.getString("userPasswordValidation")); //$NON-NLS-1$
		}
		super.checkConsistency();
	}

	@Override
	public Set<UserRight> getUserRights() {
		return userRights;
	}

	/**
	 * Defines the user rights for this user, for hibernate
	 * 
	 * @param rights the collection of all rights for this user
	 */
	protected void setUserRights(Set<UserRight> rights) {
		this.userRights = rights;
	}

	@Override
	public void addUserRight(UserRight right) {
		if (this.userRights.add(right)) {
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void removeUserRight(UserRight right) {
		if (this.userRights.remove(right)) {
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getPasswordBis() {
		return passwordBis;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setPasswordBis(String password) {
		this.passwordBis = password;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public String getSecuredPassword() {
		return encryptedPassword;
	}

	@Override
	public void setSecuredPassword(String securedPassword) {
		final String old = this.encryptedPassword;
		this.encryptedPassword = securedPassword;
		notifyIfChanged(old, securedPassword, ChangeEvent.MODEL_CHANGED);
	}
}
