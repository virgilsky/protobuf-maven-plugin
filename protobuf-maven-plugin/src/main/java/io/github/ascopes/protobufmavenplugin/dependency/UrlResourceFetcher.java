/*
 * Copyright (C) 2023 - 2024, Ashley Scopes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ascopes.protobufmavenplugin.dependency;

import io.github.ascopes.protobufmavenplugin.generate.TemporarySpace;
import io.github.ascopes.protobufmavenplugin.utils.Digests;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that consumes URLs to obtain resources from remote locations.
 *
 * @author Ashley Scopes
 * @since 0.4.0
 */
@Named
public final class UrlResourceFetcher {

  private static final int TIMEOUT = 30_000;
  private static final String USER_AGENT = "User-Agent";
  private static final Logger log = LoggerFactory.getLogger(UrlResourceFetcher.class);

  private final TemporarySpace temporarySpace;

  @Inject
  public UrlResourceFetcher(TemporarySpace temporarySpace) {
    this.temporarySpace = temporarySpace;
  }

  public Optional<Path> fetchFileFromUrl(URL url, String extension) throws ResolutionException {
    return url.getProtocol().equalsIgnoreCase("file")
        ? handleFileSystemUrl(url)
        : handleOtherUrl(url, extension);
  }

  private Optional<Path> handleFileSystemUrl(URL url) throws ResolutionException {
    try {
      return Optional.of(url.toURI())
          .map(Path::of)
          .filter(Files::exists);
    } catch (URISyntaxException ex) {
      throw new ResolutionException("Failed to resolve '" + url + "' due to malformed syntax", ex);
    }
  }

  private Optional<Path> handleOtherUrl(URL url, String extension) throws ResolutionException {
    var targetFile = targetFile(url, extension);

    try {
      var conn = url.openConnection();
      conn.setConnectTimeout(TIMEOUT);
      conn.setReadTimeout(TIMEOUT);
      conn.setAllowUserInteraction(false);
      conn.setRequestProperty(USER_AGENT, userAgent());

      log.debug("Connecting to '{}' to copy resources to '{}'", url, targetFile);
      conn.connect();

      try (
          var responseStream = conn.getInputStream();
          var fileStream = new BufferedOutputStream(Files.newOutputStream(targetFile))
      ) {
        responseStream.transferTo(fileStream);
        log.info("Copied {} to {}", url, targetFile);
        return Optional.of(targetFile);

      } catch (FileNotFoundException ex) {
        log.debug("Resource at {} was not found", url);
        return Optional.empty();
      }

    } catch (IOException ex) {
      throw new ResolutionException("Failed to copy '" + url + "' to '" + targetFile + "'", ex);
    }
  }

  private String userAgent() {
    return "protobuf-maven-plugin/" + getClass().getPackage().getImplementationVersion()
        + " Apache-Maven/" + Maven.class.getPackage().getImplementationVersion()
        + " Java/" + Runtime.version().toString();
  }

  private Path targetFile(URL url, String extension) {
    var digest = Digests.sha1(url.toExternalForm());
    var path = url.getPath();
    var lastSlash = path.lastIndexOf('/');
    var fileName = lastSlash < 0
        ? digest
        : path.substring(lastSlash + 1) + "-" + digest;

    return temporarySpace
        .createTemporarySpace("url", url.getProtocol())
        .resolve(fileName + extension);
  }
}
