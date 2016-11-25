package com.xored.launchgroup;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class CompositeContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	protected List<LaunchItem> items;

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		items = null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof List<?>)
			items = (List<LaunchItem>) newInput;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return (parentElement == items) ? items.toArray() : null;
	}

	@Override
	public Object getParent(Object element) {
		return (element == items) ? null : items;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (element == items) ? (items.size() > 0) : false;
	}
}
