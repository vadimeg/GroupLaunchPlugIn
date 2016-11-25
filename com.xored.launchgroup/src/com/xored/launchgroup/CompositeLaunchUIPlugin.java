package com.xored.launchgroup;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CompositeLaunchUIPlugin extends AbstractUIPlugin { 
//implements ILaunchesListener2 {

	// The plug-in ID
	// It's also used as prefix for launch item persistence (via attributes of the launch configuration)
	public static final String PLUGIN_ID = CompositeLaunchUIPlugin.class.getPackage().getName();
	
	// The shared instance
	private static CompositeLaunchUIPlugin plugin;
	
	private static Shell dialogShell;
	
	/**
	 * The constructor
	 */
	public CompositeLaunchUIPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CompositeLaunchUIPlugin getDefault() {
		return plugin;
	}

	public static Shell getShell() {
		if (getActiveWorkbenchShell() != null) {
			return getActiveWorkbenchShell();
		}
		if (dialogShell != null) {
			if (!dialogShell.isDisposed())
				return dialogShell;
			dialogShell = null;
		}
		IWorkbenchWindow[] windows = getDefault().getWorkbench().getWorkbenchWindows();
		return windows[0].getShell();
	}

	public static void setDialogShell(Shell shell) {
		dialogShell = shell;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public static void errorDialog(String message, IStatus status) {
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			ErrorDialog.openError(shell,
					ResourceMessages.getMessage("Plugin_Error"), message, status); //$NON-NLS-1$			
		}
	}

	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 1, t.getMessage(), null);
			ErrorDialog.openError(shell,
					ResourceMessages.getMessage("Plugin_Error"), message, status); //$NON-NLS-1$ 
		}
	}

//    public void launchesAdded(ILaunch[] launches) {
  //  }

//    public void launchesChanged(ILaunch[] launches) {
  //  }

    //public void launchesRemoved(ILaunch[] launches) {
    //}

    //public void launchesTerminated(ILaunch[] launches) {
    //}

}
