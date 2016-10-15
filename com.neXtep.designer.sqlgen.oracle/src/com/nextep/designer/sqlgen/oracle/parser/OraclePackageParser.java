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
package com.nextep.designer.sqlgen.oracle.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.ParseData;
import com.nextep.datadesigner.dbgm.impl.ProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.datadesigner.dbgm.model.Variable;
import com.nextep.datadesigner.sqlgen.impl.LightProcedure;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.dbgm.model.ITypedSqlParser;
import com.nextep.designer.dbgm.sql.TextPosition;

public class OraclePackageParser implements ITypedSqlParser {

	private final static Log log = LogFactory.getLog(OraclePackageParser.class);

	@Override
	public IParseData parse(IParseable p, String sql) {
		try {
			return internalParse(p, sql);
		} catch (BadLocationException e) {
			log.warn("Problems during parse", e);
			return null;
		}
	}

	private ParseData internalParse(IParseable p, String sql) throws BadLocationException {
		IDocument doc = createDocument(sql);
		final ParseData parseData = new ParseData();
		IPackage pkg = (IPackage) p;
		pkg.clearVariables();
		pkg.clearProcedures();
		// Reseting
		p.setParsed(false);
		// Parsing block
		PackageScanner pkgScanner = new PackageScanner();
		pkgScanner.setRange(doc, 0, doc.getLength());
		// Parsing procedures
		IToken token = pkgScanner.nextToken();
		int beginCount = 0;
		int procStartOffset = 0;
		/** A flag indicating a package has started */
		boolean packageStarted = false;
		boolean pendingPackage = false;
		/** A flag indicating a procedure declaration has started */
		boolean procedureDecl = false;
		// Procedure declaration parsing variables
		IProcedureParameter currentParam = null;
		boolean paramDefinition = false;
		boolean returnDefinition = false;
		/** The previous token */
		IToken previousToken = null;
		/** currently parsed procedure */
		IProcedure currentProc = null;
		/** Currently parsed variable */
		IVariable currentVar = null;
		/** Pending END */
		boolean pendingEnd = false;
		while (!token.isEOF()) {
			if (token == PackageScanner.PACKAGE_TOKEN) {
				pendingPackage = true;
			} else if (token == PackageScanner.PROC_ENDSPEC_TOKEN) {
				if (pendingPackage) {
					packageStarted = true;
					pendingPackage = false;
				}
				procedureDecl = false;
			} else if (token == PackageScanner.SEMICOLON_TOKEN && procedureDecl) {
				// Here we have a semicolon before the IS / AS => only a pre-declaration without
				// body
				procedureDecl = false;
				currentProc = null;

			} else if (procedureDecl) {
				if (token == PackageScanner.DECLSTART_TOKEN) {
					paramDefinition = true;
				} else if (token == PackageScanner.DECLEND_TOKEN) {
					currentProc.addParameter(currentParam);
					currentParam = null;
					paramDefinition = false;
				} else if (paramDefinition) {
					if (token == PackageScanner.NEWPARAM_TOKEN) {
						currentProc.addParameter(currentParam);
						currentParam = null;
					} else if (previousToken == PackageScanner.ASSIGN_TOKEN) {
						currentParam.setDefaultExpr(doc.get(pkgScanner.getTokenOffset(), pkgScanner
								.getTokenLength()));
					} else if (token == PackageScanner.WORD_TOKEN) {
						String word = doc.get(pkgScanner.getTokenOffset(), pkgScanner
								.getTokenLength());
						if (currentParam == null) {
							currentParam = new ProcedureParameter(word, null, null);
						} else {
							currentParam.setDatatype(new Datatype(word));
						}
					} else if (token.getData() instanceof ParameterType && currentParam != null) {
						currentParam.setParameterType((ParameterType) token.getData());
					}
				} else if (token == PackageScanner.RETURN_TOKEN && !paramDefinition) {
					returnDefinition = true;
				} else if (returnDefinition && token == PackageScanner.WORD_TOKEN) {
					// Here we are defining the return datatype
					if (currentProc.getReturnType() == null) {
						currentProc.setReturnType(new Datatype(doc.get(pkgScanner.getTokenOffset(),
								pkgScanner.getTokenLength())));
					} else {
						IDatatype d = currentProc.getReturnType();
						d
								.setName(d.getName()
										+ " " //$NON-NLS-1$
										+ doc.get(pkgScanner.getTokenOffset(), pkgScanner
												.getTokenLength()));
					}
				}

			} else if (packageStarted && token == PackageScanner.WORD_TOKEN) {
				// If we have a pending 'END' tag, we only check the word to
				// know if it is a closure or a 'end if' like tag.
				// END will always be closed by the SEMICOLON rule
				if (pendingEnd) {
					String word = doc.get(pkgScanner.getTokenOffset(), pkgScanner.getTokenLength());
					if (currentProc != null) {
						if (!word.equals(currentProc.getName())) {
							pendingEnd = false;
						}
					} else {
						if (!word.equals(pkg.getName())) {
							pendingEnd = false;
						}
					}
					// If we have started a proc we initialize it
				} else if (previousToken == PackageScanner.PROC_TOKEN) {
					// We got the proc / func name
					currentProc = new LightProcedure(doc.get(pkgScanner.getTokenOffset(),
							pkgScanner.getTokenLength()), null);
					// Adding procedure
					currentProc.setParent(pkg);
					pkg.addProcedure(currentProc);
					// Starting declaration
					procedureDecl = true;
					currentParam = null;
				} else if (currentVar == null && beginCount == 0) {
					currentVar = new Variable(doc.get(pkgScanner.getTokenOffset(), pkgScanner
							.getTokenLength()));
					if (currentProc == null) {
						pkg.addVariable(currentVar);
					} else {
						currentProc.addVariable(currentVar);
					}
				} else if (beginCount == 0 && currentVar.getDatatypeName() == null) {
					currentVar.setDatatypeName(doc.get(pkgScanner.getTokenOffset(), pkgScanner
							.getTokenLength()));
				}
			} else if (packageStarted && token == PackageScanner.SEMICOLON_TOKEN) {
				if (currentVar != null) {
					parseData.setPosition(currentVar, new TextPosition(pkgScanner.getTokenOffset()
							- currentVar.getName().length(), currentVar.getName().length()));
				}
				currentVar = null;
				if (pendingEnd) {
					beginCount--;
					if (beginCount <= 0 && currentProc != null) {
						// Setting parse data
						parseData.setPosition(currentProc, new TextPosition(procStartOffset,
								pkgScanner.getTokenOffset() + pkgScanner.getTokenLength()
										- procStartOffset));
						// Resetting proc
						currentProc = null;
					}
					pendingEnd = false;
				}
			} else if (token == PackageScanner.PROC_TOKEN) {
				// Nothing, check is made against previous token in a word token
				procStartOffset = pkgScanner.getTokenOffset();
			} else if (token == PackageScanner.BEGIN_TOKEN) {
				beginCount++;
			} else if (token == PackageScanner.END_TOKEN) {
				pendingEnd = true;
			}
			// Always set our previous token and switch to next one
			previousToken = token.isUndefined() || token.isWhitespace() ? previousToken : token;
			token = pkgScanner.nextToken();
		}
		return parseData;
	}

