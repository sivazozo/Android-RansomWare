package fr.esgi.ransomware.file_reader;

import java.io.File;

import fr.esgi.ransomware.Util;

public class FileFinderRescue extends FileFinder {

    public FileFinderRescue() {
        super();
    }

    @Override
    public boolean isFileMatch(File file) {
        return file.getName().startsWith(Util.PREFIX_FILE_REKT);
    }
}
