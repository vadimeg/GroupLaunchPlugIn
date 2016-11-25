package com.xored.launchgroup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class CompositeLaunchDelegate extends LaunchConfigurationDelegate {
	// Attributes used to handle the list of launch item within the parent
	// composite configuration
	private static final String ATTR_NAME = "name"; // launch configuration name
	private static final String ATTR_ENABLED = "enabled"; // "true" if the
															// configuration is
															// checked

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		// Getting the list of launch configurations
		// The first
		Set<ILaunchConfiguration> configurations = getLaunchConfigurationItems(configuration,
				new HashSet<ILaunchConfiguration>(), new HashSet<ILaunchConfiguration>());

		final String taskName = configuration.getName();
		final int numTicks = configurations.size();

		try {
			final SubMonitor launchMonitor = SubMonitor.convert(monitor, taskName, numTicks);
			// Iterate through the list of configurations and launch each of
			// them sequentially
			// TODO: what about launching in parallel?
			configurations.forEach(config -> {
				try {
					config.launch(mode, launchMonitor.newChild(1));
				} catch (CoreException e) {
					CompositeLaunchUIPlugin.log(e);
				}
			});
		} catch (Exception e) {
			CompositeLaunchUIPlugin.log(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected void buildProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	// Don't need to build composite configuration before launching
	// TODO: what about underlying launch configurations?
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		return false;
	}

	// Creating launch items belonging to one common composite launch
	// configuration
	// Each item is a set of attributes within the launch configuration
	// Each attribute starts with PLUGIN_ID prefix to differentiate our
	// properties from other
	// Attributes are persisted
	protected static List<LaunchItem> createLaunchItems(final ILaunchConfiguration configuration,
			List<LaunchItem> items) {
		try {
			Map<?, ?> attrs = configuration.getAttributes();
			for (Iterator<?> iterator = attrs.keySet().iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				try {
					if (attr.startsWith(CompositeLaunchUIPlugin.PLUGIN_ID)) {
						String prop = attr.substring(CompositeLaunchUIPlugin.PLUGIN_ID.length() + 1);
						int dot = prop.indexOf('.');
						String orderNum = prop.substring(0, dot);
						int index = Integer.parseInt(orderNum);
						String name = prop.substring(dot + 1);
						if (name.equals(ATTR_NAME)) {
							LaunchItem item = new LaunchItem();
							item.index = index;
							item.name = (String) attrs.get(attr);
							item.enabled = "true".equals(attrs.get(formatAttribute(index, ATTR_ENABLED)));
							try {
								item.data = getLaunchConfigurationItem(item.name);
							} catch (Exception e) {
								item.data = null;
							}
							while (index >= items.size()) {
								items.add(null);
							}
							items.set(index, item);
						}
					}
				} catch (Exception e) {
					CompositeLaunchUIPlugin.log(e);
				}
			}
		} catch (CoreException e) {
			CompositeLaunchUIPlugin.log(e);
		}
		return items;
	}

	// Returns expanded list of launch configurations to be used for
	// debugging/running within the delegate launch method
	private static Set<ILaunchConfiguration> getLaunchConfigurationItems(final ILaunchConfiguration configuration,
			HashSet<ILaunchConfiguration> items, HashSet<ILaunchConfiguration> groupLaunches) throws CoreException {
		try {
			Map<?, ?> attrs = configuration.getAttributes();
			for (Iterator<?> iterator = attrs.keySet().iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				try {
					if (attr.startsWith(CompositeLaunchUIPlugin.PLUGIN_ID)) {
						String prop = attr.substring(CompositeLaunchUIPlugin.PLUGIN_ID.length() + 1);
						int dot = prop.indexOf('.');
						String name = prop.substring(dot + 1);
						if (name.equals(ATTR_NAME)) {
							ILaunchConfiguration config = getLaunchConfigurationItem((String) attrs.get(attr));
							// If it's composite configuration that may (or may
							// not) contain the list of launch configurations
							// it should be processed recursively adding
							// underlying configurations to the list and
							// preventing composite configuration from adding to
							// the list
							// Also, need to handle possible infinite loop if
							// there are cyclic references within the composite
							// launch configuration (groupLaunches is used for
							// that purpose)
							if (config.getType().getIdentifier().equals(CompositeLaunchUIPlugin.PLUGIN_ID)) {
								if (!groupLaunches.contains(config)) {
									groupLaunches.add(config);
									getLaunchConfigurationItems(config, items, groupLaunches);
								}
							} else {
								items.add(config);
							}
						}
					}
				} catch (Exception e) {
					CompositeLaunchUIPlugin.log(e);
				}
			}
		} catch (CoreException e) {
			CompositeLaunchUIPlugin.log(e);
		}
		return items;
	}

	protected static ILaunchConfiguration getLaunchConfigurationItem(final String name) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations();
		for (int i = 0; i < launchConfigurations.length; i++) {
			ILaunchConfiguration configuration = launchConfigurations[i];
			if (configuration.getName().equals(name))
				return configuration;
		}
		return null;
	}

	//
	protected static void persistLaunchItems(final ILaunchConfigurationWorkingCopy configuration,
			final List<LaunchItem> items) {
		int i = 0;
		deleteLaunchItems(configuration);
		for (LaunchItem item : items) {
			if (item == null)
				continue;
			configuration.setAttribute(formatAttribute(i, ATTR_NAME), item.name);
			configuration.setAttribute(formatAttribute(i, ATTR_ENABLED), item.enabled + "");
			i++;
		}
	}

	protected static void deleteLaunchItems(final ILaunchConfigurationWorkingCopy configuration) {
		try {
			Map<?, ?> attrs = configuration.getAttributes();
			for (Iterator<?> iterator = attrs.keySet().iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				try {
					if (attr.startsWith(CompositeLaunchUIPlugin.PLUGIN_ID)) {
						configuration.removeAttribute(attr);
					}
				} catch (Exception e) {
					CompositeLaunchUIPlugin.log(e);
				}
			}
		} catch (CoreException e) {
			CompositeLaunchUIPlugin.log(e);
		}
	}

	private static String formatAttribute(final int index, final String attribute) {
		return CompositeLaunchUIPlugin.PLUGIN_ID + "." + index + "." + attribute;
	}
}
