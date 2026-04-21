/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2026 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.testutils.junit

import com.google.common.reflect.ClassPath
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Validation methods to run against your test classes.
 */
object TestClassValidator {

    /**
     * Validates your test classes are:
     * * Using the correct `Test` annotation from JUnit5.
     * * Using one of the log suppression extensions, and only one if [allowMultiLogExtensions] is `false`.
     * * The log suppression extensions are:
     *     * Registered as an extension.
     *     * Defined as static.
     *     * Defined as final/val.
     *     * Named `systemOut` or `systemErr` respectably.
     *
     * @param packageName Optional pacakge name to use as the root for testing. Defaults to "com.zepben".
     * @param allowMultiLogExtensions Optional flag to allow both SYSTEM_ERR and SYSTEM_OUT to be used together.
     * Defaults to `false`, which is the expected value when using Logback with a test config that moves all messages
     * to the same appender.
     *
     * @throws AssertionError If test classes are detected that breach the rules.
     */
    fun validate(
        packageName: String = "com.zepben",
        excludingPackages: Set<String> = emptySet(),
        allowMultiLogExtensions: Boolean = false,
    ) {
        val testClassDetails =
            ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses
                .asSequence()
                .filter { it.name.startsWith(packageName) }
                .filterNot { excludingPackages.any { exclude -> it.name.startsWith(exclude) } }
                .filter { it.isProjectClass() }
                .mapNotNull { runCatching { ClassDetails(it.load()) }.getOrNull() }
                .filter { it.testMethods.isNotEmpty() }
                .toList()

        val incorrectTestAnnotation = testClassDetails.filter { details ->
            details.testMethods.filterNot { it.isAnnotationPresent(Test::class.java) }.isNotEmpty()
        }

        val unregisteredSystemLogExtensions = testClassDetails.filter { details ->
            details.systemLogExtensions.filterNot { it.isAnnotationPresent(RegisterExtension::class.java) }.isNotEmpty()
        }

        val missingSystemLogExtension = testClassDetails.filter { details ->
            details.systemLogExtensions.isEmpty()
        }

        val extraSystemLogExtension = if (allowMultiLogExtensions) emptyList() else testClassDetails.filter { details ->
            details.systemLogExtensions.size > 1
        }

        val nonStaticSystemLogExtension = testClassDetails.filter { details ->
            details.systemLogExtensions.filterNot { Modifier.isStatic(it.modifiers) }.isNotEmpty()
        }

        val nonFinalSystemLogExtension = testClassDetails.filter { details ->
            details.systemLogExtensions.filterNot { Modifier.isFinal(it.modifiers) }.isNotEmpty()
        }

        val systemLogExtensionByType =
            testClassDetails.flatMap { details -> details.systemLogExtensions.map { details to it } }
                .filter { (_, field) -> Modifier.isStatic(field.modifiers) }
                .groupBy { (_, field) ->
                    field.trySetAccessible()
                    field.get(null)
                }

        val incorrectSystemLogExtensionNames = systemLogExtensionByType.flatMap { (type, detailsToField) ->
            when (type) {
                SystemLogExtension.SYSTEM_OUT -> detailsToField.filter { (_, field) -> field.name != "systemOut" }
                SystemLogExtension.SYSTEM_ERR -> detailsToField.filter { (_, field) -> field.name != "systemErr" }
                else -> throw NotImplementedError("Unsupported SystemLogExtension $type")
            }
        }

        var reason = """
                Malformed test classes detected:

                Tests with incorrect `Test` annotations: $incorrectTestAnnotation,
                Tests with unregistered `SystemLogExtension`: $unregisteredSystemLogExtensions,
                Tests with missing `SystemLogExtension`: $missingSystemLogExtension,
                Tests with extra `SystemLogExtension`: $extraSystemLogExtension,
                Tests with non-static `SystemLogExtension`: $nonStaticSystemLogExtension,
                Tests with non-final `SystemLogExtension`: $nonFinalSystemLogExtension,
                Tests with incorrectly named `SystemLogExtension` variables: ${incorrectSystemLogExtensionNames.formatted},
            """.trimIndent()
        if (!allowMultiLogExtensions)
            reason += "\n\nShould all be using the same `SystemLogExtension` type: ${systemLogExtensionByType.formatted}"

        assertThat(
            reason,
            incorrectTestAnnotation.isEmpty() &&
                unregisteredSystemLogExtensions.isEmpty() &&
                missingSystemLogExtension.isEmpty() &&
                extraSystemLogExtension.isEmpty() &&
                nonStaticSystemLogExtension.isEmpty() &&
                nonFinalSystemLogExtension.isEmpty() &&
                (allowMultiLogExtensions || (systemLogExtensionByType.size == 1)) &&
                (incorrectSystemLogExtensionNames.isEmpty()),
        )
    }

    private val List<Pair<ClassDetails, Field>>.formatted: List<String> get() = map { (details, field) -> "${details.clazz.simpleName}.${field.name}" }
    private val Map<Any, List<Pair<ClassDetails, Field>>>.formatted: Map<String, Int>
        get() =
            mapKeys { (systemLogExtension, _) ->
                when (systemLogExtension) {
                    SystemLogExtension.SYSTEM_OUT -> "SystemLogExtension.SYSTEM_OUT"
                    else /*SystemLogExtension.SYSTEM_ERR*/ -> "SystemLogExtension.SYSTEM_ERR"
                }
            }.mapValues { (_, values) -> values.size }

    private class ClassDetails(
        val clazz: Class<*>,
    ) {

        val testMethods: List<Method> by lazy {
            clazz.declaredMethods.filter { method ->
                method.annotations.any { it.annotationClass.simpleName == "Test" }
            }
        }

        val systemLogExtensions: List<Field> by lazy {
            clazz.declaredFields.filter { field -> field.type == SystemLogExtension::class.java }
        }

        override fun toString(): String = clazz.canonicalName.removePrefix(clazz.packageName).removePrefix(".")

    }

    // Get the actual URL of the class file. If this is anything other than "file" it means it has been pulled in
    // from outside the project. i.e. via a jar.
    private fun ClassPath.ClassInfo.isProjectClass(): Boolean =
        Thread.currentThread().getContextClassLoader().getResource(resourceName)?.protocol == "file"

}
