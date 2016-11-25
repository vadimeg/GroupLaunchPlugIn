package com.xored.launchgroup;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ConfigurationSelectionComposite extends Composite {
	private Composite tabArea;
	private Map<String, Composite> tabMap; 
	private StackLayout layout;

	public ConfigurationSelectionComposite(Composite parent, int style) {
		super(parent, style);
		tabMap = new LinkedHashMap<String, Composite>();
		setLayout(new GridLayout(2, false));
		createContents(this);
	}

	public void addItem(String label, Composite tab) {
		tabMap.put(label, tab);
		if (layout.topControl==null) {
			layout.topControl = tab;
		}
	}

	public void setSelection(String label) {
		setPage(label);
	}

	protected void createContents(Composite parent) {
		tabArea = createTabArea(this);
		GridData agd = new GridData(GridData.FILL_BOTH);
		agd.horizontalSpan = 2;
		tabArea.setLayoutData(agd);
	}

	public Composite getStackParent() {
		return tabArea;
	}

	private Composite createTabArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		layout = new StackLayout();
		comp.setLayout(layout);

		return comp;
	}

	private void setPage(String label) {
		layout.topControl = tabMap.get(label);
		getStackParent().layout();
	}
	
	public Control getTopControl() {
		return layout != null ? layout.topControl : null; 
	}
}
