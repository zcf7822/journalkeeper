/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.journalkeeper.examples.kv;

import io.journalkeeper.core.api.ClusterConfiguration;
import io.journalkeeper.core.api.RaftClient;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.core.api.ServerStatus;
import io.journalkeeper.utils.event.EventType;
import io.journalkeeper.utils.event.EventWatcher;
import io.journalkeeper.utils.format.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author LiYue
 * Date: 2019-04-03
 */
public class KvClient {
    private static final Logger logger = LoggerFactory.getLogger(KvClient.class);
    private final RaftClient<KvEntry, Void, KvQuery, KvResult> client;

    public KvClient(RaftClient<KvEntry, Void, KvQuery, KvResult> client) {
        this.client = client;
    }

    public void set(String key, String value) {
        long t0 = System.nanoTime();
        try {
            client.update(new KvEntry(KvEntry.CMD_SET, key, value)).get();
        } catch (CompletionException e) {
            throw new RuntimeException(e.getCause());
        }catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("SET {} {}, {} ns.", key, value, Format.formatWithComma(System.nanoTime() - t0));
        }
    }

    public String get(String key) {
        long t0 = System.nanoTime();
        String value = null;
        try {
            return value = client
                    .query(new KvQuery(KvQuery.CMD_GET, key))
                    .get()
                    .getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("GET {} , return {}, {} ns.", key, value, Format.formatWithComma(System.nanoTime() - t0));
        }
    }

    public void del(String key) {
        long t0 = System.nanoTime();
        try {
            client.update(new KvEntry(KvEntry.CMD_DEL, key, null)).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("DEL {} , {} ns.", key,  Format.formatWithComma(System.nanoTime() - t0));
        }

    }


    public List<String> listKeys() {
        long t0 = System.nanoTime();
        try {
            return client
                    .query(new KvQuery(KvQuery.CMD_LIST_KEYS, null))
                    .get()
                    .getKeys();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("LIST_KEYS, {} ns.", Format.formatWithComma(System.nanoTime() - t0));
        }
    }

    public ClusterConfiguration getClusterConfiguration() {
        try {
            return client.getServers().get();
        } catch (Throwable e) {
            return null;
        }
    }


    public ServerStatus serverStatus(URI uri) {
        try {
            return client.serverStatus(uri).get();
        } catch (Throwable e) {
            return null;
        }
    }


    public URI leaderUri() {
        try {
            return client.getServers().thenApply(ClusterConfiguration::getLeader).get();
        } catch (Throwable e) {
            return null;
        }
    }

    public List<URI> getVoters() {
        try {
            return client.getServers().thenApply(ClusterConfiguration::getVoters).get();
        } catch (Throwable e) {
            return null;
        }
    }

    public List<URI> getVoters(URI uri) {
        try {
            return client.getServers(uri).thenApply(ClusterConfiguration::getVoters).get();
        } catch (Throwable e) {
            return null;
        }
    }

    public void convertRoll(URI uri, RaftServer.Roll roll) throws ExecutionException, InterruptedException {
        client.convertRoll(uri, roll).get();
    }

    public boolean updateVoters(List<URI> oldConfigs, List<URI> newConfigs) throws ExecutionException, InterruptedException {
        return client.updateVoters(oldConfigs, newConfigs).get();
    }

    public boolean waitForLeader(long timeoutMs) throws InterruptedException, ExecutionException {
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < timeoutMs) {
            if(null != client.getServers().get().getLeader()) {
                return true;
            }
        }
        return false;
    }

}