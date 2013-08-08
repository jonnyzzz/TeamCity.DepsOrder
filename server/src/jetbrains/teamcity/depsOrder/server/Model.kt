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
import com.google.common.base.Joiner
import org.apache.log4j.Logger
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.CustomDataStorage

/**
 * Created 08.08.13 18:10
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */

data class ExternalId(val id: String)
data class Settings(val order: Array<String>?)

public class SettingsManager(val projects: ProjectManager) {
  class object {
    public val LOG: Logger = log4j(javaClass<SettingsManager>())
  }

  private val SETTINGS_DIR = "teamcity.jonnyzzz.depsOrder"
  private val SETTINGS_KEY = "order"

  public fun forBuild(build: QueuedBuildInfo): List<ExternalId> {
    val internalId = build.getBuildConfiguration().getId()
    val bt = projects.findBuildTypeById(internalId)
    if (bt == null) return noOrder()
    return forBuild(bt)
  }

  public fun forBuild(bt: SBuildType): List<ExternalId> = with(bt.store()) {
    order.map { ExternalId(it) }
  }

  public fun updateBuild(build: SBuildType, steps: List<ExternalId>) {
    with(build.store()) {
      order = steps.map { it.id }.toArray(array<String>())
    }
  }

  private fun SBuildType.store() = BuildTypeStorage(this)
  private fun noOrder() = listOf<ExternalId>()
}


public class OrderManager(val settings: SettingsManager,
                          val promotions: BuildPromotionManager) : StartingBuildAgentsFilter {
  public override fun filterAgents(ctx: AgentsFilterContext): AgentsFilterResult {
    val result = AgentsFilterResult()
    compute(ctx, result)
    return result
  }

  private fun compute(ctx: AgentsFilterContext, result: AgentsFilterResult) {
    val build = ctx.getStartingBuild()
    val order = HashSet(settings.forBuild(build))
    if (order.isEmpty()) return

    val promoId = build.getBuildPromotionInfo().getId()
    val promo = promotions.findPromotionById(promoId)
    if (promo == null) return

    val buildsToWait = promo
            .getDependencies()
            .filterNotNull()
            .map { it.getDependOn() }
            .filterNotNull()
            .filter { order.contains(ExternalId(it.getBuildTypeId())) }
            .filterNot { it.getAssociatedBuild()?.isFinished() }
            .map { it.getBuildType() }
            .filterNotNull()
            .map { it.getFullName() }

    result.setWaitReason { "Waits for build to complete: " + Joiner.on(", ")!!.join(buildsToWait) }
  }
}
