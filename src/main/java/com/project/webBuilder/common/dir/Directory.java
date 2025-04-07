package com.project.webBuilder.common.dir;


import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Directory {
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
}
