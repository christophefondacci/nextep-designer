package com.nextep.designer.sqlgen.postgre.generator;


import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

public class PostGreIndexGenerator extends SQLGenerator {
	

	public PostGreIndexGenerator(){

	}
	
	@Override
	public IGenerationResult doDrop(Object model) {
		final IIndex index = (IIndex) model;
		final String indexName = index.getIndexName();
		final ISQLScript dropScript = getSqlScript(indexName, index.getDescription(),
				ScriptType.INDEX);

		dropScript.appendSQL(prompt("Dropping index '" + indexName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("DROP INDEX ").appendSQL(escape(indexName)); //$NON-NLS-1$ //$NON-NLS-2$
				
		closeLastStatement(dropScript);

		final IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult
				.addDropScript(new DatabaseReference(index.getType(), index.getName()), dropScript);

		return genResult;
	}
	
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IGenerationResult r = GenerationFactory.createGenerationResult();
		r.integrate(doDrop(result.getTarget()));
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}
	
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IIndex index = (IIndex) model;		
		IBasicTable t = index.getIndexedTable();
		ISQLScript s = new SQLScript(index.getIndexName(), index.getDescription(), "",
				ScriptType.INDEX);
		s.appendSQL("\\echo Creating index '" + index.getIndexName() + "'..." + NEWLINE);
		s.appendSQL("CREATE ");
		switch (index.getIndexType()) {
		case UNIQUE:	
				s.appendSQL("UNIQUE ");	
			break;
		}
		s.appendSQL("INDEX " + index.getIndexName() + " ON ");
		
		s.appendSQL(t.getName() + " " + NEWLINE);
		
		switch (index.getIndexType()) {
		case HASH:	
				s.appendSQL("USING HASH " + NEWLINE);	
				
			break;
		case GIN:	
			s.appendSQL("USING GIN "  + NEWLINE);	
		
			break;
		case GIST:	
			s.appendSQL("USING GIST "  + NEWLINE);	
			
			break;
		default:
			s.appendSQL("USING BTREE " + NEWLINE);	
			
		}
		s.appendSQL("(" + NEWLINE);
		boolean first = true;
		for (IBasicColumn c : index.getColumns()) {
			s.appendSQL("    ");
			if (!first) {
				s.appendSQL(",");
			} else {
				s.appendSQL(" ");
				first = false;
			}
			// Function based index generate function source here instead of name
			String colExpr = index.getFunction(c.getReference());
			if (colExpr != null && !colExpr.trim().isEmpty()) {
				s.appendSQL(colExpr + NEWLINE);
			} else {
				s.appendSQL(escape(c.getName()) + NEWLINE);
			}
			
		}
		s.appendSQL(")" + NEWLINE);
		closeLastStatement(s);
		// Generating result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(IElementType.getInstance(IIndex.INDEX_TYPE),
				index.getName()), s);
		// Adding a table precondition
		r.addPrecondition(new DatabaseReference(t.getType(), t.getName()));
		return r;
	}
	

}
