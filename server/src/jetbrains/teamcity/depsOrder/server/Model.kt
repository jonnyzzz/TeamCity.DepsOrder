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

import jetbrains.buildServer.serverSide.buildDistribution.StartingBuildAgentsFilter
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterContext
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterResult
import jetbrains.buildServer.serverSide.buildDistribution.QueuedBuildInfo
import jetbrains.buildServer.serverSide.ProjectManager
import java.util.HashSet
import jetbrains.buildServer.serverSide.BuildPromotionManager
import org.apache.log4j.Logger
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.TeamCityProperties

/**
 * Created 08.08.13 18:10
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */

data class ExternalId(val id: String)
data class Settings(val order: Array<String>?)

public class SettingsManager(val projects: ProjectManager) {
  class object {
    public val LOG: Logger = log4j(javaClass<SettingsManager>())
    public val featureId : String = ui.Constants().featureId
  }

  public fun forBuild(build: QueuedBuildInfo): List<ExternalId> {
    val internalId = build.getBuildConfiguration().getId()
    val bt = projects.findBuildTypeById(internalId)
    if (bt == null) return noOrder()
    return forBuild(bt)
  }

  public fun forBuild(bt: SBuildType ?): List<ExternalId> {
    if (bt == null) return noOrder()
    val feature = bt.getResolvedSettings().getBuildFeatures().find { it.getType() == featureId }
    if (feature == null) return noOrder()
    if (!bt.isEnabled(feature.getId())) return noOrder()

    val buildTypeIds = (feature.getParameters()[ui.Constants().items]?: "")
            .split("[\\s,\\n]+")
            .map { it.trim() }
            .filterNot { it.length() == 0 }

    return buildTypeIds
            .map { projects.findBuildTypeByExternalId(it)}
            .filterNotNull()
            .map { ExternalId(it.getExternalId())}
  }

  private fun noOrder() = listOf<ExternalId>()
}


public class OrderManager(val settings: SettingsManager,
                          val projects: ProjectManager,
                          val promotions: BuildPromotionManager) : StartingBuildAgentsFilter {
  class object {
    public val LOG: Logger = log4j(javaClass<SettingsManager>())
  }

  public override fun filterAgents(ctx: AgentsFilterContext): AgentsFilterResult {
    val result = AgentsFilterResult()
    if (TeamCityProperties.getBooleanOrTrue("teamcity.plugin.depsOrder.enabled")) {
      compute(ctx, result)
    }
    return result
  }

  private fun compute(ctx: AgentsFilterContext, result: AgentsFilterResult) {
    val build = ctx.getStartingBuild()
    val promoId = build.getBuildPromotionInfo().getId()
    val promo = promotions.findPromotionById(promoId)
    if (promo == null) return

    val allWhoNeedsMyBuild = promo
            .getDependedOnMe()
            .filterNotNull()
            .map { it.getDependent() }

    //fallback
    if (allWhoNeedsMyBuild.isEmpty()) return

    fun selectBuildsToWait(order : List<ExternalId>) : List<ExternalId> {
      val tmp = order.takeWhile { it != ExternalId(promo.getBuildTypeExternalId()) }
      //so there were no our build inside
      if (tmp == order) return arrayListOf<ExternalId>()
      //it was there
      return tmp
    }

    val allBuildTypeIdsToWaitFor = HashSet(
            allWhoNeedsMyBuild
                    .map { settings.forBuild(it.getBuildType()) }
                    .filterNot { it.isEmpty() }
                    .flatMap { selectBuildsToWait(it) }
            )

    //fallback
    if (allBuildTypeIdsToWaitFor.isEmpty()) return

    val tops = promo.findTops()
    if (tops == null) return

    val allFinishedBuildsExternalIds = tops.flatMap { it
            .getAllDependencies()
            .filterNotNull()
            .map { it.getAssociatedBuild() }
            .filterNotNull()
            .filter { it.isFinished() }
            .map { ExternalId(it.getBuildTypeExternalId()) }
    }

    val actualBuildsToWaitFor = allBuildTypeIdsToWaitFor - allFinishedBuildsExternalIds
    if (actualBuildsToWaitFor.isEmpty()) return

    val buildNamesToWait = actualBuildsToWaitFor
              .map { projects.findBuildTypeByExternalId(it.id)}
              .filterNotNull()
              .map { it.getFullName() }
              .sort()
              .asString(", ")

    result.setWaitReason { "[Ordered Dependencies] Waits for build to complete: " + buildNamesToWait }
  }
}
