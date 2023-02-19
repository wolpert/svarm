/*
 * Copyright (c) 2023. Ned Wolpert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codeheadsystems.dstore.node.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will delete everything in the path you give it.
 */
public class DeletingFileVisitor implements FileVisitor<Path> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeletingFileVisitor.class);

  @Override
  public FileVisitResult preVisitDirectory(final Path path,
                                           final BasicFileAttributes basicFileAttributes) throws IOException {
    LOGGER.trace("preVisitDirectory({})", path);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path,
                                   final BasicFileAttributes basicFileAttributes) throws IOException {
    LOGGER.trace("visitFile({})", path);
    Files.delete(path);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(final Path path,
                                         final IOException e) throws IOException {
    LOGGER.error("visitFileFailed({})", path, e);
    return FileVisitResult.TERMINATE;
  }

  @Override
  public FileVisitResult postVisitDirectory(final Path path,
                                            final IOException e) throws IOException {
    LOGGER.trace("postVisitDirectory({})", path);
    Files.delete(path);
    return FileVisitResult.CONTINUE;
  }
}
