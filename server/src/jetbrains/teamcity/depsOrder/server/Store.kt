/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.teamcity.depsOrder.server

import jetbrains.buildServer.serverSide.CustomDataStorage
import jetbrains.buildServer.serverSide.SBuildType
import com.google.common.base.Joiner

/**
 * Created 08.08.13 19:30
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
class BuildTypeStorage(val buildType: SBuildType) {
  public var order: Array<String> by BuildTypeStorageArrayProperty(BuildTypeStorageProperty { buildType.getCustomDataStorage("teamcity.depsOrder") })
}

private class BuildTypeStorageProperty(val store: () -> CustomDataStorage) {
  public fun get(thisRef: Any?, prop: PropertyMetadata): String? {
    return with(store()) { getValue(prop.name) }
  }

  public fun set(thisRef: Any?, prop: PropertyMetadata, value: String?) {
    with(store()) {
      putValue(prop.name, value)
      flush()
    }
  }
}

private class BuildTypeStorageArrayProperty(val store: BuildTypeStorageProperty) {
  fun get(thisRef: Any?, prop: PropertyMetadata): Array<String> {
    val value = store.get(thisRef, prop)
    if (value == null) return array()

    return value.split(",").filter { it.length() > 0 }.toArray(array<String>())
  }

  fun set(thisRef: Any?, prop: PropertyMetadata, value: Array<String>) {
    store.set(thisRef, prop, Joiner.on(",")!!.join(value))
  }
}
