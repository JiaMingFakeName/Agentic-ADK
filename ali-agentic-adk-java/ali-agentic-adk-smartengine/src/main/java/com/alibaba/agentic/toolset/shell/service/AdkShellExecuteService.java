package com.alibaba.agentic.toolset.shell.service;


import java.io.IOException;
import java.util.List;

public interface AdkShellExecuteService {

    /**
     * 执行bash命令并返回结果
     *
     * @param command 要执行的bash命令
     * @return 命令执行的输出结果
     * @throws IOException 如果执行过程中发生I/O错误
     * @throws InterruptedException 如果执行过程被中断
     */
    String executeBashCommand(String command) throws IOException, InterruptedException;

    /**
     * 执行echo命令并返回结果
     *
     * @param text 要echo的文本
     * @return echo命令的输出结果
     * @throws IOException 如果执行过程中发生I/O错误
     * @throws InterruptedException 如果执行过程被中断
     */
    String executeEchoCommand(String text) throws IOException, InterruptedException;
}