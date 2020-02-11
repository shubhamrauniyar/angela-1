/*
 * The contents of this file are subject to the Terracotta Public License Version
 * 2.0 (the "License"); You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://terracotta.org/legal/terracotta-public-license.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Covered Software is Angela.
 *
 * The Initial Developer of the Covered Software is
 * Terracotta, Inc., a Software AG company
 */

import org.awaitility.Awaitility;
import org.junit.Test;
import org.terracotta.angela.client.ClientArray;
import org.terracotta.angela.client.ClientArrayFuture;
import org.terracotta.angela.client.ClusterFactory;
import org.terracotta.angela.client.Tsa;
import org.terracotta.angela.client.config.ConfigurationContext;
import org.terracotta.angela.common.TerracottaServerState;
import org.terracotta.angela.common.tcconfig.TerracottaServer;
import org.terracotta.angela.common.topology.ClientArrayTopology;
import org.terracotta.angela.common.topology.LicenseType;
import org.terracotta.angela.common.topology.PackageType;
import org.terracotta.angela.common.topology.Topology;

import java.util.Collection;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.terracotta.angela.client.config.custom.CustomConfigurationContext.customConfigurationContext;
import static org.terracotta.angela.common.clientconfig.ClientArrayConfig.newClientArrayConfig;
import static org.terracotta.angela.common.distribution.Distribution.distribution;
import static org.terracotta.angela.common.tcconfig.TcConfig.tcConfig;
import static org.terracotta.angela.common.topology.Version.version;

/**
 * @author Aurelien Broszniowski
 */
public class GettingStarted {
  private static String EHCACHE_OS_VERSION = "3.8.1";

  @Test
  public void configureCluster() throws Exception {
    // tag::configureCluster[]
    ConfigurationContext configContext = customConfigurationContext() // <1>
        .tsa(tsa -> tsa // <2>
            .topology(new Topology( // <3>
                distribution(version(EHCACHE_OS_VERSION), PackageType.KIT, LicenseType.EHCACHE_OS), // <4>
                tcConfig(version(EHCACHE_OS_VERSION), getClass().getResource("/tc-config-a.xml")))) // <5>
        );

    ClusterFactory factory = new ClusterFactory("GettingStarted::configureCluster", configContext); // <6>
    Tsa tsa = factory.tsa() // <7>
        .startAll(); // <8>

    factory.close(); // <9>
    // end::configureCluster[]
  }

  @Test
  public void showTsaApi() throws Exception {
    Topology topology = new Topology(
        distribution(version(EHCACHE_OS_VERSION), PackageType.KIT, LicenseType.EHCACHE_OS),
        tcConfig(version(EHCACHE_OS_VERSION), getClass().getResource("/tc-config-ap.xml"))
    );
    ConfigurationContext configContext = customConfigurationContext().tsa(tsa -> tsa.topology(topology));

    try (ClusterFactory factory = new ClusterFactory("GettingStarted::showTsaApi", configContext)) {
      // tag::showTsaApi[]
      Tsa tsa = factory.tsa() // <1>
          .startAll(); // <2>

      TerracottaServer active = tsa.getActive(); // <3>
      Collection<TerracottaServer> actives = tsa.getActives(); // <4>
      TerracottaServer passive = tsa.getPassive(); // <5>
      Collection<TerracottaServer> passives = tsa.getPassives(); // <6>

      tsa.stopAll(); // <7>

      tsa.start(active); // <8>
      tsa.start(passive);

      tsa.stop(active); // <9>
      Callable<TerracottaServerState> serverState = () -> tsa.getState(passive); // <10>
      Awaitility.await()
          .pollInterval(1, SECONDS)
          .atMost(15, SECONDS)
          .until(serverState, is(TerracottaServerState.STARTED_AS_ACTIVE));
      // end::showTsaApi[]
    }
  }

  @Test
  public void runClient() throws Exception {
    // tag::runClient[]
    ConfigurationContext configContext = customConfigurationContext()
        .clientArray(clientArray -> clientArray // <1>
            .clientArrayTopology(new ClientArrayTopology( // <2>
                distribution(version(EHCACHE_OS_VERSION), PackageType.KIT, LicenseType.EHCACHE_OS), // <3>
                newClientArrayConfig().host("localhost-1", "localhost").host("localhost-2", "localhost")) // <4>
            )
        );
    ClusterFactory factory = new ClusterFactory("GettingStarted::runClient", configContext);
    ClientArray clientArray = factory.clientArray(); // <5>
    ClientArrayFuture f = clientArray.executeOnAll((context) -> System.out.println("Hello")); // <6>
    f.get(); // <7>

    factory.close();
    // end::runClient[]
  }
}
