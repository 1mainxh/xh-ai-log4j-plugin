package com.diylog4j.agent;

import java.lang.instrument.Instrumentation;

/**
 * Java Agent 入口类
 */
public class Log4jAgent {

    /**
     * Agent 入口方法
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Log4j Plugin Agent started with args: " + agentArgs);
        
        // 添加类转换器
        inst.addTransformer(new Log4jClassTransformer());
    }

    /**
     * Agent 入口方法（动态 attach）
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }
}
