/*
 * Copyright (C) 2023, Ashley Scopes.
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

package io.github.ascopes.protobufmavenplugin.resolve;

import io.github.ascopes.protobufmavenplugin.platform.HostEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolver for {@code protoc} which looks up the provided version in the Maven remote repositories
 * for the project and fetches the desired version prior to returning the path to the executable.
 *
 * @author Ashley Scopes
 */
public final class MavenProtocResolver implements ProtocResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(MavenProtocResolver.class);

  private final String version;
  private final ArtifactResolver artifactResolver;
  private final MavenSession session;
  private final MavenProtocCoordinateFactory coordinateFactory;

  /**
   * Initialise this resolver.
   *
   * @param version the version/version range to resolve.
   * @param artifactResolver the Maven artifact reaolver to use.
   * @param session the current Maven session.
   */
  public MavenProtocResolver(
      String version,
      ArtifactResolver artifactResolver,
      MavenSession session
  ) {
    this.version = version;
    this.artifactResolver = artifactResolver;
    this.session = session;
    coordinateFactory = new MavenProtocCoordinateFactory();
  }

  @Override
  public Path resolveProtoc() throws ProtocResolutionException {
    Path path;

    try {
      var coordinate = coordinateFactory.create(version);
      var request = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());

      LOGGER.info("Resolving protoc '{}' from local/remote repositories", identifier(coordinate));

      var result = artifactResolver.resolveArtifact(request, coordinate);
      path = result.getArtifact().getFile().toPath();

    } catch (ArtifactResolverException ex) {
      throw new ProtocResolutionException(
          "Failed to resolve protoc artifact from remote repository",
          ex
      );
    }

    LOGGER.info("Resolved protoc to local path '{}'", path);

    if (!HostEnvironment.isWindows()) {
      LOGGER.debug("Ensuring '{}' is marked as executable", path);

      try {
        // Copy so we can modify the set.
        var permissions = new HashSet<>(Files.getPosixFilePermissions(path));
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(path, permissions);
      } catch (IOException ex) {
        throw new ProtocResolutionException("Setting executable bit for '" + path + "' failed", ex);
      }
    }

    return path;
  }

  private String identifier(ArtifactCoordinate coordinate) {
    return "mvn:"
        + coordinate.getGroupId()
        + "/"
        + coordinate.getArtifactId()
        + "/"
        + coordinate.getVersion()
        + "/"
        + coordinate.getClassifier()
        + "/"
        + coordinate.getExtension();
  }
}