package com.xored.launchgroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Dialog to add launch configuration
 */
@SuppressWarnings("restriction")
public class ConfigurationSelectionDialog extends TitleAreaDialog implements ISelectionChangedListener {
	private ISelection selection;
	private ILaunchGroup[] launchGroups;
	private String mode; // current mode: one of debug/run/profile
	private ViewerFilter filter;
	private IStructuredSelection initialSelection;
	private ConfigurationSelectionComposite composite;

	public ConfigurationSelectionDialog(final Shell shell, final String mode) {
		super(shell);

		launchGroups = DebugUITools.getLaunchGroups();
		this.mode = mode;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		filter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object item) {
				if (item instanceof ILaunchConfigurationType) {
					try {
						ILaunchConfigurationType type = (ILaunchConfigurationType) item;
						return getLaunchManager().getLaunchConfigurations(type).length > 0;
					} catch (CoreException e) {
						return false;
					}
				} else if (item instanceof ILaunchConfiguration) {
					return true;
				}
				return true;
			}
		};
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control x = super.createContents(parent);
		validate();
		setErrorMessage(null);
		return x;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		getShell().setText(ResourceMessages.getMessage("Add_Launch_Configuration"));
		setTitle(ResourceMessages.getMessage("Add_one_or_more_configurations"));

		composite = new ConfigurationSelectionComposite(comp, SWT.NONE);
		HashMap<String, ILaunchGroup> modes = new HashMap<String, ILaunchGroup>();
		for (ILaunchGroup launchGroup : launchGroups) {
			if (!modes.containsKey(launchGroup.getMode())) {
				modes.put(launchGroup.getMode(), launchGroup);
			}
		}
		for (Iterator<String> iterator = modes.keySet().iterator(); iterator.hasNext();) {
			String mode = iterator.next();
			ILaunchGroup launchGroup = modes.get(mode);
			LaunchConfigurationFilteredTree tree = new LaunchConfigurationFilteredTree(
					composite.getStackParent(), SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
					new PatternFilter(), launchGroup, null);
			String label = mode;
			composite.addItem(label, tree);
			tree.createViewControl();
			ViewerFilter[] filters = tree.getViewer().getFilters();
			for (ViewerFilter viewerFilter : filters) {
				if (viewerFilter instanceof LaunchGroupFilter) {
					tree.getViewer().removeFilter(viewerFilter);
				}
			}
			tree.getViewer().addFilter(filter);
			tree.getViewer().addSelectionChangedListener(this);
			if (launchGroup.getMode().equals(this.mode)) {
				composite.setSelection(label);
			}
			if (initialSelection != null) {

				tree.getViewer().setSelection(initialSelection, true);
			}
		}
		composite.pack();
		Rectangle bounds = composite.getBounds();
		GridData data = ((GridData) composite.getLayoutData());
		if (data == null) {
			data = new GridData(GridData.FILL_BOTH);
			composite.setLayoutData(data);
		}
		data.heightHint = Math.max(convertHeightInCharsToPixels(15), bounds.height);
		data.widthHint = Math.max(convertWidthInCharsToPixels(40), bounds.width);

		return comp;
	}

	public ILaunchConfiguration[] getSelectedLaunchConfigurations() {
		List<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>();
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
				Object selection = iter.next();
				if (selection instanceof ILaunchConfiguration) {
					configs.add((ILaunchConfiguration) selection);
				}
			}
		}
		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}

	public static ConfigurationSelectionDialog createDialog(Shell shell, String mode) {
		return new ConfigurationSelectionDialog(shell, mode);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Tree topTree = null;
		final Control topControl = composite.getTopControl();
		if (topControl instanceof FilteredTree) {
			final TreeViewer viewer = ((FilteredTree) topControl).getViewer();
			if (viewer != null) {
				topTree = viewer.getTree();
			}
		}
		if (topTree == null) {
			return;
		}

		boolean selectionIsForVisibleViewer = false;
		final Object src = event.getSource();
		if (src instanceof Viewer) {
			final Control viewerControl = ((Viewer) src).getControl();
			if (viewerControl == topTree) {
				selectionIsForVisibleViewer = true;
			}
		}

		if (!selectionIsForVisibleViewer) {
			return;
		}

		selection = event.getSelection();
		validate();
	}

	protected void validate() {
		Button ok_button = getButton(IDialogConstants.OK_ID);
		boolean isValid = true;
		if (getSelectedLaunchConfigurations().length < 1) {
			setErrorMessage(ResourceMessages.getMessage("Select_launch_configuration"));
			isValid = false;
		} else {
			setErrorMessage(null);
		}

		if (ok_button != null)
			ok_button.setEnabled(isValid);
	}
}
