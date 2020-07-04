/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelrepository;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@SuppressWarnings("nls")
public class GitHelper {

    public static Repository createNewRepository(File localPath) throws IOException {
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();
        return repository;
    }
    
    public static File getTempTestsFolder() {
        File file = new File(System.getProperty("java.io.tmpdir"), "com.archimatetool.modelrepository.tests.tmp");
        file.deleteOnExit();
        file.mkdirs();
        return file;
    }
    

}

