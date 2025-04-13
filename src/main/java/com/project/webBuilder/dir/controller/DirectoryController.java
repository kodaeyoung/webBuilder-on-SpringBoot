package com.project.webBuilder.dir.controller;


import com.project.webBuilder.dir.FileNode;
import com.project.webBuilder.dir.service.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping("/get-structure/dir")
    public List<FileNode> getStructure(@RequestParam String path) throws IOException {
        return directoryService.getDirectoryStructure(path);
    }

    @GetMapping("/get-content/file")
    public String getFileContents(@RequestParam String path) throws IOException {
        return directoryService.getFileContents(path);
    }
}
