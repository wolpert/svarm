package com.codeheadsystems.dstore.node.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DeletingFileVisitor implements FileVisitor<Path> {
  @Override
  public FileVisitResult preVisitDirectory(final Path path,
                                           final BasicFileAttributes basicFileAttributes) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path,
                                   final BasicFileAttributes basicFileAttributes) throws IOException {
    System.out.println("Deleting file: " + path);
    Files.delete(path);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(final Path path,
                                         final IOException e) throws IOException {
    return FileVisitResult.TERMINATE;
  }

  @Override
  public FileVisitResult postVisitDirectory(final Path path,
                                            final IOException e) throws IOException {
    System.out.println("Deleting dir: " + path);
    Files.delete(path);
    return FileVisitResult.CONTINUE;
  }
}
