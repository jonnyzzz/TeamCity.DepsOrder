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

package jetbrains.teamcity.depsOrder.server.ui

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.controllers.BaseController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.servlet.ModelAndView
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.controllers.admin.projects.EditBuildTypeFormFactory
import jetbrains.buildServer.web.openapi.WebControllerManager
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import kotlin.properties.Delegates
import jetbrains.teamcity.depsOrder.server.*

/**
 * Created 08.08.13 19:53
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class Paths(val context : PluginDescriptor) {
  public val controller : String by Delegates.lazy { context / "feature.html" }
  public val jsp : String by Delegates.lazy { context / "feature.jsp" }
}

public class OrderingFeature(val paths : Paths) : BuildFeature() {
  public override fun getType(): String = Constants().featureId
  public override fun getDisplayName(): String = "Order Dependencies"
  public override fun getEditParametersUrl(): String = paths.controller

  public override fun describeParameters(params: Map<String, String>): String {
    return "Builds order: " + (params[Constants().items]?:"").replaceAll("[\r\n]+"," ").trim()
  }
}

public data class DependencyEntry(val externalId: String, val name: String) {
  public fun getReference() : String = ReferencesResolverUtil.makeReference("dep.${externalId}.system.teamcity.buildType.id")
}

public class Constants {
  public val items : String = "items"
  public val featureId : String = "teamcity.depsOrder"
}

public class FeatureControllerRegister(web : WebControllerManager, paths: Paths, controller : FeatureController) {{
  web.registerController(paths.controller, controller)
}}

public class FeatureController(val paths: Paths, val form: EditBuildTypeFormFactory) : BaseController() {
  protected override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
    val form = form.getOrCreateForm(request)
    if (form == null) return simpleView("Failed to find build type edit form")
    val settings = form.getSettings()
    if (settings == null) return simpleView("Failed to read settings from edit form")

    val mv = ModelAndView(paths.jsp)
    mv.getModel()!!.put("items",
    settings
            .getDependencies()
            .map { it.getDependOn() }
            .filterNotNull()
            .map { DependencyEntry(it.getExternalId(), it.getFullName()) }
    )
    return mv
  }
}