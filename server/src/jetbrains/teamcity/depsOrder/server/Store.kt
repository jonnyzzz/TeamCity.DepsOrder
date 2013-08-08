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
