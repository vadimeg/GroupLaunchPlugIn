package com.xored.launchgroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public abstract class ButtonsPanel extends Composite implements SelectionListener {
	Button addButton;
	Button removeButton;

	public ButtonsPanel(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		addButton = createPushButton(this, "Add...");
		removeButton = createPushButton(this, "Remove");
		setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true));
	}

	protected Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(key);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(data);
		button.addSelectionListener(this);
		return button;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	protected void invalidate() {}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Widget widget = e.widget;
		if (widget == addButton) {
			onAddButtonPressed();
		} else if (widget == removeButton) {
			onRemoveButtonPressed();
		}
	}

	abstract void onAddButtonPressed();
	abstract void onRemoveButtonPressed();
}
