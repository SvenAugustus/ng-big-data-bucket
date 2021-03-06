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

package xyz.flysium.bd;

import org.apache.zookeeper.ZooKeeper;
import xyz.flysium.bd.configcenter.MyConfiguration;
import xyz.flysium.bd.configcenter.WatchAndCallback;
import xyz.flysium.bd.utils.ZookeeperUtils;

import java.io.IOException;

/**
 * 测试配置中心
 *
 * <p>
 * <li>修改 {@link #ZK_ADDRESS} 连接字符串为 可用的 Zookeeper集群 </li>
 * <li>执行 {@link #main} 方法后，zkCli.sh连接，依次执行：</li>
 * <pre>
 * create /AppConf/testConf "hello, config center !"
 * set /AppConf/testConf "new configurations!"
 * set /AppConf/testConf "new configurations  2!"
 * set /AppConf/testConf "new configurations  3!"
 * delete /AppConf/testConf
 * create /AppConf/testConf "hello, config center !"
 * set /AppConf/testConf "new configurations!"
 * delete /AppConf/testConf
 * </pre>
 *
 * @author Sven Augustus
 * @version 1.0
 */
public class TestConfigurationCenter {

  public static final String ZK_ADDRESS = "127.0.0.1:2281,127.0.0.1:2282,127.0.0.1:2283";

  /**
   * <p>
   * {@link WatchAndCallback} 提供了两类API:
   * <li>获取存在的配置信息，如果不存在则阻塞等待：{@link WatchAndCallback#awaitExistsReturn()}，{@link
   * WatchAndCallback#awaitExistsReturn(int)}</li>
   * <li>阻塞等待数据变化，一但变化则返回配置信息：{@link WatchAndCallback#awaitDataChangedReturn()}，{@link
   * WatchAndCallback#awaitDataChangedReturn(int)}</li>
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final ZooKeeper zooKeeper = ZookeeperUtils.newConnection(ZK_ADDRESS + "/AppConf", 3000, 1000);
    // 如果　Zookeeper 是带分组，那么下面的 path 其实真实应该加上分组，也就是 /testConf 其实应该是 /AppConf/testConf
    WatchAndCallback callback = new WatchAndCallback(zooKeeper, "/testConf");

    // 获取存在的配置信息，如果不存在则阻塞等待
    MyConfiguration configuration = callback.awaitExistsReturn();
    System.out.println("Configuration Exists： " + configuration.getConf());
    while (true) {
      // 阻塞等待数据变化，一但变化则返回配置信息
      configuration = callback.awaitDataChangedReturn();
      if (configuration != null) {
        System.out.println("Configuration Changed： " + configuration.getConf());
      }
    }
//    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      ZookeeperUtils.closeConnection(zooKeeper);
//    }));
  }

}
