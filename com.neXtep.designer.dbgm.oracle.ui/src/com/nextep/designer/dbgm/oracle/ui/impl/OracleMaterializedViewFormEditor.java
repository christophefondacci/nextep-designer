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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.oracle.ui.impl;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.designer.dbgm.oracle.model.BuildType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.MaterializedViewType;
import com.nextep.designer.dbgm.oracle.model.RefreshMethod;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.oracle.ui.factories.OracleConverterFactory;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class OracleMaterializedViewFormEditor extends AbstractFormEditor<IMaterializedView> {

	private CCombo timeCombo, methodCombo, typeCombo, buildCombo;
	private Text startText, nextText;
	private Button queryRewriteButton;

	public OracleMaterializedViewFormEditor() {
		super(
				DBOMUIMessages.getString("editor.matView.title"), DBOMUIMessages.getString("editor.matView.desc"), false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		final IMaterializedView view = getModel();

		// Binding refresh time
		IObservableValue widgetValue = WidgetProperties.selection().observe(timeCombo);
		IObservableValue modelValue = PojoProperties
				.value(IMaterializedView.class, "refreshTime").observe(view); //$NON-NLS-1$
		// Converters setup
		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setConverter(OracleConverterFactory
				.createGenericEnumModelConverter(RefreshTime.class));
		UpdateValueStrategy modelTargetStrategy = new UpdateValueStrategy();
		modelTargetStrategy.setConverter(OracleConverterFactory
				.createGenericEnumTargetConverter(RefreshTime.class));
		// Binding
		context.bindValue(widgetValue, modelValue, targetModelStrategy, modelTargetStrategy);

		// Binding refresh method
		widgetValue = WidgetProperties.selection().observe(methodCombo);
		modelValue = PojoProperties.value(IMaterializedView.class, "refreshMethod").observe(view); //$NON-NLS-1$
		// Converters setup
		targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setConverter(OracleConverterFactory
				.createGenericEnumModelConverter(RefreshMethod.class));
		modelTargetStrategy = new UpdateValueStrategy();
		modelTargetStrategy.setConverter(OracleConverterFactory
				.createGenericEnumTargetConverter(RefreshMethod.class));
		// Binding
		context.bindValue(widgetValue, modelValue, targetModelStrategy, modelTargetStrategy);

		// Binding view type
		widgetValue = WidgetProperties.selection().observe(typeCombo);
		modelValue = PojoProperties.value(IMaterializedView.class, "viewType").observe(view); //$NON-NLS-1$
		// Converters setup
		targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setConverter(OracleConverterFactory
				.createGenericEnumModelConverter(MaterializedViewType.class));
		modelTargetStrategy = new UpdateValueStrategy();
		modelTargetStrategy.setConverter(OracleConverterFactory
				.createGenericEnumTargetConverter(MaterializedViewType.class));
		// Binding
		context.bindValue(widgetValue, modelValue, targetModelStrategy, modelTargetStrategy);

		// Binding start expression
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(startText);
		modelValue = PojoProperties.value(IMaterializedView.class, "startExpr").observe(view); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);

		// Binding next expression
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nextText);
		modelValue = PojoProperties.value(IMaterializedView.class, "nextExpr").observe(view); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);

		// Binding build type
		widgetValue = WidgetProperties.selection().observe(buildCombo);
		modelValue = PojoProperties.value(IMaterializedView.class, "buildType").observe(view); //$NON-NLS-1$
		// Converters setup
		targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setConverter(OracleConverterFactory
				.createGenericEnumModelConverter(BuildType.class));
		modelTargetStrategy = new UpdateValueStrategy();
		modelTargetStrategy.setConverter(OracleConverterFactory
				.createGenericEnumTargetConverter(BuildType.class));
		// Binding
		context.bindValue(widgetValue, modelValue, targetModelStrategy, modelTargetStrategy);

		// Binding start expression
		widgetValue = WidgetProperties.selection().observe(queryRewriteButton);
		modelValue = PojoProperties
				.value(IMaterializedView.class, "queryRewriteEnabled").observe(view); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Label lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.refreshTime")); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		timeCombo = new CCombo(parent, SWT.READ_ONLY | SWT.BORDER);
		toolkit.adapt(timeCombo);
		for (RefreshTime time : RefreshTime.values()) {
			timeCombo.add(time.name());
		}

		// Creating refresh method combo
		lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.refreshMethod"), SWT.RIGHT); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		methodCombo = new CCombo(parent, SWT.READ_ONLY | SWT.BORDER);
		toolkit.adapt(methodCombo);
		for (RefreshMethod method : RefreshMethod.values()) {
			methodCombo.add(method.name());
		}
		// Creating view type combo
		lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.viewType"), SWT.RIGHT); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		typeCombo = new CCombo(parent, SWT.READ_ONLY | SWT.BORDER);
		toolkit.adapt(typeCombo);
		for (MaterializedViewType type : MaterializedViewType.values()) {
			typeCombo.add(type.name());
		}

		// Creating start expression text
		lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.startExpr"), SWT.RIGHT); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		startText = toolkit.createText(parent, "", SWT.BORDER); //$NON-NLS-1$
		startText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Creating next expression text
		lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.nextExpr"), SWT.RIGHT); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		nextText = toolkit.createText(parent, "", SWT.BORDER); //$NON-NLS-1$
		nextText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Creating build type combo
		lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.buildType"), SWT.RIGHT); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		buildCombo = new CCombo(parent, SWT.READ_ONLY | SWT.BORDER);
		toolkit.adapt(buildCombo);
		for (BuildType buildType : BuildType.values()) {
			buildCombo.add(buildType.name());
		}

		// Creating query rewrite button
		lbl = toolkit.createLabel(parent,
				DBOMUIMessages.getString("editor.matView.queryRewrite"), SWT.RIGHT); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		queryRewriteButton = toolkit.createButton(parent, "Enabled", SWT.CHECK); //$NON-NLS-1$
	}

	@Override
	protected void doRefresh() {

	}

}
