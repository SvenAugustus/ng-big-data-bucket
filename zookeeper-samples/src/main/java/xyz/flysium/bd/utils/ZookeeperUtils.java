/*
 * MIT License
 *
 * Copyright (c) 2020 SvenAugustus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.flysium.bd.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper 工具类
 *
 * @author Sven Augustus
 * @version 1.0
 */
public final class ZookeeperUtils {

  private ZookeeperUtils() {
  }

  /**
   * 创建并连接 Zookeeper
   *
   * @param connectString  连接Zookeeper字符串，一般为Zookeeper的IP:PORT，以,连接，如 127.0.0.1:2281,127.0.0.1:2282,127.0.0.1:2283
   * @param sessionTimeout Session超时时间，单位毫秒
   * @param connectTimeout 连接超时时间，单位毫秒
   * @return Zookeeper客户端实例，已连接
   * @throws IOException 如果连接异常则抛出
   */
  public static ZooKeeper newConnection(String connectString, int sessionTimeout,
      int connectTimeout) throws IOException {
    final CountDownLatch latch = new CountDownLatch(1);
    DefaultWatcher watcher = new DefaultWatcher(latch);
    ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, watcher);
    try {
      if (connectTimeout > 0) {
        latch.await(connectTimeout, TimeUnit.MILLISECONDS);
      } else {
        latch.await();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return zooKeeper;
  }

  /**
   * 关闭连接 Zookeeper
   *
   * @param zooKeeper Zookeeper实例，已连接
   */
  public static void closeConnection(ZooKeeper zooKeeper) {
    if (zooKeeper == null) {
      return;
    }
    try {
      if (zooKeeper.getState().isConnected()) {
        zooKeeper.close();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * 创建 CuratorFramework 客户端 并连接 Zookeeper
   *
   * @param connectString  连接Zookeeper字符串，一般为Zookeeper的IP:PORT，以,连接，如 127.0.0.1:2281,127.0.0.1:2282,127.0.0.1:2283
   * @param sessionTimeout Session超时时间，单位毫秒
   * @param connectTimeout 连接超时时间，单位毫秒
   * @return CuratorFramework 客户端，已连接
   * @throws IOException 如果连接异常则抛出
   */
  public static CuratorFramework newCuratorClient(String connectString, int sessionTimeout,
      int connectTimeout) throws IOException, InterruptedException {
    // TODO
    // 初始休眠时间为 1000ms, 最大重试次数为 3
    RetryPolicy retry = new ExponentialBackoffRetry(1000, 3);
    // 创建一个客户端, sessionTimeout(ms)为 session 超时时间, connectTimeout(ms)为链接超时时间
    CuratorFramework client = CuratorFrameworkFactory
        .newClient(connectString, sessionTimeout, connectTimeout, retry);
    client.start();
    return client;
  }

  /**
   * 关闭连接 CuratorFramework 客户端
   *
   * @param client CuratorFramework 客户端
   */
  public static void closeCuratorClient(CuratorFramework client) {
    if (client == null) {
      return;
    }
    try {
      CloseableUtils.closeQuietly(client);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
