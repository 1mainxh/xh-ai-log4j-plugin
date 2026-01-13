package com.diylog4j.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Log4j 类转换器（用于 Java Agent）
 */
public class Log4jClassTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                           ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        
        // 只处理我们关心的类
        if (className == null || !className.startsWith("com/diylog4j")) {
            return null;
        }

        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            
            // 这里可以添加 ASM 代码增强逻辑
            // 例如：在方法调用前后添加日志级别切换逻辑
            
            cr.accept(cw, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (Exception e) {
            System.err.println("Error transforming class: " + className);
            e.printStackTrace();
            return null;
        }
    }
}
