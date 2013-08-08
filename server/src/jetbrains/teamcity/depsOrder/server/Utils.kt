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

import java.io.File
import org.apache.log4j.Logger
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.util.LinkedHashSet
import com.google.common.base.Joiner

/**
 * Created 08.08.13 18:32
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */

public inline fun File.div(child : String) : File = File(this, child)

//we define this category to have plugin logging without logger configs patching
public inline fun log4j<T>(clazz : Class<T>) : Logger = Logger.getLogger("jetbrains.buildServer.${clazz.getName()}")!!

public inline fun PluginDescriptor.div(s : String) : String = this.getPluginResourcesPath(s)
public inline fun String.div(s : String) : String = this + "/" + s

public inline fun <T> Collection<T>.minus(other : Collection<T>) : Set<T> {
  val tmp = LinkedHashSet(this)
  tmp.removeAll(other)
  return tmp
}

public inline fun <T> Collection<T>.asString(sep : String = ",") : String = Joiner.on(sep)!!.join(this)!!