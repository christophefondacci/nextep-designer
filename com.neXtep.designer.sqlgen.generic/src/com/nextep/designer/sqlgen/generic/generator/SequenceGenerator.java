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
package com.nextep.designer.sqlgen.generic.generator;

import java.math.BigDecimal;

import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.sql.ISequenceDialect;
import com.nextep.designer.dbgm.sql.impl.SequenceDialect;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public class SequenceGenerator extends SQLGenerator {

	protected static final String INDENT = "    "; //$NON-NLS-1$

	private ISequenceDialect seqDialect;

	/**
	 * The sequence dialect is initialized by this constructor. Vendor specific
	 * implementations must set their own seqDialect if they need to override
	 * the keyword value of some clauses or if they do not support some clause.
	 */
	public SequenceGenerator() {
		setSeqDialect(new SequenceDialect());
	}

	protected void setSeqDialect(ISequenceDialect dialect) {
		this.seqDialect = dialect;
	}

	protected ISequenceDialect getSeqDialect() {
		return seqDialect;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ISequence seq = (ISequence) model;
		final String seqName = getName(seq);

		ISQLScript additionScript = new SQLScript(seqName, seq.getDescription(),
				getSQLCommandWriter().promptMessage("Creating sequence '" + seqName + "'..."), //$NON-NLS-1$ //$NON-NLS-2$
				ScriptType.SEQ);

		additionScript.appendSQL("CREATE SEQUENCE ").appendSQL(seqName); //$NON-NLS-1$

		appendGenericAttributesFull(additionScript, seq);

		additionScript.appendSQL(getSQLCommandWriter().closeStatement());

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(seq.getType(), seqName), additionScript);

		return genResult;
	}

	protected void appendGenericAttributesFull(ISQLScript script, ISequence seq) {
		appendValueClause(script, getSeqDialect().getStartWithClause(), seq.getStart());
		appendValueClause(script, getSeqDialect().getIncrementByClause(), seq.getIncrement());

		if (seq.getMinValue() != null) {
			appendValueClause(script, getSeqDialect().getMinValueClause(), seq.getMinValue());
		} else {
			appendNoValueClause(script, getSeqDialect().getNoMinValueClause());
		}

		if (seq.getMaxValue() != null) {
			appendValueClause(script, getSeqDialect().getMaxValueClause(), seq.getMaxValue());
		} else {
			appendNoValueClause(script, getSeqDialect().getNoMaxValueClause());
		}

		appendNoValueClause(script, (seq.isCycle() ? getSeqDialect().getCycleClause()
				: getSeqDialect().getNoCycleClause()));

		if (seq.isCached()) {
			appendValueClause(script, getSeqDialect().getCacheClause(), seq.getCacheSize());
		} else {
			appendNoValueClause(script, getSeqDialect().getNoCacheClause());
		}

		appendNoValueClause(script, (seq.isOrdered() ? getSeqDialect().getOrderClause()
				: getSeqDialect().getNoOrderClause()));
	}

	private <T extends Number> void appendValueClause(ISQLScript script, String clause, T value) {
		if (clause != null && value != null) {
			script.appendSQL(NEWLINE).appendSQL(INDENT).appendSQL(clause).appendSQL(" "); //$NON-NLS-1$

			if (value instanceof BigDecimal) {
				script.appendSQL((BigDecimal) value);
			} else if (value instanceof Integer) {
				script.appendSQL((Integer) value);
			} else if (value instanceof Long) {
				script.appendSQL((Long) value);
			}
		}
	}

	private void appendNoValueClause(ISQLScript script, String clause) {
		if (clause != null) {
			script.appendSQL(NEWLINE).appendSQL(INDENT).appendSQL(clause);
		}
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ISequence seq = (ISequence) model;
		final String seqName = seq.getName();

		ISQLScript dropScript = new SQLScript(seqName, seq.getDescription(), getSQLCommandWriter()
				.promptMessage("Dropping sequence '" + seqName + "'..."), ScriptType.SEQ); //$NON-NLS-1$ //$NON-NLS-2$

		dropScript.appendSQL("DROP SEQUENCE ").appendSQL(seqName); //$NON-NLS-1$
		dropScript.appendSQL(getSQLCommandWriter().closeStatement());

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(seq.getType(), seqName), dropScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final ISequence old = (ISequence) result.getTarget();
		final ISequence seq = (ISequence) result.getSource();

		IGenerationResult genResult = null;
		if (isAlterable(old, seq)) {
			final String seqName = seq.getName();
			ISQLScript updateScript = new SQLScript(seqName, seq.getDescription(),
					getSQLCommandWriter().promptMessage("Altering sequence '" + seqName + "'..."), //$NON-NLS-1$ //$NON-NLS-2$
					ScriptType.SEQ);

			updateScript.appendSQL("ALTER SEQUENCE ").appendSQL(seqName); //$NON-NLS-1$

			final BigDecimal start = seq.getStart();
			if (start != null && !start.equals(old.getStart())) {
				appendValueClause(updateScript, getSeqDialect().getRestartWithClause(), start);
			}

			final Long increment = seq.getIncrement();
			if (increment != null && !increment.equals(old.getIncrement())) {
				appendValueClause(updateScript, getSeqDialect().getIncrementByClause(), increment);
			}

			final BigDecimal minValue = seq.getMinValue();
			if (minValue != null && !minValue.equals(old.getMinValue())) {
				appendValueClause(updateScript, getSeqDialect().getMinValueClause(), minValue);
			} else if (null == minValue && old.getMinValue() != null) {
				appendNoValueClause(updateScript, getSeqDialect().getNoMinValueClause());
			}

			final BigDecimal maxValue = seq.getMaxValue();
			if (maxValue != null && !maxValue.equals(old.getMaxValue())) {
				appendValueClause(updateScript, getSeqDialect().getMaxValueClause(), maxValue);
			} else if (null == maxValue && old.getMaxValue() != null) {
				appendNoValueClause(updateScript, getSeqDialect().getNoMaxValueClause());
			}

			if (seq.isCycle() ^ old.isCycle()) {
				appendNoValueClause(updateScript, (seq.isCycle() ? getSeqDialect().getCycleClause()
						: getSeqDialect().getNoCycleClause()));
			}

			final Boolean isCached = seq.isCached();
			if ((isCached != null && (!isCached.equals(old.isCached()) || seq.getCacheSize() != old
					.getCacheSize())) || (null == isCached && old.isCached() != null)) {
				if (isCached) {
					appendValueClause(updateScript, getSeqDialect().getCacheClause(),
							seq.getCacheSize());
				} else {
					appendNoValueClause(updateScript, getSeqDialect().getNoCacheClause());
				}
			}

			if (seq.isOrdered() ^ old.isOrdered()) {
				appendNoValueClause(updateScript, (seq.isOrdered() ? getSeqDialect()
						.getOrderClause() : getSeqDialect().getNoOrderClause()));
			}

			updateScript.appendSQL(NEWLINE).appendSQL(getSQLCommandWriter().closeStatement());

			genResult = GenerationFactory.createGenerationResult();
			genResult.addUpdateScript(new DatabaseReference(seq.getType(), seqName), updateScript);
		} else {
			// Dropping and re-creating the sequence
			genResult = doDrop(old);
			genResult.integrate(generateFullSQL(seq));
		}

		return genResult;
	}

	/**
	 * Checks if the specified old sequence can be altered to match the
	 * specified new sequence's attributes. This method is called by the
	 * {@link #generateDiff(IComparisonItem)} method to check if it should
	 * generate a DROP/CREATE statements sequence rather than a single ALTER
	 * statement.<br>
	 * By default, the old sequence is always considered as alterable.<br>
	 * Vendor specific implementations should override this method if the
	 * modification of some attributes cannot be done with an ALTER statement.
	 * 
	 * @param oldSeq
	 *            the old {@link ISequence} object
	 * @param newSeq
	 *            the new {@link ISequence} object
	 * @return <code>true</code> if the specified old sequence can be changed by
	 *         an ALTER statement to match the specified new sequence's
	 *         attributes, <code>false</code> otherwise
	 */
	protected boolean isAlterable(ISequence oldSeq, ISequence newSeq) {
		return true;
	}

}
