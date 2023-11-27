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

import java.nio.file.Path;

/**
 * Base interface for a component that can resolve a {@code protoc} executable.
 *
 * @author Ashley Scopes
 */
public interface ExecutableResolver {

  /**
   * Determine the path to the executable.
   *
   * @return the path to the executable.
   * @throws ExecutableResolutionException if resolution fails for any reason.
   */
  Path resolve() throws ExecutableResolutionException;
}