	protected IDocument createDocument(String parsedString) {
		IDocument document = new Document();
		if (parsedString != null) {
			document.set(parsedString.toUpperCase());
		} else {
			document.set(""); //$NON-NLS-1$
		}
		return document;
	}

	@Override
	public String parseName(String sql) {
		String tag = null;
		if (isBody(sql)) {
			tag = "body"; //$NON-NLS-1$
		} else {
			tag = "package"; //$NON-NLS-1$
		}
		// Extracting procedure or function name from the SQL source
		Pattern pattern = Pattern.compile("\\s*(" + tag + ")\\s+((\\w)+)"); //$NON-NLS-1$ //$NON-NLS-2$ 
		Matcher m = pattern.matcher(sql.toLowerCase());
		// Looking for first occurrence
		String parsedName = null;
		if (m.find()) {
			parsedName = sql.substring(m.start(2), m.end(2));
		}
		return parsedName;
	}

	@Override
	public String rename(String sqlToRename, String newName) {
		// Detecting whether we have a spec or a body
		String tag = null;
		if (isBody(sqlToRename)) {
			tag = "body"; //$NON-NLS-1$
		} else {
			tag = "package"; //$NON-NLS-1$
		}
		String renamed = renameSqlHeader(sqlToRename, newName, tag);
		renamed = renameSqlEnd(renamed, newName);
		return renamed;
	}

	/**
	 * Detects whether the supplied SQL string corresponds to the body part or to the spec part
	 * 
	 * @param sql sql to recognize
	 * @return <code>true</code> if this is a body SQL source, <code>false</code> for spec sql
	 *         source
	 */
	private boolean isBody(String sql) {
		final Pattern pattern = Pattern.compile("package(\\s)+body(\\s)+");
		final Matcher m = pattern.matcher(sql.toLowerCase());
		return m.find();
		//		return Pattern.matches("package(\\s)+body(\\s)+", sql.toLowerCase()); //$NON-NLS-1$
	}

	@Override
	public void rename(IParseable parseable, String newName) {
		final IPackage pkg = (IPackage) parseable;

		// Renaming SPEC HEADER package declaration
		String renamedSpec = renameSqlHeader(pkg.getSpecSourceCode(), newName, "package"); //$NON-NLS-1$
		// Renaming SPEC END package declaration
		renamedSpec = renameSqlEnd(renamedSpec, newName);
		pkg.setSpecSourceCode(renamedSpec);
		// Renaming BODY HEADER package declaration
		String renamedBody = renameSqlHeader(pkg.getBodySourceCode(), newName, "body"); //$NON-NLS-1$
		// Renaming BODY END package declaration
		renamedBody = renameSqlEnd(renamedBody, newName);
		pkg.setBodySourceCode(renamedBody);
	}

	/**
	 * Renames the provided SQL string by the new name considering that the name will follow the
	 * supplied token
	 * 
	 * @param sql entire SQL source to rename
	 * @param newName new name to inject in the SQL source
	 * @param firstTokenBeforeName first token before the name information
	 * @return the full SQL, renamed
	 */
	private String renameSqlHeader(String sql, String newName, String firstTokenBeforeName) {
		// Extracting procedure or function name from the SQL source
		Pattern pattern = Pattern.compile("\\s*(" + firstTokenBeforeName + ")\\s+((\\w)+)"); //$NON-NLS-1$ //$NON-NLS-2$
		String lowerCased = sql.toLowerCase();
		Matcher m = pattern.matcher(lowerCased);
		String newSql = sql;
		// Looking for first occurrence
		if (m.find()) {
			// Building the "renamed" SQL source declaration
			newSql = sql.substring(0, m.start(2)) + newName + sql.substring(m.end(2));
		}
		return newSql;
	}

	private String renameSqlEnd(String sql, String newName) {
		// Matching the END tag
		final Pattern pattern = Pattern.compile("end\\s+((\\w)+)(;|/|\\s)*$"); //$NON-NLS-1$
		final Matcher m = pattern.matcher(sql.toLowerCase());
		String newSql = sql;
		if (m.find()) {
			newSql = sql.substring(0, m.start(1)) + newName + sql.substring(m.end(1));
		}
		return newSql;
	}
}
