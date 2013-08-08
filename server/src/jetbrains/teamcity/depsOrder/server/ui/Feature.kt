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
  public override fun getType(): String = "teamcity.depsOrder"
  public override fun getDisplayName(): String = "Order Dependencies"
  public override fun getEditParametersUrl(): String = paths.controller
}

public data class DependencyEntry(val externalId: String, val name: String) {
  public fun getReference() : String = ReferencesResolverUtil.makeReference("dep.${externalId}.system.teamcity.buildType.id")
}

public class Constants {
  public val items : String = "items"
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