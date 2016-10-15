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
package com.nextep.designer.sqlgen.ui.views;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.sqlgen.model.IGenerationConsole;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.sqlgen.ui.SQLGenImages;

/**
 * This console is used for displaying SQL scripts results. It is a simple wrapper around an
 * {@link IOConsole} which allows to specify an encoding parameter to display properly outputs for
 * scripts encoded in a non-default encoding.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class GenerationConsole implements IGenerationConsole, IConsole {

	private static final Log LOGGER = LogFactory.getLog(GenerationConsole.class);
	private static final String CONSOLE_NAME = "Generation console"; //$NON-NLS-1$

	private static String encoding = SQLGenUtil
			.getPreference(PreferenceConstants.SQL_SCRIPT_ENCODING);

	private IOConsole console;
	private IOConsoleOutputStream stream = null;

	public GenerationConsole(String name, boolean autoLifecycle) {
		this.console = new IOConsole(name, null,
				ImageDescriptor.createFromImage(SQLGenImages.ICON_CONSOLE), encoding, autoLifecycle);
	}

	public static class GenerationConsoleFactory implements IConsoleFactory {

		@Override
		public void openConsole() {
			showConsole(null);
		}
	}

	public static void showConsole(GenerationConsole console) {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		boolean exists = false;
		if (console != null) {
			IConsole[] existing = manager.getConsoles();
			for (int i = 0; i < existing.length; i++) {
				if (console == existing[i]) {
					exists = true;
				}
			}
		} else {
			console = new GenerationConsole(CONSOLE_NAME, true);
		}
		if (!exists) {
			manager.addConsoles(new IConsole[] { console });
		}
		manager.showConsoleView(console);
	}

	public void log(String text) {
		if (stream == null)
			start();
		if (Designer.isUnitTest()) {
			LOGGER.info("Generation ==> " + text);
		}
		try {
			stream.write(text);
			stream.write("\n"); //$NON-NLS-1$
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public void start() {
		showConsole(this);
		this.stream = console.newOutputStream();
	}

	public void end() {
		try {
			stream.close();
			/*
			 * FIXME [BGA]: This bloc of code has been commented out since setName method is not
			 * accessible to this Wrapper.
			 */
			// Display.getDefault().syncExec(new Runnable() {
			//
			// @Override
			// public void run() {
			// setName(getName() + " <Finished>");
			// }
			// });
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		return console.createPage(view);
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		console.addPropertyChangeListener(listener);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return console.getImageDescriptor();
	}

	@Override
	public String getName() {
		return console.getName();
	}

	@Override
	public String getType() {
		return console.getType();
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		console.removePropertyChangeListener(listener);
	}
}
