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

package io.sweers.classgentesting.processor;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import io.sweers.copydynamic.annotations.CopyDynamic;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

/**
 * A simple sample processor that, given an annotated class, generates another class with a static
 * creator for it.
 *
 * <code><pre>
 *   // Source class...
 *   &#064;CopyDynamic
 *   public class TestClass {}
 *
 *   // Generates bytecode equivalent of this...
 *   public final class TestClassFactory {
 *     private TestClassFactory() {
 *
 *     }
 *
 *     public static TestClass create() {
 *       return new TestClass();
 *     }
 *   }
 * </pre></code>>
 */
@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
public final class ClassGenProcessor extends AbstractProcessor {

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    set.add(CopyDynamic.class.getCanonicalName());
    return set;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(CopyDynamic.class);

    for (Element element : annotatedElements) {
      TypeElement typeElement = (TypeElement) element;
      String packageName = MoreElements.getPackage(typeElement).toString();
      String packageJvmName = packageName.replace(".", "/");
      String targetClassName = typeElement.getSimpleName().toString();
      String bridgeName = targetClassName + "Factory";
      String targetJvmName = packageJvmName + "/" + targetClassName;
      String bridgeClassJvmName = packageJvmName + "/" + bridgeName;

      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, bridgeClassJvmName, null, "java/lang/Object", null);

      // Private empty default constructor
      MethodVisitor constructorVisitor = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
      constructorVisitor.visitCode();
      constructorVisitor.visitVarInsn(ALOAD, 0); // load "this"
      constructorVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      constructorVisitor.visitInsn(RETURN);
      // Note that MAXS are computed, but we still have to call this at the end with throwaway values
      constructorVisitor.visitMaxs(-1, -1);
      constructorVisitor.visitEnd();

      // Constructor bridge method
      MethodVisitor mv = cw.visitMethod(ACC_STATIC + ACC_PUBLIC, "create", "()L" + targetJvmName + ";", null, null);
      mv.visitCode();
      mv.visitTypeInsn(NEW, targetJvmName);
      mv.visitInsn(DUP);

      mv.visitMethodInsn(INVOKESPECIAL, targetJvmName, "<init>", "()V", false);
      mv.visitInsn(ARETURN);

      // Note that MAXS are computed, but we still have to call this at the end with throwaway values
      mv.visitMaxs(-1, -1);
      mv.visitEnd();
      cw.visitEnd();

      // Write to filer
      try {
        JavaFileObject filerSourceFile = processingEnv.getFiler().createClassFile(packageName + "." + bridgeName,
            typeElement);
        try (OutputStream os = filerSourceFile.openOutputStream()) {
          os.write(cw.toByteArray());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return false;
  }
}
