package com.strod.moduleinit.plugin.utils

import com.strod.moduleinit.plugin.core.RegisterTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Scan all class in the package: com/strod/moduleinit/
 * find out all routers,interceptors and providers
 */
class ScanUtil {

    /**
     * scan jar file
     * @param jarFile All jar files that are compiled into apk
     * @param destFile dest file after this transform
     */
    static void scanJar(File jarFile, File destFile) {
        if (jarFile) {
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName.endsWith('.class')){
                    if (entryName.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)) {
                        Logger.i("entryName:"+entryName)
                        InputStream inputStream = file.getInputStream(jarEntry)
                        scanClass(inputStream)
                        inputStream.close()
                    } else if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                        Logger.i("fileContainsInitClass->destFile:"+destFile.getName())
                        // mark this jar file contains LogisticsCenter.class
                        // After the scan is complete, we will generate register code into this file
                        RegisterTransform.fileContainsInitClass = destFile
                    }
                }

            }
            file.close()
        }
    }

    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository") && !path.startsWith("androidx")
    }

    static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)
    }

    /**
     * scan class file
     * @param class file
     */
    static void scanClass(File file) {
        scanClass(new FileInputStream(file))
    }

    static void scanClass(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            RegisterTransform.registerList.each { ext ->
                if (ext.interfaceName && interfaces != null) {
                    interfaces.each { itName ->
                        if (itName == ext.interfaceName) {
                            //fix repeated inject init code when Multi-channel packaging
                            if (!ext.classList.contains(name)) {
                                ext.classList.add(name)
                            }
                        }
                    }
                }
            }
        }
    }

}