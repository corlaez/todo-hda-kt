package com.corlaez.util

import org.webjars.WebJarAssetLocator
import java.nio.charset.Charset

private val locator by lazy { WebJarAssetLocator() }

// Do not use a trailing / for resource paths
// IMPORTANT: Must use getSystemResourceAsStream to load a resource from a jar
internal fun textFromResource(name: String, charset: Charset = Charset.forName("UTF-8")) =
    ClassLoader.getSystemResourceAsStream(name)!!.bufferedReader(charset).readText()

internal fun fullPathForWebjar(trailingPath: String) = locator.getFullPath(trailingPath)

internal fun textFromWebjar(trailingPath: String) = locator
    .getFullPath(trailingPath)
    .let(::textFromResource)

internal fun textFromWebjarWithArtifactId(artifactId: String, trailingPath: String) = locator.allWebJars.values
    .first { it.artifactId == artifactId }.contents
    .first { it.contains(trailingPath) }
    .let(::textFromResource)
