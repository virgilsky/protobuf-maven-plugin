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

package io.github.ascopes.protobufmavenplugin;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;


/**
 * Implementation independent descriptor for an artifact or dependency that can be used in a Maven
 * Plugin parameter.
 *
 * @author Ashley Scopes
 * @since 1.2.0
 */
public final class MavenArtifact {

  private @Nullable String groupId;
  private @Nullable String artifactId;
  private @Nullable String version;
  private @Nullable String classifier;
  private @Nullable String type;

  public Optional<String> getGroupId() {
    return Optional.ofNullable(groupId);
  }

  public void setGroupId(@Nullable String groupId) {
    this.groupId = groupId;
  }

  public Optional<String> getArtifactId() {
    return Optional.ofNullable(artifactId);
  }

  public void setArtifactId(@Nullable String artifactId) {
    this.artifactId = artifactId;
  }

  public Optional<String> getVersion() {
    return Optional.ofNullable(version);
  }

  public void setVersion(@Nullable String version) {
    this.version = version;
  }

  public Optional<String> getClassifier() {
    return Optional.ofNullable(classifier);
  }

  public void setClassifier(@Nullable String classifier) {
    this.classifier = classifier;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  @Parameter(alias = "extension")
  public void setType(@Nullable String type) {
    this.type = type;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (!(other instanceof MavenArtifact)) {
      return false;
    }

    var that = (MavenArtifact) other;

    return Objects.equals(groupId, that.groupId)
        && Objects.equals(artifactId, that.artifactId)
        && Objects.equals(version, that.version)
        && Objects.equals(classifier, that.classifier)
        && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version, classifier, type);
  }

  @Override
  public String toString() {
    return Stream.of(groupId, artifactId, version, classifier, type)
        .map(attr -> Objects.requireNonNullElse(attr, ""))
        .collect(Collectors.joining(":"));
  }
}