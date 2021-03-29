package com.efimchick.ifmo.io.filetree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileTreeImpl implements FileTree {

    private static final String SPACE = " ";
    private static final String BYTES = "bytes";
    private static final String NODE = "├─ ";
    private static final String INDENT = "│  ";
    private static final String ANGLE = "└─ ";
    private static final int ROOT_DIRECTORY = 1;
    private static final String NEW_LINE = "\n";
    private Path rootPath;

    @Override
    public Optional<String> tree(Path path) {
        rootPath = path;
        StringBuilder builder = new StringBuilder();
        if (path != null && new File(String.valueOf(path)).isDirectory()) {
            try {
                List<File> catalog = getCatalog(path);
                catalog.forEach(file -> builder.append(printTree(path, file)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Optional.of(builder.toString());
        }
        if (path != null && new File(String.valueOf(path)).isFile()) {
            return getOptionalResult(path);
        } else return Optional.empty();
    }

    private StringBuilder printTree(Path path, File file) {
        StringBuilder builder = new StringBuilder();
        if (file.isDirectory() && isRootDirectory(path, file)) {
            builder.append(getDirectoryWithBytes(path, file));
            builder.append(getFiles(file));
        }
        return builder;
    }

    private StringBuilder getFiles(File file) {
        StringBuilder builder = new StringBuilder();
        List<File> directoryFiles = getDirectoryFiles(file);

        directoryFiles.forEach(directoryFile -> {
            builder.append(getSubDirectory(directoryFiles, directoryFile));
            builder.append(getLastSubDirectory(directoryFiles, directoryFile));
        });
        directoryFiles.forEach(directoryFile -> {
            builder.append(getFile(directoryFiles, directoryFile));
            builder.append(getLastFile(directoryFiles, directoryFile));
        });
        return builder;
    }

    private StringBuilder getSubDirectory(List<File> directoryFiles, File directoryFile) {
        StringBuilder builder = new StringBuilder();
        if (directoryFile.isDirectory() && !isLastFile(directoryFiles, directoryFile)) {
            try {
                builder.append(addIndent(getAmountSubDirectories(rootPath, directoryFile)))
                        .append(NODE).append(getDirectoryWithBytes(directoryFile.toPath(), directoryFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.append(getFiles(directoryFile));
        }
        return builder;
    }

    private StringBuilder getLastSubDirectory(List<File> directoryFiles, File directoryFile) {
        StringBuilder builder = new StringBuilder();
        if (directoryFile.isDirectory() && isLastFile(directoryFiles, directoryFile)) {
            try {
                builder.append(addIndent(getAmountSubDirectories(rootPath, directoryFile)))
                        .append(ANGLE).append(getDirectoryWithBytes(directoryFile.toPath(), directoryFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.append(getFiles(directoryFile));
        }
        return builder;
    }

    private StringBuilder getFile(List<File> directoryFiles, File directoryFile) {
        StringBuilder builder = new StringBuilder();
        if (directoryFile.isFile() && !isLastFile(directoryFiles, directoryFile)) {
            try {
                builder.append(addIndent(getAmountSubDirectories(rootPath, directoryFile)))
                        .append(NODE).append(getResultToString(directoryFile.toPath())).append(NEW_LINE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

    private StringBuilder getLastFile(List<File> directoryFiles, File directoryFile) {
        StringBuilder builder = new StringBuilder();
        if (directoryFile.isFile() && isLastFile(directoryFiles, directoryFile)) {
            try {
                builder.append(addIndent(getAmountSubDirectories(rootPath, directoryFile)))
                        .append(ANGLE).append(getResultToString(directoryFile.toPath())).append(NEW_LINE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

    private List<File> getCatalog(Path path) throws IOException {
        return Files.walk(path)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    private int getSumSize(Path path) throws IOException {
        return Files.walk(path)
                .map(Path::toFile)
                .filter(File::isFile)
                .mapToInt(value -> getFileSize(value.toPath()))
                .sum();
    }

    private StringBuilder getDirectoryWithBytes(Path path, File file) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(file.getName()).append(SPACE).append(getSumSize(path)).append(SPACE).append(BYTES).append(NEW_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder;
    }

    private boolean isRootDirectory(Path path, File file) {
        return file.toString().equals(path.toString());
    }

    private int getAmountSubDirectories(Path rootPath, File subFile) throws IOException {
        return getSubFileSize(subFile) - getRootPathSize(rootPath) - ROOT_DIRECTORY;
    }

    private int getRootPathSize(Path rootPath) {
        return Arrays.asList(rootPath.toString().split("/")).size();
    }

    private int getSubFileSize(File subFile) {
        return Arrays.asList(subFile.toString().split("/")).size();
    }

    private boolean isLastFile(List<File> directoryFiles, File file) {
        return file.equals(directoryFiles.get(directoryFiles.size() - 1));
    }

    private List<File> getDirectoryFiles(File file) {
        return Arrays.stream(Objects.requireNonNull(file.listFiles()))
                .sorted((o2, o1) -> o2.toString().compareToIgnoreCase(o1.toString()))
                .collect(Collectors.toList());
    }

    private Optional<String> getOptionalResult(Path path) {
        String string = null;
        try {
            string = getResultToString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(string);
    }

    private String getResultToString(Path path) throws IOException {
        return path.getFileName().toString() + SPACE + getFileSize(path) + SPACE + BYTES;
    }

    private String getContentInFile(Path path) throws IOException {
        String contents = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(path)));
            contents = reader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private int getFileSize(Path path) {
        String contents = null;
        try {
            contents = getContentInFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (contents != null) {
            return contents.length();
        } else return 0;
    }

    private String addIndent(int value) {
        return INDENT.repeat(value);
    }
}