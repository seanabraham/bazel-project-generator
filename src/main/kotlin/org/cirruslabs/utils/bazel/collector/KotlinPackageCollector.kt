package org.cirruslabs.utils.bazel.collector

import kotlinx.coroutines.withContext
import org.cirruslabs.utils.bazel.model.kotlin.KotlinPackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.kotlinx.coroutines.Dispatchers
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Path

class KotlinPackageCollector(
  workspaceRoot: Path,
  environment: KotlinCoreEnvironment
): AbstractKotlinPackageCollector<KotlinPackageInfo>(workspaceRoot, environment) {
  companion object {
    suspend fun create(workspaceRoot: Path): KotlinPackageCollector = withContext(Dispatchers.IO) {
      val environment = KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        CompilerConfiguration().apply {
          val messageCollector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, true)
          put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
        },
        EnvironmentConfigFiles.JVM_CONFIG_FILES
      )
      KotlinPackageCollector(workspaceRoot, environment)
    }
  }

  override val subRoot: String
    get() = "main/kotlin"

  override fun buildFilePath(packageInfo: KotlinPackageInfo): Path =
    workspaceRoot.resolve(packageInfo.targetPath).resolve("BUILD.bazel")

  override fun generateBuildFileContent(registry: PackageRegistry, packageInfo: KotlinPackageInfo): String =
    packageInfo.generateBuildFile(registry)

  override fun generateTarget(fqn: String, relativePackagePath: String, directPackageDependencies: Set<String>, files: List<KtFile>): KotlinPackageInfo {
    val packageInfo = KotlinPackageInfo(fullyQualifiedName = fqn, targetPath = relativePackagePath)
    packageInfo.addDirectPackageDependencies(directPackageDependencies)
    return packageInfo
  }
}