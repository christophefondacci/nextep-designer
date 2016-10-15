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
package com.neXtep.shared.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class JdbcScriptParser {

	// private static final Log LOGGER =
	// LogFactory.getLog(JdbcScriptParser.class);

	private String sql;
	private int offset = 0;
	private int currentLine = 0;
	private int currentStartLine = 0;
	private final String promptTag;
	private String statementEndTag;
	private final String scriptCallerTag;
	private boolean noCarriage = false;
	private final Collection<Partition> partitions;
	private int beginCount = 0;
	private int caseCount = 0;
	private boolean multilineActive = false;
	private boolean commentStmtActive = false;
	private boolean typeSpecActive = false;
	private boolean typeBodyActive = false;
	private boolean viewStmtActive = false;
	private boolean pckgStmtActive = false;
	private boolean procStmtActive = false;
	private boolean funcStmtActive = false;
	private boolean subQueryStmtActive = false;
	private boolean caseExprActive = false;

	private static class Partition {

		public static final String WORD = "word"; //$NON-NLS-1$
		public static final String SQL = "sql"; //$NON-NLS-1$
		public static final String TEXT = "text"; //$NON-NLS-1$
		public static final String SINGLE_COMMENT = "single_comment"; //$NON-NLS-1$
		public static final String MULTI_COMMENT = "multi_comment"; //$NON-NLS-1$

		private int start, end;
		private String type;

		public Partition() {
		}

		public Partition(String type) {
			this.type = type;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public String getType() {
			return type;
		}
	}

	/**
	 * Creates a new instance of a JDBC script parser for the specified SQL
	 * string to parse and the provided SQL parser.
	 * 
	 * @param sql
	 * @param promptTag
	 * @param statementEndTag
	 * @param scriptCallerTag
	 */
	public JdbcScriptParser(String sql, String promptTag, String statementEndTag,
			String scriptCallerTag) {
		this.sql = sql;
		this.promptTag = promptTag.toUpperCase();
		this.statementEndTag = statementEndTag;
		this.scriptCallerTag = scriptCallerTag == null ? null : scriptCallerTag.toUpperCase();
		offset = 0;
		// this.sql =this.sql.replaceAll("^" +
		// Matcher.quoteReplacement(promptTag) + "(.)*$", "");
		// this.sql = this.sql.replaceAll("(?m)^set (.)*$", "");
		this.sql = this.sql.replaceAll("(?m)^define (.)*$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.sql = this.sql.replaceAll("(?m)^spool (.)*$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.sql = this.sql.replaceAll("(?m)^exit(.)*$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.sql = this.sql.replaceAll("(?m)\\r\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		this.sql = this.sql.replaceAll("(?m)\\r", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		this.statementEndTag = this.statementEndTag.replaceAll("(?m)\\r\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		partitions = getPartitions(this.sql);
	}

	/**
	 * @return the next item parsed from the SQL string
	 * @throws IOException
	 *             in case we had problems reading from the sql string
	 */
	public ParsedItem getNextItem() throws IOException {
		final StringBuffer buf = new StringBuffer(50);
		String line;
		/** Stores the offset before the read start to know what we've read */
		int offsetBeforeRead = offset;
		int offsetBeforeLine = offset;
		currentStartLine = currentLine;
		while ((line = getLine(sql)) != null) {
			currentLine++;
			// If the current line is empty we do not need to parse it, but we
			// must append it to the
			// buffer, otherwise characters index will be wrong for partitions
			// detection.
			if (line.trim().length() == 0) {
				buf.append(line);
				continue;
			}

			final String s = line.trim().toUpperCase();
			if (!multilineActive && s.equals("SHOW ERRORS")) { //$NON-NLS-1$
				resetFlags();
				return getNextItem();
			} else if (!multilineActive && s.startsWith("EXIT") //$NON-NLS-1$
					&& buf.toString().trim().length() == 0) {
				resetFlags();
				return new ParsedItem(ItemType.PROMPT, "Exiting."); //$NON-NLS-1$
			} else if (!multilineActive
					&& (s.startsWith(promptTag) || s.startsWith("--") || s.startsWith("PROMPT")) //$NON-NLS-1$ //$NON-NLS-2$
					&& buf.toString().trim().length() == 0) {
				resetFlags();
				return new ParsedItem(ItemType.PROMPT, line.replaceFirst(
						"^(" + promptTag.replace("\\", "\\\\") + "|--|PROMPT)", "").trim());
			} else if (s.startsWith("DELIMITER")) { //$NON-NLS-1$
				statementEndTag = s.substring("DELIMITER".length() + 1); //$NON-NLS-1$
				statementEndTag = statementEndTag.replace('\r', ' ');
				statementEndTag = statementEndTag.replace('\n', ' ');
				statementEndTag = statementEndTag.trim();
			} else {
				if (scriptCallerTag != null && !"".equals(scriptCallerTag) //$NON-NLS-1$
						&& line.trim().toUpperCase().startsWith(scriptCallerTag)) {
					return new ParsedItem(ItemType.SCRIPT_CALL, line.substring(
							scriptCallerTag.length()).trim());
				} else {
					buf.append(line);
					String currStmtEndTag = statementEndTag;
					int endTagBuffIndex = buf.lastIndexOf(currStmtEndTag);
					boolean wasMultiline = multilineActive;
					if (!isMultiline(line, offsetBeforeLine)
							|| (!";".equals(statementEndTag) && endTagBuffIndex >= 0)) { //$NON-NLS-1$
						/*
						 * If the current statement is not a multiline statement
						 * and no statement separator has been found, or the
						 * last separator found is part of a comment, than try
						 * to find a semi-colon if not current separator.
						 */
						if (!wasMultiline
								&& (endTagBuffIndex < 0 || !Partition.SQL
										.equals(getPartitionType(offsetBeforeRead + endTagBuffIndex)))
								&& !";".equals(statementEndTag)) { //$NON-NLS-1$
							currStmtEndTag = ";"; //$NON-NLS-1$
							endTagBuffIndex = buf.lastIndexOf(currStmtEndTag);
						}

						/*
						 * If a statement delimiter or a semi-colon has been
						 * found in the current statement, and if we not are not
						 * in a multiline statement, then we return the current
						 * statement for execution.
						 */
						if (endTagBuffIndex >= 0
								&& Partition.SQL.equals(getPartitionType(offsetBeforeRead
										+ endTagBuffIndex)) && !multilineActive) {
							resetFlags();
							String stmt = buf.substring(0, endTagBuffIndex).trim();
							if (stmt.length() == 0) {
								return getNextItem();
							}
							return new ParsedItem(ItemType.STATEMENT, stmt);
						}
					}
				}
			}
			offsetBeforeLine = offset;
		}
		if (buf.toString().trim().length() > 0) {
			return new ParsedItem(ItemType.STATEMENT, buf.toString());
		}
		return null;
	}

	private void resetFlags() {
		multilineActive = false;
		commentStmtActive = false;
		typeSpecActive = false;
		typeBodyActive = false;
		viewStmtActive = false;
		pckgStmtActive = false;
		procStmtActive = false;
		funcStmtActive = false;
		subQueryStmtActive = false;
		caseExprActive = false;
		beginCount = 0;
	}

	/**
	 * Checks if the specified line is part of a multiline statement.
	 * 
	 * @param line
	 *            the current line
	 * @param offsetBeforeLine
	 *            number of characters that have been read for the current
	 *            statement before the current line
	 * @return <code>true</code> if the specified line is part of multiline
	 *         statement, <code>false</code> otherwise
	 */
	private boolean isMultiline(String line, int offsetBeforeLine) {
		// Splitting line words
		List<Partition> wordParts = getWordPartitions(line);
		// Analyzing every word
		int partPos = 0;
		String prevWord = null;
		for (Partition part : wordParts) {
			String currWord = line.substring(part.getStart(), part.getEnd());
			String nextWord = null;
			if (partPos + 1 < wordParts.size()) {
				Partition nextPart = wordParts.get(partPos + 1);
				nextWord = line.substring(nextPart.getStart(), nextPart.getEnd()).toUpperCase();
			}

			// Checking word partition
			String wordPartitionType = getPartitionType(offsetBeforeLine + part.getStart());
			if (Partition.SQL.equals(wordPartitionType)) {
				// Converting to upper case for keywords comparisons
				currWord = currWord.toUpperCase();

				if ("DECLARE".equals(currWord)) { //$NON-NLS-1$
					// Declare enables multiline without incrementing counter
					multilineActive = true;
				} else if ("COMMENT".equals(currWord)) { //$NON-NLS-1$
					commentStmtActive = true;
				} else if ("TYPE".equals(currWord)) { //$NON-NLS-1$
					if ("BODY".equals(nextWord)) { //$NON-NLS-1$
						typeBodyActive = true;
					} else {
						typeSpecActive = true;
					}
				} else if ("VIEW".equals(currWord)) { //$NON-NLS-1$
					viewStmtActive = true;
				} else if ("PACKAGE".equals(currWord)) { //$NON-NLS-1$
					pckgStmtActive = true;
				} else if ("PROCEDURE".equals(currWord)) { //$NON-NLS-1$
					procStmtActive = true;
				} else if ("FUNCTION".equals(currWord)) { //$NON-NLS-1$
					funcStmtActive = true;
				} else if ("WITH".equals(currWord) && prevWord == null) { //$NON-NLS-1$
					subQueryStmtActive = true;
				} else if (("IS".equals(currWord) && !("NOT".equals(nextWord) || "NULL" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.equals(nextWord)))
						|| ("AS".equals(currWord) && nextWord == null && !subQueryStmtActive)) { //$NON-NLS-1$
					if (commentStmtActive) {
						commentStmtActive = false;
					} else if (typeSpecActive) {
						typeSpecActive = false;
					} else if (viewStmtActive) {
						viewStmtActive = false;
					} else {
						// BEGIN counter is incremented by one if we are
						// entering in a package
						// statement or user-defined object type body
						if ((pckgStmtActive || typeBodyActive) && !multilineActive) {
							beginCount++;
						}

						// Activate multiline if we are not in a COMMENT, VIEW
						// statement or TYPE
						// specification
						multilineActive = true;
					}
				} else if (("CASE".equals(currWord) && (prevWord == null || !"END".equals(prevWord)))) { //$NON-NLS-1$ //$NON-NLS-2$
					caseExprActive = true;
					caseCount++;
				} else if (("BEGIN".equals(currWord) && !(pckgStmtActive && !funcStmtActive //$NON-NLS-1$
						&& !procStmtActive && beginCount == 1))) {
					/*
					 * BEGIN keyword is ignored when used inside a package but
					 * outside a function or a procedure (aka package
					 * initialization section).
					 */
					multilineActive = true;
					beginCount++;
				} else if ("END".equals(currWord) //$NON-NLS-1$
						&& !("IF".equals(nextWord) || "REPEAT".equals(nextWord) || "LOOP" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.equals(nextWord))) {
					if (caseExprActive) {
						caseCount--;
						if (caseCount == 0) {
							caseExprActive = false;
						}
					} else {
						if (pckgStmtActive && beginCount == 1) {
							pckgStmtActive = false;
						} else if (funcStmtActive
								&& (beginCount == 1 || (pckgStmtActive && beginCount == 2))) {
							funcStmtActive = false;
						} else if (procStmtActive
								&& (beginCount == 1 || (pckgStmtActive && beginCount == 2))) {
							procStmtActive = false;
						}

						beginCount--;
						if (beginCount == 0) {
							multilineActive = false;
						}
					}
				}
			} else if (Partition.SINGLE_COMMENT.equals(wordPartitionType)) {
				// If the current word is part of a single line comment, we
				// don't need to analyze
				// the remaining words as they are also part of the comment.
				return multilineActive;
			}

			partPos++;
			prevWord = currWord;
		}
		return multilineActive;
	}

	/**
	 * Builds a list of word partitions for the given string.
	 * 
	 * @param s
	 *            string to process
	 * @return a list of word partitions
	 */
	private List<Partition> getWordPartitions(String s) {
		final List<Partition> parts = new ArrayList<Partition>();
		final Pattern p = Pattern.compile("[\\w%$@]++"); //$NON-NLS-1$
		final Matcher m = p.matcher(s);

		while (m.find()) {
			Partition part = new Partition(Partition.WORD);
			part.setStart(m.start());
			part.setEnd(m.end());
			parts.add(part);
		}

		// if (LOGGER.isDebugEnabled()) {
		// if (parts.size() > 0) {
		//				LOGGER.debug("[JDBC parser] Line partitions"); //$NON-NLS-1$
		// for (Partition part : parts) {
		//					LOGGER.debug("[JDBC parser] [" + part.getStart() + "," + part.getEnd() + "][" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		//							+ part.getType() + "]: " + s.substring(part.getStart(), part.getEnd())); //$NON-NLS-1$
		// }
		// }
		// }

		return parts;
	}

	/**
	 * @param s
	 *            string to parse
	 * @return the next line of text of the specified string, including the end
	 *         of line terminators
	 */
	private String getLine(String s) {
		if (offset >= s.length())
			return null;
		final int endLine = s.indexOf('\n', offset);
		int endCar = 99999999;
		if (!noCarriage) {
			endCar = s.indexOf('\r', offset);
			if (endCar == -1) {
				noCarriage = true;
			}
		}
		final int end = noCarriage ? endLine : Math.min(endLine, endCar);
		if (end == -1) {
			if (s.length() > offset) {
				final String endString = s.substring(offset);
				offset = s.length();
				return endString;
			}
			return null;
		}
		String line = s.substring(offset, end + 1);
		offset = end + 1;
		if (s.charAt(end) == '\r' && s.length() > end && s.charAt(end + 1) == '\n') {
			offset++;
			line = line + '\n';
		}
		return line;
	}

	/**
	 * Builds a list of partitioned content for the given string.
	 * 
	 * @param s
	 *            string to process
	 * @return
	 */
	private Collection<Partition> getPartitions(String s) {
		final List<Partition> parts = new ArrayList<Partition>();
		// TODO [BGA] Explain in details the regular expression
		final Pattern p = Pattern
				.compile("('([^']++|'')*+')|(--[^\\r\\n]*+)|(/\\*[^+][\\w\\W]*?(?=\\*/)\\*/)"); //$NON-NLS-1$
		final Matcher m = p.matcher(s);

		while (m.find()) {
			Partition part = new Partition();
			part.setStart(m.start());
			part.setEnd(m.end());

			final String matchSequence = m.group();
			if (matchSequence.startsWith("'")) { //$NON-NLS-1$
				part.setType(Partition.TEXT);
			} else if (matchSequence.startsWith("/*")) { //$NON-NLS-1$
				part.setType(Partition.MULTI_COMMENT);
			} else {
				// } else if (matchSequence.startsWith("--")) { //$NON-NLS-1$
				part.setType(Partition.SINGLE_COMMENT);
			}

			parts.add(part);
		}

		// if (LOGGER.isDebugEnabled()) {
		// if (parts.size() > 0) {
		//				LOGGER.debug("[JDBC parser] Script partitions"); //$NON-NLS-1$
		// for (Partition part : parts) {
		//					LOGGER.debug("[JDBC parser] [" + part.getStart() + "," + part.getEnd() + "][" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		//							+ part.getType() + "]: " + s.substring(part.getStart(), part.getEnd())); //$NON-NLS-1$
		// }
		// }
		// }

		return parts;
	}

	/**
	 * Returns a string representing the type of the partition that starts at
	 * the specified offset or encompasses the specified offset. The returned
	 * type must match one of the constants defined in the {@link Partition}
	 * object:
	 * <ul>
	 * <li>Partition#SQL</li>
	 * <li>Partition#TEXT</li>
	 * <li>Partition#SINGLE_COMMENT</li>
	 * <li>Partition#MULTI_COMMENT</li>
	 * </ul>
	 * 
	 * @param offset
	 *            offset
	 * @return a string representing the type of the partition corresponding to
	 *         the specified offset
	 */
	private String getPartitionType(int offset) {
		final Partition part = getPartition(offset);

		if (part != null) {
			return part.getType();
		}

		// Only the text blocks and the comments partitions have been
		// identified, so if we don't
		// find a match, it means the specified offset is in a SQL partition.
		return Partition.SQL;
	}

	/**
	 * Returns the partition that starts at the specified offset or encompasses
	 * the specified offset.
	 * 
	 * @param offset
	 *            offset
	 * @return the partition corresponding to the specified offset,
	 *         <code>null</code> if no partition has been found
	 */
	private Partition getPartition(int offset) {
		for (Partition part : partitions) {
			// If the current partition ends before the specified offset, we
			// move to the next
			// partition.
			if (part.getEnd() < offset) {
				continue;
			}

			// Partitions are sorted in ascending order, therefore if the
			// beginning of the current
			// partition is after the specified offset, the we're done
			// searching.
			if (part.getStart() > offset) {
				return null;
			}

			if (part.getStart() <= offset && part.getEnd() > offset) {
				return part;
			}
		}
		return null;
	}

	/**
	 * @return the currentLine
	 */
	public int getCurrentLine() {
		return currentLine;
	}

	/**
	 * @return the currentStartLine
	 */
	public int getCurrentStartLine() {
		return currentStartLine;
	}
}
