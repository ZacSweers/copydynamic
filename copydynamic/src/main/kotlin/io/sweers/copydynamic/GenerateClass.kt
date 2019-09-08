/*
 * Copyright (c) 2019 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sweers.copydynamic

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.Type
import org.objectweb.asm.Type.BOOLEAN_TYPE
import org.objectweb.asm.Type.BYTE_TYPE
import org.objectweb.asm.Type.CHAR_TYPE
import org.objectweb.asm.Type.DOUBLE_TYPE
import org.objectweb.asm.Type.FLOAT_TYPE
import org.objectweb.asm.Type.INT_TYPE
import org.objectweb.asm.Type.LONG_TYPE
import org.objectweb.asm.Type.SHORT_TYPE
import javax.annotation.processing.Filer
import javax.lang.model.element.Element

private const val MARKER = "kotlin/jvm/internal/DefaultConstructorMarker"

fun generateClass(
    packageName: String,
    bridgeName: String,
    targetClassName: String,
    filer: Filer,
    primaryConstructorDesc: String,
    originatingElements: List<Element>
) {
  val packageJvmName = packageName.replace(".", "/")
  val targetJvmName = "${packageJvmName}/$targetClassName"
  val bridgeClassJvmName = "${packageJvmName}/$bridgeName"

  // Tease out the initial description, we'll synthesize our own starting from it
  // Given something like this and targeting "package/to/TargetClass"
  //   "(Ljava/lang/Object;)V"
  // We'll save
  //   "(Ljava/lang/Object;"
  // And formulate the following two
  // - Bridge method signature: "(Ljava/lang/Object;I)Lpackage/to/TargetClass"
  //   - I for the int mask
  //   - Change return type to the target class
  // - Target defaults constructor: "(Ljava/lang/Object;ILkotlin/jvm/internal/DefaultConstructorMarker;)V"
  //   - I for the int mask
  //   - kotlin/jvm/internal/DefaultConstructorMarker for the constructor marker arg (always null at runtime)
  val prefix = primaryConstructorDesc.substringBeforeLast(")")
  val bridgeDesc = "${prefix}I)L$targetJvmName;"
  val defaultsConstructorDesc = "${prefix}IL$MARKER;)V"

  val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES).apply {
    visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, bridgeClassJvmName, null, "java/lang/Object", null)
  }

  // Private empty default constructor
  cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).apply {
    visitCode()
    visitVarInsn(ALOAD, 0) // load "this"
    visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
    visitInsn(RETURN)

    // Note that MAXS are computed, but we still have to call this at the end with throwaway values
    visitMaxs(-1, -1)
    visitEnd()
  }

  // Constructor bridge method
  cw.visitMethod(ACC_STATIC + ACC_PUBLIC, "constructorBridge", bridgeDesc, null, null).apply {
    visitCode()
    visitTypeInsn(NEW, targetJvmName)
    visitInsn(DUP)

    // Load parameters onto the stack
    var counter = 0
    val args = Type.getArgumentTypes(bridgeDesc)
    for (argType in args) {
      visitVarInsn(argType.toLoadInstruction(), counter)
      counter += argType.toStackSize()
    }
    // null for the default constructor marker
    visitInsn(ACONST_NULL)
    visitMethodInsn(INVOKESPECIAL, targetJvmName, "<init>", defaultsConstructorDesc, false)
    visitInsn(ARETURN)

    // Note that MAXS are computed, but we still have to call this at the end with throwaway values
    visitMaxs(-1, -1)
    visitEnd()
  }
  cw.visitEnd()

  // Write to filer
  val filerSourceFile = filer.createClassFile("${packageName}.$bridgeName",
      *originatingElements.toTypedArray()
  )
  filerSourceFile.openOutputStream().use { os ->
    os.write(cw.toByteArray())
  }
}

private fun Type.toLoadInstruction(): Int {
  return when (this) {
    BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, SHORT_TYPE, INT_TYPE -> Opcodes.ILOAD
    LONG_TYPE -> Opcodes.LLOAD
    FLOAT_TYPE -> Opcodes.FLOAD
    DOUBLE_TYPE -> Opcodes.DLOAD
    else -> ALOAD
  }
}

private fun Type.toStackSize(): Int {
  return when (this) {
    LONG_TYPE, DOUBLE_TYPE -> 2
    else -> 1
  }
}
