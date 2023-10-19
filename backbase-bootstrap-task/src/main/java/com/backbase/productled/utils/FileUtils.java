package com.backbase.productled.utils;

import com.backbase.productled.model.ContentTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 02. Nov 2022 11:38 am
 */
public class FileUtils {

    public static List<ContentTemplate> getContentTemplate(String repoId, Set<String> files) {
        return files.stream()
                .map(File::new)
                .map(file -> ContentTemplate.builder()
                        .file(file)
                        .pathInRepository(getTemplatePath(repoId, file.getPath(), file.getName()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static String getTemplatePath(String repoId, String filePathString, String fileName) {
        String filePathName = filePathString
                .substring(filePathString.indexOf(repoId) + repoId.length());
        return filePathName.substring(0, filePathName.length() - fileName.length());
    }

    public static Set<String> listFilesUsingFilesList(String dir) throws IOException {
        Set<String> filesNames = new HashSet<>();
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            stream.filter(Files::isDirectory)
                    .map(dir1 -> {
                        try {
                            return listFilesUsingFilesList(dir1.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet())
                    .forEach(filesNames::addAll);
        }

        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            filesNames.addAll(stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .collect(Collectors.toSet()));
        }
        return filesNames;
    }
}
