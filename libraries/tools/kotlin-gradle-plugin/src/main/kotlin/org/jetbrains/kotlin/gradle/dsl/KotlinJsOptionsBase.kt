// DO NOT EDIT MANUALLY!
// Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
package org.jetbrains.kotlin.gradle.dsl

internal abstract class KotlinJsOptionsBase : org.jetbrains.kotlin.gradle.dsl.KotlinJsOptions {

    private var allWarningsAsErrorsField: kotlin.Boolean? = null
    override var allWarningsAsErrors: kotlin.Boolean
        get() = allWarningsAsErrorsField ?: false
        set(value) {
            allWarningsAsErrorsField = value
        }

    private var suppressWarningsField: kotlin.Boolean? = null
    override var suppressWarnings: kotlin.Boolean
        get() = suppressWarningsField ?: false
        set(value) {
            suppressWarningsField = value
        }

    private var verboseField: kotlin.Boolean? = null
    override var verbose: kotlin.Boolean
        get() = verboseField ?: false
        set(value) {
            verboseField = value
        }

    private var apiVersionField: kotlin.String?? = null
    override var apiVersion: kotlin.String?
        get() = apiVersionField ?: null
        set(value) {
            apiVersionField = value
        }

    private var languageVersionField: kotlin.String?? = null
    override var languageVersion: kotlin.String?
        get() = languageVersionField ?: null
        set(value) {
            languageVersionField = value
        }

    private var friendModulesDisabledField: kotlin.Boolean? = null
    override var friendModulesDisabled: kotlin.Boolean
        get() = friendModulesDisabledField ?: false
        set(value) {
            friendModulesDisabledField = value
        }

    private var mainField: kotlin.String? = null
    override var main: kotlin.String
        get() = mainField ?: "call"
        set(value) {
            mainField = value
        }

    private var metaInfoField: kotlin.Boolean? = null
    override var metaInfo: kotlin.Boolean
        get() = metaInfoField ?: true
        set(value) {
            metaInfoField = value
        }

    private var moduleKindField: kotlin.String? = null
    override var moduleKind: kotlin.String
        get() = moduleKindField ?: "plain"
        set(value) {
            moduleKindField = value
        }

    private var noStdlibField: kotlin.Boolean? = null
    override var noStdlib: kotlin.Boolean
        get() = noStdlibField ?: true
        set(value) {
            noStdlibField = value
        }

    private var outputFileField: kotlin.String?? = null
    override var outputFile: kotlin.String?
        get() = outputFileField ?: null
        set(value) {
            outputFileField = value
        }

    private var sourceMapField: kotlin.Boolean? = null
    override var sourceMap: kotlin.Boolean
        get() = sourceMapField ?: false
        set(value) {
            sourceMapField = value
        }

    private var sourceMapEmbedSourcesField: kotlin.String?? = null
    override var sourceMapEmbedSources: kotlin.String?
        get() = sourceMapEmbedSourcesField ?: null
        set(value) {
            sourceMapEmbedSourcesField = value
        }

    private var sourceMapPrefixField: kotlin.String?? = null
    override var sourceMapPrefix: kotlin.String?
        get() = sourceMapPrefixField ?: null
        set(value) {
            sourceMapPrefixField = value
        }

    private var targetField: kotlin.String? = null
    override var target: kotlin.String
        get() = targetField ?: "v5"
        set(value) {
            targetField = value
        }

    private var typedArraysField: kotlin.Boolean? = null
    override var typedArrays: kotlin.Boolean
        get() = typedArraysField ?: true
        set(value) {
            typedArraysField = value
        }

    internal open fun updateArguments(args: org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments) {
        allWarningsAsErrorsField?.let { args.allWarningsAsErrors = it }
        suppressWarningsField?.let { args.suppressWarnings = it }
        verboseField?.let { args.verbose = it }
        apiVersionField?.let { args.apiVersion = it }
        languageVersionField?.let { args.languageVersion = it }
        friendModulesDisabledField?.let { args.friendModulesDisabled = it }
        mainField?.let { args.main = it }
        metaInfoField?.let { args.metaInfo = it }
        moduleKindField?.let { args.moduleKind = it }
        noStdlibField?.let { args.noStdlib = it }
        outputFileField?.let { args.outputFile = it }
        sourceMapField?.let { args.sourceMap = it }
        sourceMapEmbedSourcesField?.let { args.sourceMapEmbedSources = it }
        sourceMapPrefixField?.let { args.sourceMapPrefix = it }
        targetField?.let { args.target = it }
        typedArraysField?.let { args.typedArrays = it }
    }
}

internal fun org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments.fillDefaultValues() {
    allWarningsAsErrors = false
    suppressWarnings = false
    verbose = false
    apiVersion = null
    languageVersion = null
    friendModulesDisabled = false
    main = "call"
    metaInfo = true
    moduleKind = "plain"
    noStdlib = true
    outputFile = null
    sourceMap = false
    sourceMapEmbedSources = null
    sourceMapPrefix = null
    target = "v5"
    typedArrays = true
}
