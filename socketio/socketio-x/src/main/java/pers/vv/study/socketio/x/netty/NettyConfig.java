/*
 * Copyright (c) 2012-2019 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.vv.study.socketio.x.netty;

import lombok.Data;

/**
 * TCP socket configuration contains configuration for main server channel
 * and client channels
 *
 * @see java.net.SocketOptions
 */
@Data
public class NettyConfig {

    private boolean isUseLinuxNativeEpoll;

    private int bossThreads;

    private int workerThreads;

    private boolean tcpNoDelay;

    private int tcpSendBufferSize;

    private int tcpReceiveBufferSize;

    private boolean tcpKeepAlive;

    private int soLinger;

    private boolean reuseAddress;

    private int acceptBackLog;

    public NettyConfig() {
        init();
    }

    private void init() {
        isUseLinuxNativeEpoll = false;
        bossThreads = Runtime.getRuntime().availableProcessors();
        workerThreads = Runtime.getRuntime().availableProcessors();
        tcpNoDelay = true;
        tcpSendBufferSize = -1;
        tcpReceiveBufferSize = -1;
        tcpKeepAlive = false;
        soLinger = -1;
        reuseAddress = false;
        acceptBackLog = 1024;
    }
}
