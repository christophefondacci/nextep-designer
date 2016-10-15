package com.nextep.designer.tutorial.ui;

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.designer.tutorial.model.ISynonym;

public class SynonymController extends VersionableController {

	public SynonymController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}
	@Override
	protected IElementType getType() {
		return IElementType.getInstance(ISynonym.TYPE_ID);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new SynonymEditor((ISynonym)content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new SynonymNavigator((ISynonym)model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

}
