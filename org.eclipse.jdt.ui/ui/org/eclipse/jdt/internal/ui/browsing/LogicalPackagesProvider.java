/*******************************************************************************
 * Copyright (c) 2000, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.ui.browsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.JavaPlugin;

abstract class LogicalPackagesProvider implements IPropertyChangeListener, IElementChangedListener {

	protected Map fMapToLogicalPackage;
	protected Map fMapToPackageFragments;
	protected boolean fCompoundState;
	protected StructuredViewer fViewer;
	protected boolean fInputIsProject;
	
	public LogicalPackagesProvider(StructuredViewer viewer){
		fViewer= viewer;
		fCompoundState= isInCompoundState();
		fInputIsProject= true;
		fMapToLogicalPackage= new HashMap();
		fMapToPackageFragments= new HashMap();
		JavaPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);	
	}
	
	/**
	 * Adds the given fragments to the internal map.
	 * Existing fragments will be replaced by the new ones. 
	 */
	protected void addFragmentsToMap(IPackageFragment[] children) {
		for (int i= 0; i < children.length; i++) {
			IPackageFragment fragment= children[i];
			String key= getKey(fragment);
			fMapToPackageFragments.put(key, fragment);
		}	
	}

	protected String getKey(IPackageFragment fragment) {
		return fragment.getElementName() + fragment.getJavaProject().getElementName();
	}

	/**
	 * Returns the logical package for the given package fragment
	 * or <code>null</code> if it is not grouped by logical package.
	 * 
	 * @param fragment the package fragment
	 * @return the logical package
	 */
	public LogicalPackage findLogicalPackage(IPackageFragment fragment) {
		Assert.isNotNull(fragment);
		if (isInCompoundState())
			return (LogicalPackage)fMapToLogicalPackage.get(getKey(fragment));
		else
			return null;
	}

	/**
	 * Combines packages with same names into a logical package which will
	 * be added to the resulting array. If a package is not yet in this content
	 * provider then the package fragment is added to the resulting array.
	 */
	protected Object[] combineSamePackagesIntoLogialPackages(IPackageFragment[] children) {

		if (!fCompoundState)
			return children;

		List newChildren= new ArrayList();

		for (int i= 0; i < children.length; i++) {
			IPackageFragment fragment=  children[i];
			
			if (fragment == null)
				continue;
			
			LogicalPackage lp= findLogicalPackage(fragment);

			if (lp != null) {
				if (lp.belongs(fragment)) {
					lp.add(fragment);
				}
				if(!newChildren.contains(lp))
					newChildren.add(lp);
				
			} else {
				String key= getKey(fragment);
				IPackageFragment frag= (IPackageFragment)fMapToPackageFragments.get(key);
				if (frag != null && !fragment.equals(frag)) {
					lp= new LogicalPackage(frag);
					lp.add(fragment);
					newChildren.remove(frag);
					newChildren.add(lp);
					fMapToLogicalPackage.put(key, lp);
					fMapToPackageFragments.remove(frag);
				} else {
					fMapToPackageFragments.put(key, fragment);
					newChildren.add(fragment);
				}
			}
		}
		return newChildren.toArray();
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (fCompoundState == isInCompoundState())
			return;
		else
			fCompoundState= isInCompoundState();
		
		if (!isInCompoundState()) {
			fMapToLogicalPackage.clear();
			fMapToPackageFragments.clear();
		}
		
		if(fViewer instanceof TreeViewer){
			TreeViewer viewer= (TreeViewer) fViewer;
			Object[] expandedObjects= viewer.getExpandedElements();	
			viewer.refresh();
			viewer.setExpandedElements(expandedObjects);
		} else
			fViewer.refresh();
	}

	protected boolean isInCompoundState() {
		// XXX: for now we don't off a preference might become a view menu entry
		//		return AppearancePreferencePage.compoundPackagesInPackagesView();
		return true;

	}
	
	public void dispose(){
		JavaPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		fMapToLogicalPackage= null;
		fMapToPackageFragments= null;
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput != null) {
			JavaCore.addElementChangedListener(this);
		} else {
			JavaCore.removeElementChangedListener(this);
		}
		fInputIsProject= (newInput instanceof IJavaProject);
		
		if(viewer instanceof StructuredViewer)
			fViewer= (StructuredViewer)viewer;
	}
	
	abstract protected void processDelta(IJavaElementDelta delta) throws JavaModelException;

	/*
	 * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		}
	}
	
}
