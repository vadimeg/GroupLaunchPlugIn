package com.xored.launchgroup;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CompositeLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { new CompositeLaunchTab(mode) };
		setTabs(tabs);
	}
}
