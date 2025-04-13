package com.project.webBuilder.dir.service;


import com.project.webBuilder.dir.FileNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DirectoryService {

    private Path rootDirPath = Paths.get(System.getProperty("user.dir"));
    // 디렉터리 복사
    public static void copyDirectory(Path source, Path target) throws IOException {
        // 파일 복사를 위해 Files.walkFileTree 사용
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path destination = target.resolve(source.relativize(file));
                Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path destination = target.resolve(source.relativize(dir));
                if (Files.notExists(destination)) {
                    Files.createDirectories(destination);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    //디렉터리 삭제
    public static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    //디렉터리 비교
    public static boolean isSameDirectory(Path dir1, Path dir2) throws IOException {
        try (Stream<Path> files1 = Files.walk(dir1);
             Stream<Path> files2 = Files.walk(dir2)) {

            List<String> list1 = files1
                    .filter(Files::isRegularFile)
                    .map(p -> dir1.relativize(p).toString())
                    .sorted()
                    .collect(Collectors.toList());

            List<String> list2 = files2
                    .filter(Files::isRegularFile)
                    .map(p -> dir2.relativize(p).toString())
                    .sorted()
                    .collect(Collectors.toList());

            if (!list1.equals(list2)) {
                return false;
            }

            for (String relativePath : list1) {
                Path file1 = dir1.resolve(relativePath);
                Path file2 = dir2.resolve(relativePath);

                if (!Files.exists(file1) || !Files.exists(file2)) return false;

                byte[] bytes1 = Files.readAllBytes(file1);
                byte[] bytes2 = Files.readAllBytes(file2);

                if (!Arrays.equals(bytes1, bytes2)) {
                    return false;
                }
            }
            return true;
        }
    }


    // 디렉터리 구조 제공
    public List<FileNode> getDirectoryStructure(String basePath) throws IOException {

        return getDirectoryRecursive(Path.of(basePath), Path.of(""));
    }

    private List<FileNode> getDirectoryRecursive(Path basePath, Path currentPath) throws IOException {
        List<FileNode> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirPath.resolve(basePath).resolve(currentPath))) {
            for (Path path : stream) {
                String relativePath = basePath.resolve(currentPath).resolve(path.getFileName()).toString().replace("\\", "/");
                FileNode node = new FileNode(path.getFileName().toString(), relativePath, Files.isDirectory(path));

                if (Files.isDirectory(path)) {
                    node.setChildren(getDirectoryRecursive(basePath, currentPath.resolve(path.getFileName())));
                }

                result.add(node);
            }
        }
        return result;
    }

    //파일 내용 제공
    public String getFileContents(String filePath) throws IOException {
        Path fullPath = rootDirPath.resolve(filePath);
        return Files.readString(fullPath, StandardCharsets.UTF_8);
    }
}
