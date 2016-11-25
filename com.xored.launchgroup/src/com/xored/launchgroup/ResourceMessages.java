package com.xored.launchgroup;

public final class ResourceMessages {
	private static final java.util.ResourceBundle RESOURCE_BUNDLE =
			java.util.ResourceBundle.getBundle("com.xored.launchgroup.ResourceMessages"); //$NON-NLS-1$

	private ResourceMessages() { } // Don't create!

	public static String getMessage(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (java.util.MissingResourceException e) {
			return key;
		}
	}
	
	public static String getFormattedMessage(String key, Object ... arguments) {
		return java.text.MessageFormat.format(getMessage(key), arguments);
	}
}
