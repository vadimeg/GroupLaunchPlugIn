package com.xored.launchgroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class CompositeLaunchTab extends AbstractLaunchConfigurationTab {
	protected CheckboxTreeViewer tree;
	protected List<LaunchItem> items = new ArrayList<LaunchItem>();
	private String mode;
	
	public CompositeLaunchTab(final String mode) {
        this.mode = mode;
    }
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(2, false));
		tree = new CheckboxTreeViewer(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Tree table = tree.getTree();
		table.setFont(parent.getFont());
		tree.setContentProvider(new CompositeContentProvider());
		tree.setLabelProvider(new LaunchItemLabelProvider());
		tree.setCheckStateProvider(new ICheckStateProvider() {
			public boolean isChecked(final Object item) {
				return (item instanceof LaunchItem) ? ((LaunchItem)item).enabled : false;
			}
			public boolean isGrayed(Object element) { return false; }
		});

		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TreeColumn column1 = new TreeColumn(table, SWT.NONE);
		column1.setText(ResourceMessages.getMessage("Type"));
		column1.setWidth(300);
		TreeColumn column2 = new TreeColumn(table, SWT.NONE);
		column2.setText(ResourceMessages.getMessage("Name"));
		column2.setWidth(200);
		tree.setInput(items);

		final ButtonsPanel buttonsPanel = new ButtonsPanel(comp) {
			@Override
			protected void onAddButtonPressed() {
				ConfigurationSelectionDialog dialog = 
					ConfigurationSelectionDialog.createDialog(
							tree.getControl().getShell(), mode);
				if (dialog.open() == Dialog.OK) {
					ILaunchConfiguration[] configurations = dialog.getSelectedLaunchConfigurations();
					if (configurations.length < 1) 
						return;
					for (ILaunchConfiguration configuration : configurations) {
						LaunchItem item = new LaunchItem();
						items.add(item);
						item.index = items.size() - 1;
						item.enabled = true;
						item.name = configuration.getName();
						item.data = configuration;
						tree.refresh(true);
						tree.setChecked(item, item.enabled);
					}
					invalidate();
					updateLaunchConfigurationDialog();
				}
			}
			
			@Override
			protected void onRemoveButtonPressed() {
				int[] indices = getSelectedIndices();
				if (indices.length < 1)
					return;
				for (int i = indices.length - 1; i >= 0; i--) {
					items.remove(indices[i]);
				}
				tree.refresh(true);
				invalidate();
				updateLaunchConfigurationDialog();
			}

			private int[] getSelectedIndices() {
				StructuredSelection sel = (StructuredSelection) tree.getSelection();
				List<Integer> indices = new ArrayList<Integer>();
				
				for (Iterator<?> iter = sel.iterator(); iter.hasNext(); ) {
					LaunchItem el = (LaunchItem)iter.next();
					indices.add(items.indexOf(el));
					
				}
				int[] result = new int[indices.size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = indices.get(i);
				}
				return result;
			}

			@Override
			protected void invalidate() {
				removeButton.setEnabled(((StructuredSelection)tree.getSelection()).size() > 0);
			}
		};

		tree.addCheckStateListener(new ICheckStateListener(){
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				((LaunchItem)event.getElement()).enabled = event.getChecked();
				updateLaunchConfigurationDialog();
			}
		});
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				buttonsPanel.invalidate();
			}
		});

		buttonsPanel.invalidate();
	}

	@Override
	public String getName() {
		return ResourceMessages.getMessage("Configurations"); 
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		items = CompositeLaunchDelegate.createLaunchItems(configuration, new ArrayList<LaunchItem>());
		if (tree != null) {
			tree.setInput(items);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		CompositeLaunchDelegate.persistLaunchItems(configuration, items);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		setMessage(ResourceMessages.getMessage("SELECT_ONE_OR_MORE_CONFIGURATIONS"));
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		int count = 0;
		for (LaunchItem item : items) {
			if (item.enabled) { 
				if ( item.data == null) {
					setErrorMessage(ResourceMessages.getFormattedMessage("LAUNCH_DOES_NOT_EXIST", item.name));
					return false;
				}
				count++;
			} 
		}
		if (count < 1) {
			setErrorMessage(ResourceMessages.getMessage("MUST_HAVE_AT_LEAST_ONE_CONFIGURATION")); 
			return false;				
		}
		return true;
	}
}
