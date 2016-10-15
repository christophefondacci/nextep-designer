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
package com.nextep.designer.ui.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.widgets.Section;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.UIImages;

/**
 * @author Christophe Fondacci
 */
public final class UIHelper {

	private static final Log log = LogFactory.getLog(UIHelper.class);

	/**
	 * Retrieves the currently selected object model or <code>null</code> if no
	 * model selected .
	 * 
	 * @param window
	 *            active window or event window
	 * @return the model object currently being selected
	 */
	@SuppressWarnings("unchecked")
	public static List<?> getSelectedModel(IWorkbenchWindow window) {
		// Retrieving selection service
		if (window == null || window.getSelectionService() == null) {
			return null;
		}
		ISelection sel = window.getSelectionService().getSelection();
		// We only look for a structured non-empty selection
		if (sel instanceof IStructuredSelection) {
			if (sel != null && !sel.isEmpty()) {
				List<Object> selectedModels = new ArrayList<Object>();
				final IStructuredSelection s = (IStructuredSelection) sel;
				final Iterator<?> selIt = s.iterator();
				while (selIt.hasNext()) {
					selectedModels.add(selIt.next());
				}
				return selectedModels;
			}
		} else {
			IWorkbenchPart part = null;
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				part = page.getActivePart();
			}
			if (part instanceof IEditorPart) {
				final IEditorInput input = ((IEditorPart) part).getEditorInput();
				if (input instanceof IModelOriented<?>) {
					return Arrays.asList(((IModelOriented<?>) input).getModel());
				}
			}

		}
		// Any other context returns a null object
		return Collections.EMPTY_LIST;
	}

	/**
	 * Retrieves the currently selected object model or <code>null</code> if
	 * none. If several objects are selected, it will return the first element
	 * of the selection.<br>
	 * We advise you to use the List form of this method for correct multiple
	 * selection handling. This is a convenience method for handlers which do
	 * not support multiple selection and which should only be activated on a
	 * single selection.
	 * 
	 * @param window
	 * @return
	 */
	public static Object getSelectedSingleModel(IWorkbenchWindow window) {
		List<?> multiSelection = getSelectedModel(window);
		if (multiSelection != null && !multiSelection.isEmpty()) {
			return multiSelection.iterator().next();
		} else {
			return null;
		}
	}

	public static IPreferenceStore getPreferenceStore() {
		return CoreUiPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Retrieves the editor extensions for a specific category. Editors may call
	 * this method to instantiate external contributions to the edition UI of an
	 * object.
	 * 
	 * @param model
	 *            model being edited
	 * @param category
	 *            category to retrieve or <code>null</code> for all
	 *            contributions
	 * @return an array of {@link IDisplayConnector} objects, which may be null
	 */
	public static Collection<IDisplayConnector> getEditorExtension(ITypedObject model,
			String category) {
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				"com.neXtep.designer.ui.editorContribution", "typeId", model.getType().getId()); //$NON-NLS-1$ //$NON-NLS-2$
		Collection<IDisplayConnector> connectors = new ArrayList<IDisplayConnector>();
		for (IConfigurationElement elt : elts) {
			if (category == null || category.equals(elt.getAttribute("category"))) { //$NON-NLS-1$
				try {
					IDisplayConnector d = (IDisplayConnector) elt
							.createExecutableExtension("class"); //$NON-NLS-1$
					d.setModel(model);
					connectors.add(d);
				} catch (CoreException e) {
					log.error("Problems while instantiating UI contributions", e); //$NON-NLS-1$
				}
			}
		}
		return connectors;
	}

	/**
	 * Retrieves the shell to instantiate dialogs / UI components from. This
	 * method should be used when no explicit parent could be retrieved and will
	 * return the most appropriate Shell instance to use, avoiding focus
	 * problems.
	 * 
	 * @return the shell to use when no explicit parent shell is known
	 */
	public static Shell getShell() {
		// if (CoreUiPlugin.getDefault().getWorkbench() != null
		// &&
		// CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
		// != null) {
		// return
		// CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		// } else {
		if (Display.getCurrent() == null) {
			return Display.getDefault().getActiveShell();
		} else {
			return Display.getCurrent().getActiveShell();
		}
		// }
	}

	/**
	 * Retrieves the icon corresponding to the specified vendor.
	 * 
	 * @param vendor
	 *            the {@link DBVendor} to retrieve icon for
	 * @return the database vendor icon
	 */
	public static Image getVendorIcon(DBVendor vendor) {
		if (vendor == null) {
			return null;
		}
		// TODO: switch this to extension based contribution
		switch (vendor) {
		case DB2:
			return UIImages.DB2_ICON;
		case DERBY:
			return UIImages.JDBC_ICON;
		case JDBC:
			return UIImages.JDBC_ICON;
		case MYSQL:
			return UIImages.MYSQL_ICON;
		case ORACLE:
			return UIImages.ORACLE_ICON;
		case POSTGRE:
			return UIImages.POSTGRE_ICON;
		case MSSQL:
			return UIImages.MSSQL_ICON;
		default:
			return null;
		}
	}

	/**
	 * Handles the enablement of controls (text, combos, buttons, etc.)
	 * contained in the specified composite based on the locking status of the
	 * specified model.
	 * 
	 * @param parent
	 *            the parent composite of controls to enable / disable
	 * @param model
	 *            the model element which controls the enablement states. If
	 *            this is a lockable then controls will be enabled when unlocked
	 *            and disabled when locked, in any other case it will be enabled
	 */
	public static void handleEnablement(Composite parent, Object model) {
		// Computing the enablement state
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final ILockable<?> lockable = coreService.getLockable(model);
		final boolean enabled = lockable == null ? true : !lockable.updatesLocked();
		setEnablement(parent, enabled);
	}

	/**
	 * Defines the enablement of controls (text, combos, buttons, etc.)
	 * contained in the specified composite based on the given enablement flag
	 * 
	 * @param parent
	 *            the parent composite of controls to enable / disable
	 * @param enabled
	 *            <code>true</code> to enable controls, <code>false</code> to
	 *            disable
	 */
	public static void setEnablement(Composite parent, boolean enabled) {
		// Applying to child controls
		if (!parent.isDisposed()) {
			boolean isFirst = true;
			for (Control c : parent.getChildren()) {
				if ((parent instanceof Section) && (c instanceof Text) && isFirst) {
					isFirst = false;
					continue;
				}
				if (!c.isDisposed()) {
					if (c instanceof Text) {
						((Text) c).setEditable(enabled);
					} else if (c instanceof CCombo) {
						c.setEnabled(enabled);
					} else if (c instanceof Composite) {
						setEnablement((Composite) c, enabled);
					} else if ((c instanceof Label) || (c instanceof Section)
							|| (c instanceof Link)) {
						// Nothing to do
					} else {
						c.setEnabled(enabled);
					}
				} else {
					log.warn("Handling enablement on a disposed child widget : " + c); //$NON-NLS-1$
				}
			}
		} else {
			log.warn("Handling enablement on a disposed widget : " + parent); //$NON-NLS-1$
		}
	}
}
