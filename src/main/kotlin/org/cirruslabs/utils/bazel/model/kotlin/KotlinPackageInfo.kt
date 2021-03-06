package org.cirruslabs.utils.bazel.model.kotlin

import org.cirruslabs.utils.bazel.model.base.PackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry

class KotlinPackageInfo(
  fullyQualifiedName: String,
  targetPath: String
) : PackageInfo(fullyQualifiedName, targetPath, "kt") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    val deps = directPackageDependencies
      .map { packageRegistry.findInfo(it) }
      .flatten()
      .map { it.fullTargetLocation }
      .toSortedSet().joinToString(separator = "\n") { "\"$it\"," }
    return """# GENERATED
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

kt_jvm_library(
  name = "$targetName",
  srcs = glob(["*.kt"]),
  visibility = ["//visibility:public"],
  deps = [
${deps.prependIndent("    ")}
  ],
)"""
  }
}
