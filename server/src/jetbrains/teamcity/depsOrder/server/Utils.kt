package jetbrains.teamcity.depsOrder.server

import java.io.File
import org.apache.log4j.Logger
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Created 08.08.13 18:32
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */

public fun File.div(child : String) : File = File(this, child)

//we define this category to have plugin logging without logger configs patching
inline fun log4j<T>(clazz : Class<T>) : Logger = Logger.getLogger("jetbrains.buildServer.${clazz.getName()}")!!

inline fun PluginDescriptor.div(s : String) = this.getPluginResourcesPath(s)
inline fun String.div(s : String) = this + "/" + s
