/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelrepository.propertysections;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

import com.archimatetool.modelrepository.views.repositories.IModelRepositoryTreeEntry;


public class LabelProvider implements ILabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public Image getImage(Object element) {
        if(element instanceof IStructuredSelection) {
            element = ((IStructuredSelection)element).getFirstElement();
        }
        
        if(element instanceof IModelRepositoryTreeEntry) {
            return ((IModelRepositoryTreeEntry)element).getImage();
        }
        
        return null;
    }

    @Override
    public String getText(Object element) {
        if(element instanceof IStructuredSelection) {
            element = ((IStructuredSelection)element).getFirstElement();
        }
        
        if(element instanceof IModelRepositoryTreeEntry) {
            return ((IModelRepositoryTreeEntry)element).getName();
        }
        
        return " "; //$NON-NLS-1$
    }

}
