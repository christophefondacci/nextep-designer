/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.services;

import java.sql.Connection;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.model.IDatabaseTarget;

/**
 * @author Bruno Gautier
 */
public interface IConnectionService {

	Connection connect(IDatabaseTarget target) throws InstallerException;

}
