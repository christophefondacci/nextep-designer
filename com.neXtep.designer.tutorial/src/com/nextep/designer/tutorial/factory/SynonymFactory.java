package com.nextep.designer.tutorial.factory;

import com.nextep.designer.tutorial.impl.Synonym;
import com.nextep.designer.tutorial.model.ISynonym;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class SynonymFactory extends VersionableFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new Synonym();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		ISynonym src = (ISynonym)source.getVersionnedObject().getModel();
		ISynonym tgt = (ISynonym)destination.getVersionnedObject().getModel();

		tgt.setSynonymedObject(src.getSynonymedObject());
		
		versionCopy(source, destination);
	}

}
