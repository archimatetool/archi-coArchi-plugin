/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelrepository.repository;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;

/**
 * Central manager for notifying changes to repos
 * 
 * @author Phillip Beauvoir
 */
public class RepositoryListenerManager {

    public static final RepositoryListenerManager INSTANCE = new RepositoryListenerManager();
    
    private List<IRepositoryListener> listeners = new ArrayList<IRepositoryListener>();

    public void addListener(IRepositoryListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(IRepositoryListener listener) {
        listeners.remove(listener);
    }

    public void fireRepositoryChangedEvent(String eventName, final IArchiRepository repository) {
        if(repository == null) {
            return;
        }
        
        for(IRepositoryListener listener : listeners) {
            SafeRunner.run(() -> {
                listener.repositoryChanged(eventName, repository);
            });
        }
    }

}
