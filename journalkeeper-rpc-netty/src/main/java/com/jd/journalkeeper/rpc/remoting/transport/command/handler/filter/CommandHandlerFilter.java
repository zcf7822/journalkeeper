package com.jd.journalkeeper.rpc.remoting.transport.command.handler.filter;

import com.jd.journalkeeper.rpc.remoting.transport.command.Command;
import com.jd.journalkeeper.rpc.remoting.transport.exception.TransportException;

/**
 * 命令处理调用链
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public interface CommandHandlerFilter {

    Command invoke(CommandHandlerInvocation invocation) throws TransportException;
}