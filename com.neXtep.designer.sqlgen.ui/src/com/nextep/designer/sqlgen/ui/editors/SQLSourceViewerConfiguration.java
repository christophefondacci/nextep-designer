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
package com.nextep.designer.sqlgen.ui.editors;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.ui.SpecEditorInput;
import com.nextep.designer.sqlgen.ui.editors.sql.SQLCompletionProcessor;
import com.nextep.designer.sqlgen.ui.model.IConnectable;

/**
 * @author Christophe Fondacci
 */
public class SQLSourceViewerConfiguration extends TextSourceViewerConfiguration {

	private SQLScanner sqlScanner;
	private IEditorPart txtEditor;

	/**
	 * Custom constructor to define our text editor used to retrieve the editor input for the
	 * content assist.
	 * 
	 * @param editor text editor initiating the creation.
	 */
	public SQLSourceViewerConfiguration(IEditorPart editor) {
		super();
		this.txtEditor = editor;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, SQLPartitionScanner.PROMPT,
				SQLPartitionScanner.COMMENT, SQLPartitionScanner.SINGLECOMMENT,
				SQLPartitionScanner.STRING };
	}

	static class SingleTokenScanner extends BufferedRuleBasedScanner {

		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));

		}
	}

	public SQLScanner getSQLScanner() {
		if (sqlScanner == null) {
			// TODO Retrieve current vendor for JDBC-based workspace to highlight every vendor
			// specific script
			txtEditor.getEditorInput();
			sqlScanner = new SQLScanner(DBVendor.getDefaultVendor());
		}
		return sqlScanner;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		// Creating damager repairer for SQL
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSQLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		// Creating damager repairer for single line Comments
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(
				SQLAttributeManager.getAttribute(SQLAttributeManager.PROMPT)));
		reconciler.setDamager(dr, SQLPartitionScanner.SINGLECOMMENT);
		reconciler.setRepairer(dr, SQLPartitionScanner.SINGLECOMMENT);

		// Creating damager repairer for Comments
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(
				SQLAttributeManager.getAttribute(SQLAttributeManager.COMMENT)));
		reconciler.setDamager(dr, SQLPartitionScanner.COMMENT);
		reconciler.setRepairer(dr, SQLPartitionScanner.COMMENT);

		// Creating damager repairer for Strings
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(
				SQLAttributeManager.getAttribute(SQLAttributeManager.STRING)));
		reconciler.setDamager(dr, SQLPartitionScanner.STRING);
		reconciler.setRepairer(dr, SQLPartitionScanner.STRING);

		// Creating damager repairer for Prompts
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(
				SQLAttributeManager.getAttribute(SQLAttributeManager.PROMPT)));
		reconciler.setDamager(dr, SQLPartitionScanner.PROMPT);
		reconciler.setRepairer(dr, SQLPartitionScanner.PROMPT);
		return reconciler;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		final ISQLEditorInput<?> input = (ISQLEditorInput<?>) txtEditor.getEditorInput();
		Object completionModel = input.getModel();
		// We get the connection if connected
		if (input instanceof IConnectable) {
			final IConnection connection = ((IConnectable) input).getConnection();
			if (connection != null) {
				completionModel = connection;
			}
		}
		assistant.setContentAssistProcessor(
				new SQLCompletionProcessor(completionModel,
						txtEditor.getEditorInput() instanceof SpecEditorInput),
				IDocument.DEFAULT_CONTENT_TYPE);
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setContextInformationPopupBackground(FontFactory.COMMENT_COLOR);
		assistant.setInformationControlCreator(new IInformationControlCreator() {

			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}

		});
		//
		return assistant;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer,
	 *      java.lang.String)
	 */
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		String partitioning = getConfiguredDocumentPartitioning(sourceViewer);
		if (SQLPartitionScanner.COMMENT.equals(contentType)) {
			return new IAutoEditStrategy[] { new PLDocAutoIndentStrategy(partitioning,
					((ISQLEditorInput) txtEditor.getEditorInput()).getModel()) };
		} else if (SQLPartitionScanner.STRING.equals(contentType)) {
			return new IAutoEditStrategy[] { new StringAutoIndentStrategy(partitioning) };
		} else if (SQLPartitionScanner.SINGLECOMMENT.equals(contentType)) {
			return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
		} else {
			// Retrieving correct parser
			ISQLParser parser = GeneratorFactory.getSQLParser(DBGMHelper.getCurrentVendor());
			Collection<IAutoEditStrategy> strategies = new ArrayList<IAutoEditStrategy>();
			strategies.add(new PLDocAutoIndentStrategy(partitioning, ((ISQLEditorInput) txtEditor
					.getEditorInput()).getModel()));
			strategies.addAll(parser.getAutoEditStrategies());
			// strategies.add(new StringAutoIndentStrategy(partitioning));
			return strategies.toArray(new IAutoEditStrategy[strategies.size()]);
		}

	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return new IHyperlinkDetector[] { new SQLHyperlinkDetector() };
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new SQLTextHtmlHover();
	}
}
