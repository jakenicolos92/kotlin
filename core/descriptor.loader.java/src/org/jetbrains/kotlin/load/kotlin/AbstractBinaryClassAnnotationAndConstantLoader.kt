/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.load.kotlin

import com.google.protobuf.MessageLite
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.ProtoBuf
import org.jetbrains.kotlin.serialization.deserialization.*
import org.jetbrains.kotlin.serialization.jvm.ClassMapperLite
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf.propertySignature
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBufUtil
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.KotlinType
import java.util.*

abstract class AbstractBinaryClassAnnotationAndConstantLoader<A : Any, C : Any, T : Any>(
        storageManager: StorageManager,
        private val kotlinClassFinder: KotlinClassFinder,
        private val errorReporter: ErrorReporter
) : AnnotationAndConstantLoader<A, C, T> {
    private val storage = storageManager.createMemoizedFunction<KotlinJvmBinaryClass, Storage<A, C>> {
        kotlinClass ->
        loadAnnotationsAndInitializers(kotlinClass)
    }

    protected abstract fun loadConstant(desc: String, initializer: Any): C?

    protected abstract fun loadAnnotation(
            annotationClassId: ClassId,
            source: SourceElement,
            result: MutableList<A>
    ): KotlinJvmBinaryClass.AnnotationArgumentVisitor?

    protected abstract fun loadTypeAnnotation(proto: ProtoBuf.Annotation, nameResolver: NameResolver): A

    private fun loadAnnotationIfNotSpecial(
            annotationClassId: ClassId,
            source: SourceElement,
            result: MutableList<A>
    ): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
        if (JvmAnnotationNames.isSpecialAnnotation(annotationClassId, true)) return null

        return loadAnnotation(annotationClassId, source, result)
    }

    override fun loadClassAnnotations(classProto: ProtoBuf.Class, nameResolver: NameResolver): List<A> {
        val classId = nameResolver.getClassId(classProto.fqName)
        val kotlinClass = kotlinClassFinder.findKotlinClass(classId)
        if (kotlinClass == null) {
            // This means that the resource we're constructing the descriptor from is no longer present: KotlinClassFinder had found the
            // class earlier, but it can't now
            errorReporter.reportLoadingError("Kotlin class for loading class annotations is not found: ${classId.asSingleFqName()}", null)
            return listOf()
        }

        val result = ArrayList<A>(1)

        kotlinClass.loadClassAnnotations(object : KotlinJvmBinaryClass.AnnotationVisitor {
            override fun visitAnnotation(classId: ClassId, source: SourceElement): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                return loadAnnotationIfNotSpecial(classId, source, result)
            }

            override fun visitEnd() {
            }
        })

        return result
    }

    override fun loadCallableAnnotations(container: ProtoContainer, proto: MessageLite, kind: AnnotatedCallableKind): List<T> {
        if (kind == AnnotatedCallableKind.PROPERTY) {
            proto as ProtoBuf.Property

            val syntheticFunctionSignature = getPropertySignature(proto, container.nameResolver, container.typeTable, synthetic = true)
            val fieldSignature = getPropertySignature(proto, container.nameResolver, container.typeTable, field = true)

            val propertyAnnotations = syntheticFunctionSignature?.let { sig ->
                findClassAndLoadMemberAnnotations(container, sig, property = true)
            }.orEmpty()

            val fieldAnnotations = fieldSignature?.let { sig ->
                findClassAndLoadMemberAnnotations(container, sig, property = true, field = true)
            }.orEmpty()

            // TODO: check delegate presence in some other way
            return loadPropertyAnnotations(propertyAnnotations, fieldAnnotations,
                                           if (fieldSignature?.signature?.contains(JvmAbi.DELEGATED_PROPERTY_NAME_SUFFIX) ?: false) {
                                               AnnotationUseSiteTarget.PROPERTY_DELEGATE_FIELD
                                           }
                                           else {
                                               AnnotationUseSiteTarget.FIELD
                                           })
        }

        val signature = getCallableSignature(proto, container.nameResolver, container.typeTable, kind) ?: return emptyList()
        return transformAnnotations(findClassAndLoadMemberAnnotations(container, signature))
    }

    override fun loadEnumEntryAnnotations(container: ProtoContainer, proto: ProtoBuf.EnumEntry): List<A> {
        val signature = MemberSignature.fromFieldNameAndDesc(
                container.nameResolver.getString(proto.name),
                ClassMapperLite.mapClass((container as ProtoContainer.Class).classId)
        )
        return findClassAndLoadMemberAnnotations(container, signature)
    }

    protected abstract fun loadPropertyAnnotations(propertyAnnotations: List<A>, fieldAnnotations: List<A>,
                                                   fieldUseSiteTarget: AnnotationUseSiteTarget): List<T>

    protected abstract fun transformAnnotations(annotations: List<A>): List<T>

    private fun findClassAndLoadMemberAnnotations(
            container: ProtoContainer, signature: MemberSignature, property: Boolean = false, field: Boolean = false
    ): List<A> {
        val kotlinClass = findClassWithAnnotationsAndInitializers(container, getImplClassName(container, property), field)
                          ?: return listOf()

        return storage(kotlinClass).memberAnnotations[signature] ?: listOf()
    }

    override fun loadValueParameterAnnotations(
            container: ProtoContainer,
            message: MessageLite,
            kind: AnnotatedCallableKind,
            parameterIndex: Int,
            proto: ProtoBuf.ValueParameter
    ): List<A> {
        val methodSignature = getCallableSignature(message, container.nameResolver, container.typeTable, kind)
        if (methodSignature != null) {
            val index = parameterIndex + computeJvmParameterIndexShift(container, message)
            val paramSignature = MemberSignature.fromMethodSignatureAndParameterIndex(methodSignature, index)
            return findClassAndLoadMemberAnnotations(container, paramSignature)
        }

        return listOf()
    }

    private fun computeJvmParameterIndexShift(container: ProtoContainer, message: MessageLite): Int {
        return when (message) {
            is ProtoBuf.Function -> if (message.hasReceiver()) 1 else 0
            is ProtoBuf.Property -> if (message.hasReceiver()) 1 else 0
            is ProtoBuf.Constructor -> when {
                (container as ProtoContainer.Class).kind == ProtoBuf.Class.Kind.ENUM_CLASS -> 2
                container.isInner -> 1
                else -> 0
            }
            else -> throw UnsupportedOperationException("Unsupported message: ${message.javaClass}")
        }
    }

    override fun loadExtensionReceiverParameterAnnotations(
            container: ProtoContainer,
            message: MessageLite,
            kind: AnnotatedCallableKind
    ): List<A> {
        val methodSignature = getCallableSignature(message, container.nameResolver, container.typeTable, kind)
        if (methodSignature != null) {
            val paramSignature = MemberSignature.fromMethodSignatureAndParameterIndex(methodSignature, 0)
            return findClassAndLoadMemberAnnotations(container, paramSignature)
        }

        return emptyList()
    }

    override fun loadTypeAnnotations(type: ProtoBuf.Type, nameResolver: NameResolver): List<A> {
        return type.getExtension(JvmProtoBuf.typeAnnotation).map { loadTypeAnnotation(it, nameResolver) }
    }

    override fun loadTypeParameterAnnotations(typeParameter: ProtoBuf.TypeParameter, nameResolver: NameResolver): List<A> {
        return typeParameter.getExtension(JvmProtoBuf.typeParameterAnnotation).map { loadTypeAnnotation(it, nameResolver) }
    }

    override fun loadPropertyConstant(container: ProtoContainer, proto: ProtoBuf.Property, expectedType: KotlinType): C? {
        val signature = getCallableSignature(proto, container.nameResolver, container.typeTable, AnnotatedCallableKind.PROPERTY)
                        ?: return null

        val kotlinClass = findClassWithAnnotationsAndInitializers(container, getImplClassName(container, property = true), field = true)
                          ?: return null

        return storage(kotlinClass).propertyConstants[signature]
    }

    private fun findClassWithAnnotationsAndInitializers(
            container: ProtoContainer, implClassName: ClassId?, field: Boolean
    ): KotlinJvmBinaryClass? {
        if (implClassName != null) {
            return kotlinClassFinder.findKotlinClass(implClassName)
        }

        if (container is ProtoContainer.Class) {
            if (field && container.kind == ProtoBuf.Class.Kind.COMPANION_OBJECT &&
                (container.outerClassKind == ClassKind.CLASS || container.outerClassKind == ClassKind.ENUM_CLASS)) {
                // Backing fields of properties of a companion object in a class are generated in the outer class
                return kotlinClassFinder.findKotlinClass(container.classId.outerClassId)
            }

            return kotlinClassFinder.findKotlinClass(container.classId)
        }

        return null
    }

    private fun getImplClassName(container: ProtoContainer, property: Boolean): ClassId? {
        if (property && container is ProtoContainer.Class && container.kind == ProtoBuf.Class.Kind.INTERFACE) {
            return container.classId.createNestedClassId(Name.identifier(JvmAbi.DEFAULT_IMPLS_CLASS_NAME))
        }
        return ((container as? ProtoContainer.Package)?.packagePartSource as? JvmPackagePartSource)?.classId
    }

    private fun loadAnnotationsAndInitializers(kotlinClass: KotlinJvmBinaryClass): Storage<A, C> {
        val memberAnnotations = HashMap<MemberSignature, MutableList<A>>()
        val propertyConstants = HashMap<MemberSignature, C>()

        kotlinClass.visitMembers(object : KotlinJvmBinaryClass.MemberVisitor {
            override fun visitMethod(name: Name, desc: String): KotlinJvmBinaryClass.MethodAnnotationVisitor? {
                return AnnotationVisitorForMethod(MemberSignature.fromMethodNameAndDesc(name.asString(), desc))
            }

            override fun visitField(name: Name, desc: String, initializer: Any?): KotlinJvmBinaryClass.AnnotationVisitor? {
                val signature = MemberSignature.fromFieldNameAndDesc(name.asString(), desc)

                if (initializer != null) {
                    val constant = loadConstant(desc, initializer)
                    if (constant != null) {
                        propertyConstants[signature] = constant
                    }
                }
                return MemberAnnotationVisitor(signature)
            }

            inner class AnnotationVisitorForMethod(signature: MemberSignature) : MemberAnnotationVisitor(signature), KotlinJvmBinaryClass.MethodAnnotationVisitor {

                override fun visitParameterAnnotation(
                        index: Int, classId: ClassId, source: SourceElement
                ): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                    val paramSignature = MemberSignature.fromMethodSignatureAndParameterIndex(signature, index)
                    var result = memberAnnotations[paramSignature]
                    if (result == null) {
                        result = ArrayList<A>()
                        memberAnnotations[paramSignature] = result
                    }
                    return loadAnnotationIfNotSpecial(classId, source, result)
                }
            }

            open inner class MemberAnnotationVisitor(protected val signature: MemberSignature) : KotlinJvmBinaryClass.AnnotationVisitor {
                private val result = ArrayList<A>()

                override fun visitAnnotation(classId: ClassId, source: SourceElement): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                    return loadAnnotationIfNotSpecial(classId, source, result)
                }

                override fun visitEnd() {
                    if (result.isNotEmpty()) {
                        memberAnnotations[signature] = result
                    }
                }
            }
        })

        return Storage(memberAnnotations, propertyConstants)
    }

    private fun getPropertySignature(
            proto: ProtoBuf.Property,
            nameResolver: NameResolver,
            typeTable: TypeTable,
            field: Boolean = false,
            synthetic: Boolean = false
    ): MemberSignature? {
        val signature =
                if (proto.hasExtension(propertySignature)) proto.getExtension(propertySignature)
                else return null

        if (field) {
            val (name, desc) = JvmProtoBufUtil.getJvmFieldSignature(proto, nameResolver, typeTable) ?: return null
            return MemberSignature.fromFieldNameAndDesc(name, desc)
        }
        else if (synthetic && signature.hasSyntheticMethod()) {
            return MemberSignature.fromMethod(nameResolver, signature.syntheticMethod)
        }

        return null
    }

    private fun getCallableSignature(
            proto: MessageLite,
            nameResolver: NameResolver,
            typeTable: TypeTable,
            kind: AnnotatedCallableKind
    ): MemberSignature? {
        return when {
            proto is ProtoBuf.Constructor -> {
                MemberSignature.fromMethodNameAndDesc(JvmProtoBufUtil.getJvmConstructorSignature(proto, nameResolver, typeTable) ?: return null)
            }
            proto is ProtoBuf.Function -> {
                MemberSignature.fromMethodNameAndDesc(JvmProtoBufUtil.getJvmMethodSignature(proto, nameResolver, typeTable) ?: return null)
            }
            proto is ProtoBuf.Property && proto.hasExtension(propertySignature) -> {
                val signature = proto.getExtension(propertySignature)
                when (kind) {
                    AnnotatedCallableKind.PROPERTY_GETTER -> MemberSignature.fromMethod(nameResolver, signature.getter)
                    AnnotatedCallableKind.PROPERTY_SETTER -> MemberSignature.fromMethod(nameResolver, signature.setter)
                    AnnotatedCallableKind.PROPERTY -> getPropertySignature(proto, nameResolver, typeTable, true, true)
                    else -> null
                }
            }
            else -> null
        }
    }

    private class Storage<A, C>(
            val memberAnnotations: Map<MemberSignature, List<A>>,
            val propertyConstants: Map<MemberSignature, C>
    )
}
