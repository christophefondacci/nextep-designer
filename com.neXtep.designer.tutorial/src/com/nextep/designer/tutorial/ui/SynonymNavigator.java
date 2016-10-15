package com.nextep.designer.tutorial.ui;

import com.nextep.datadesigner.ctrl.IGenericController;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.tutorial.model.ISynonym;

public class SynonymNavigator extends TypedNavigator {

	public SynonymNavigator(ISynonym synonym, IGenericController controller) {
		super(synonym,controller);
	}
	
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}

}
