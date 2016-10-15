package com.nextep.designer.tutorial.impl.merge;

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.tutorial.model.ISynonym;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

public class SynonymMerger extends Merger {
	
	public static final String ATTR_SYNONYM_FOR = "synonym for";
	

	@Override
	protected IComparisonItem doCompare(IReferenceable source,
			IReferenceable target) {
		ISynonym src = (ISynonym)source;
		ISynonym tgt = (ISynonym)target;
		
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy().getComparisonScope());

		compareName(result, src, tgt);
		result.addSubItem(new ComparisonAttribute(ATTR_SYNONYM_FOR, src == null ? null : src.getSynonymedObject(), tgt == null ? null : tgt.getSynonymedObject()));
		return result;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		ISynonym synonym = (ISynonym)target;

		fillName(result, synonym);
		
		if(synonym.getName() == null) {
			return null;
		}
		
		synonym.setSynonymedObject(getStringProposal(ATTR_SYNONYM_FOR, result));
		return synonym;
	}

}
