package com.xored.launchgroup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class LaunchItemLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof LaunchItem))
			return null;
		if (columnIndex == 0) {
			final LaunchItem item = (LaunchItem) element;
			if (item.data == null) {
				Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				return errorImage;
			}
			
			try {
                String key = item.data.getType().getIdentifier();
                return DebugUITools.getImage(key);
            } catch (CoreException e) {
            	Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				return errorImage;
            }
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof LaunchItem))
			return null;
		final LaunchItem item = (LaunchItem) element;
		
		if (columnIndex == 0) {
			try {
				return item.data.getType().getName();
			} catch (CoreException e) {
				return "";
			}
		} else if (columnIndex == 1) {
			return item.name;
		}

		return null;
	}
}
