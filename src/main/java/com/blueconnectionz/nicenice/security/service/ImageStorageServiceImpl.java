package com.blueconnectionz.nicenice.security.service;


import com.blueconnectionz.nicenice.controller.AuthController;
import com.blueconnectionz.nicenice.model.Document;
import com.blueconnectionz.nicenice.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class ImageStorageServiceImpl implements ImageStorageService{

    @Autowired
    DocumentRepository documentRepository;

    private final Path root = Paths.get("uploads");

    @Override
    public void init() throws IOException {
        Files.createDirectory(root);
    }

    @Override
    public void save(MultipartFile file, String uniqueDocumentId) throws IOException {
        Files.copy(file.getInputStream(),this.root.resolve(file.getOriginalFilename()));
        Path path = this.root.resolve(file.getOriginalFilename());
        String url = MvcUriComponentsBuilder
                .fromMethodName(AuthController.class, "getFile", path.getFileName().toString()).build().toString();
        Document document = new Document(
                url,
                uniqueDocumentId
        );
        documentRepository.save(document);
    }

    @Override
    public Resource load(String name) throws MalformedURLException {
        Path file = root.resolve(name);
        Resource resource = new UrlResource(file.toUri());
        if(resource.exists()|| resource.isReadable())
            return resource;
        else
            throw new RuntimeException("FAILED TO READ FILE");
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() throws IOException {
        return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
    }
}
