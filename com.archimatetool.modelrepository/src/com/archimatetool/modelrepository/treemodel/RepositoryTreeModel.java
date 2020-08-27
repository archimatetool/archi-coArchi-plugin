/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelrepository.treemodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.archimatetool.editor.ArchiPlugin;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.jdom.JDOMUtils;
import com.archimatetool.modelrepository.repository.ArchiRepository;
import com.archimatetool.modelrepository.repository.RepoUtils;

/**
 * Tree Model representing repositories and groups
 * 
 * @author Phillip Beauvoir
 */
public class RepositoryTreeModel extends Group {
    
    /**
     * Backing File
     */
    private File backingFile = new File(ArchiPlugin.INSTANCE.getUserDataFolder(), "repositories.xml"); //$NON-NLS-1$
    
    /**
     * Listeners
     */
    private List<IRepositoryTreeModelListener> listeners = new ArrayList<IRepositoryTreeModelListener>();
    
    private static RepositoryTreeModel instance;
    
    public static RepositoryTreeModel getInstance() {
        if(instance == null) {
            instance = new RepositoryTreeModel();
        }
        
        return instance;
    }
    
    
    private RepositoryTreeModel() {
        super(null);
    }

    /**
     * @return All Repository Refs in this tree model
     */
    public List<RepositoryRef> getAllRepositoryRefs() {
        return getChildRepositoryRefs(this);
    }
    
    private List<RepositoryRef> getChildRepositoryRefs(Group group) {
        List<RepositoryRef> list = group.getRepositoryRefs();
        for(Group child : group.getGroups()) {
            list.addAll(getChildRepositoryRefs(child));
        }
        return list;
    }
    
    /**
     * @param repoLocation
     * @return True if repoLocation is present as a RepositoryRef in this tree model
     */
    public boolean hasRepositoryRef(File repoLocation) {
        return findRepositoryRef(repoLocation) != null;
    }

    /**
     * @param repoLocation
     * @return A RepositoryRef matching repoLocation in this tree model
     */
    public RepositoryRef findRepositoryRef(File repoLocation) {
        if(repoLocation == null) {
            return null;
        }
        
        for(RepositoryRef ref : getAllRepositoryRefs()) {
            if(repoLocation.equals(ref.getArchiRepository().getLocalRepositoryFolder())) {
                return ref;
            }
        }
        
        return null;
    }
    
    public void loadManifest() throws IOException, JDOMException {
        groups = new ArrayList<Group>();
        repos = new ArrayList<RepositoryRef>();
        
        if(backingFile.exists()) {
            Document doc = JDOMUtils.readXMLFile(backingFile);
            if(doc.hasRootElement()) {
                load(doc.getRootElement(), this);
            }
        }
    }
    
    private void load(Element parentElement, Group parentGroup) {
        for(Element groupElement : parentElement.getChildren("group")) { //$NON-NLS-1$
            loadGroup(groupElement, parentGroup);
        }
        
        for(Element refElement : parentElement.getChildren("repository")) { //$NON-NLS-1$
            loadRepositoryRef(refElement, parentGroup);
        }
    }

    private void loadGroup(Element groupElement, Group parentGroup) {
        String name = StringUtils.safeString(groupElement.getAttributeValue("name")); //$NON-NLS-1$
        Group group = new Group(name);
        parentGroup.add(group);
        load(groupElement, group);
    }
    
    private void loadRepositoryRef(Element refElement, Group parentGroup) {
        String location = refElement.getAttributeValue("location"); //$NON-NLS-1$
        if(location != null) {
            File file = new File(location);
            if(RepoUtils.isArchiGitRepository(file)) {
                RepositoryRef ref = new RepositoryRef(new ArchiRepository(file));
                parentGroup.add(ref);
            }
        }
    }
    
    public void saveManifest() throws IOException {
        Document doc = new Document();
        Element rootElement = new Element("repositories"); //$NON-NLS-1$
        doc.setRootElement(rootElement);
        
        save(rootElement, this);
        
        JDOMUtils.write2XMLFile(doc, backingFile);
    }
    
    private void save(Element parentElement, Group parentGroup) {
        for(Group group : parentGroup.getGroups()) {
            saveGroup(parentElement, group);
        }
        
        for(RepositoryRef ref : parentGroup.getRepositoryRefs()) {
            saveRepositoryRef(parentElement, ref);
        }
    }
    
    private void saveGroup(Element parentElement, Group group) {
        Element groupElement = new Element("group"); //$NON-NLS-1$
        groupElement.setAttribute("name", group.getName()); //$NON-NLS-1$
        parentElement.addContent(groupElement);
        
        save(groupElement, group);
    }
    
    private void saveRepositoryRef(Element parentElement, RepositoryRef ref) {
        Element repoElement = new Element("repository"); //$NON-NLS-1$
        repoElement.setAttribute("location", ref.getArchiRepository().getLocalRepositoryFolder().getPath()); //$NON-NLS-1$
        parentElement.addContent(repoElement);
    }
    
    @Override
    public void addListener(IRepositoryTreeModelListener listener) {
        if(listeners != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeListener(IRepositoryTreeModelListener listener) {
        if(listeners != null) {
            listeners.remove(listener);
        }
    }
    
    protected void fireListenerEvent(IModelRepositoryTreeEntry entry) {
        if(listeners != null) {
            for(IRepositoryTreeModelListener listener : listeners) {
                listener.treeEntryChanged(entry);
            }
        }
    }
    
    public void dispose() {
        listeners = null;
        groups = null;
        repos = null;
        instance = null;
    }
}