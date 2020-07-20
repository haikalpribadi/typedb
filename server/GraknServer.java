/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package grakn.core.server;

import com.datastax.oss.driver.api.core.CqlSession;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.core.common.config.Config;
import grakn.core.common.config.ConfigKey;
import grakn.core.common.config.SystemProperty;
import grakn.core.common.exception.ErrorMessage;
import grakn.core.server.keyspace.KeyspaceManager;
import grakn.core.server.rpc.KeyspaceService;
import grakn.core.server.rpc.OpenRequest;
import grakn.core.server.rpc.ServerOpenRequest;
import grakn.core.server.rpc.SessionService;
import grakn.core.server.session.HadoopGraphFactory;
import grakn.core.server.session.JanusGraphFactory;
import grakn.core.server.session.SessionFactory;
import grakn.core.server.util.LockManager;
import grakn.core.server.util.PIDManager;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The main class of the 'grakn' command. This class is not a class responsible
 * for booting up the real command, but rather the command itself.
 *
 * Main class in charge to start gRPC server and initialise Grakn system keyspace.
 */
public class GraknServer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(GraknServer.class);
    private static final int GRPC_EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() * 2; // default Netty way of assigning threads, probably expose in config in future
    private static final int ELG_THREADS = 4; // this could also be 1, but to avoid risks set it to 4, probably expose in config in future

    private final io.grpc.Server serverRPC;

    public GraknServer(io.grpc.Server serverRPC) {
        // Lock provider
        this.serverRPC = serverRPC;
    }

    private void start() throws IOException {
        serverRPC.start();
    }

    // NOTE: this method is used by Grakn KGMS and should be kept public
    private void awaitTermination() throws InterruptedException {
        serverRPC.awaitTermination();
    }

    @Override
    public void close() {
        try {
            serverRPC.shutdown();
            serverRPC.awaitTermination();
        } catch (InterruptedException e) {
            LOG.error("Exception while closing Server:", e);
            Thread.currentThread().interrupt();
        }
    }

    public static class Arguments {
        private final Tracing grablTracing;

        private Arguments(String[] args) {
            Options options = new Options();

            options.addOption(Option.builder("t")
                                      .longOpt("tracing-enabled")
                                      .desc("Enable grabl tracing")
                                      .required(false)
                                      .type(Boolean.class)
                                      .build());

            options.addOption(Option.builder()
                                      .longOpt("tracing-uri")
                                      .hasArg()
                                      .desc("Grabl tracing URI")
                                      .required(false)
                                      .type(String.class)
                                      .build());

            options.addOption(Option.builder()
                                      .longOpt("tracing-username")
                                      .hasArg()
                                      .desc("Grabl tracing username")
                                      .required(false)
                                      .type(String.class)
                                      .build());

            options.addOption(Option.builder()
                                      .longOpt("tracing-access-token")
                                      .hasArg()
                                      .desc("Grabl tracing access-token")
                                      .required(false)
                                      .type(String.class)
                                      .build());

            CommandLineParser parser = new DefaultParser();
            CommandLine arguments;
            try {
                arguments = parser.parse(options, args);
            } catch (ParseException e) {
                (new HelpFormatter()).printHelp("Grakn options", options);
                throw new RuntimeException(e.getMessage());
            }

            if (arguments.hasOption("tracing-enabled")) {
                grablTracing = new Tracing(
                        arguments.getOptionValue("tracing-uri"),
                        arguments.getOptionValue("tracing-username"),
                        arguments.getOptionValue("tracing-access-token")
                );
            } else {
                grablTracing = null;
            }
        }

        boolean isGrablTracing() {
            return grablTracing != null;
        }

        Tracing getGrablTracing() {
            return grablTracing;
        }

        static class Tracing {
            private final String uri;
            private final String username;
            private final String accessToken;

            private Tracing(String uri, String username, String accessToken) {
                this.uri = uri;
                this.username = username;
                this.accessToken = accessToken;
            }

            String getUri() {
                return uri;
            }

            String getUsername() {
                return username;
            }

            String getAccessToken() {
                return accessToken;
            }

            boolean isSecureMode() {
                return username != null;
            }
        }
    }

    /**
     * Create a Server configured for Grakn Core.
     *
     * Build a GrpcServer using the Netty default builder.
     * The Netty builder accepts 3 thread executors (threadpools):
     * - Boss Event Loop Group  (a.k.a. bossEventLoopGroup() )
     * - Worker Event Loop Group ( a.k.a. workerEventLoopGroup() )
     * - Application Executor (a.k.a. executor() )
     * <p>
     * The Boss group can be the same as the worker group.
     * It's purpose is to accept calls from the network, and create Netty channels (not gRPC Channels) to handle the socket.
     * <p>
     * Once the Netty channel has been created it gets passes to the Worker Event Loop Group.
     * This is the threadpool dedicating to doing socket read() and write() calls.
     * <p>
     * The last thread group is the application executor, also called the "app thread".
     * This is where the gRPC stubs do their main work.
     * It is for handling the callbacks that bubble up from the network thread.
     * <p>
     * Note from grpc-java developers:
     * Most people should use either reuse the same boss event loop group as the worker group.
     * Barring this, the boss eventloop group should be a single thread, since it does very little work.
     * For the app thread, users should provide a fixed size thread pool, as the default unbounded cached threadpool
     * is not the most efficient, and can be dangerous in some circumstances.
     * <p>
     * More info here: https://groups.google.com/d/msg/grpc-io/LrnAbWFozb0/VYCVarkWBQAJ
     *
     * @return a Server instance configured for Grakn Core
     */
    static GraknServer createServer(Arguments arguments) {
        // Grakn Server configuration
        Config config = Config.create();

        JanusGraphFactory janusGraphFactory = new JanusGraphFactory(config);

        // locks
        LockManager lockManager = new LockManager();

        Integer storagePort = config.getProperty(ConfigKey.STORAGE_PORT);
        String storageHostname = config.getProperty(ConfigKey.STORAGE_HOSTNAME);
        // CQL cluster used by KeyspaceManager to fetch all existing keyspaces
        CqlSession cqlSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(storageHostname, storagePort))
                .withLocalDatacenter("datacenter1")
                .build();

        HadoopGraphFactory hadoopGraphFactory = new HadoopGraphFactory(config);
        SessionFactory sessionFactory = new SessionFactory(lockManager, janusGraphFactory, hadoopGraphFactory, config);
        KeyspaceManager keyspaceManager = new KeyspaceManager(cqlSession, janusGraphFactory, sessionFactory);

        if (arguments.isGrablTracing()) {
            GraknServer.LOG.info("Grabl tracing is enabled!");

            GrablTracing grablTracingClient;
            if (arguments.getGrablTracing().isSecureMode()) {
                GraknServer.LOG.info("Using Grabl tracing secure mode");
                grablTracingClient = GrablTracing.withLogging(GrablTracing.tracing(
                        arguments.getGrablTracing().getUri(),
                        arguments.getGrablTracing().getUsername(),
                        arguments.getGrablTracing().getAccessToken()
                ));
            } else {
                GraknServer.LOG.warn("Using Grabl tracing UNSECURED mode!!!");
                grablTracingClient = GrablTracing.withLogging(GrablTracing.tracing(arguments.getGrablTracing().getUri()));
            }
            GrablTracingThreadStatic.setGlobalTracingClient(grablTracingClient);
            GraknServer.LOG.info("Completed tracing setup");
        }

        // create gRPC server
        int grpcPort = config.getProperty(ConfigKey.GRPC_PORT);
        OpenRequest requestOpener = new ServerOpenRequest(sessionFactory);
        SessionService sessionService = new SessionService(requestOpener);
        Runtime.getRuntime().addShutdownHook(new Thread(sessionService::shutdown, "session-service-shutdown"));
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(ELG_THREADS, new ThreadFactoryBuilder().setNameFormat("grpc-ELG-handler-%d").build());
        ExecutorService grpcExecutorService = Executors.newFixedThreadPool(GRPC_EXECUTOR_THREADS, new ThreadFactoryBuilder().setNameFormat("grpc-request-handler-%d").build());
        GraknServer server = new GraknServer(NettyServerBuilder.forPort(grpcPort)
                                           .executor(grpcExecutorService)
                                           .workerEventLoopGroup(eventLoopGroup)
                                           .bossEventLoopGroup(eventLoopGroup)
                                           .maxConnectionIdle(1, TimeUnit.HOURS)
                                           .channelType(NioServerSocketChannel.class)
                                           .addService(sessionService)
                                           .addService(new KeyspaceService(keyspaceManager))
                                           .build());

        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "grakn-server-shutdown"));
        return server;
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) ->
                                                          LOG.error(ErrorMessage.UNCAUGHT_EXCEPTION.getMessage(t.getName()), e));

        try {
            String graknPidFileProperty = Optional.ofNullable(SystemProperty.GRAKN_PID_FILE.value())
                    .orElseThrow(() -> new RuntimeException(ErrorMessage.GRAKN_PIDFILE_SYSTEM_PROPERTY_UNDEFINED.getMessage()));

            Path pidfile = Paths.get(graknPidFileProperty);
            PIDManager pidManager = new PIDManager(pidfile);
            pidManager.trackGraknPid();

            // Start Server with timer
            Stopwatch timer = Stopwatch.createStarted();
            GraknServer server = GraknServer.createServer(new Arguments(args));
            server.start();
            LOG.info("Grakn started in {}", timer.stop());
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                // grakn server stop is called
                server.close();
                Thread.currentThread().interrupt();
            }
        } catch (RuntimeException | IOException e) {
            LOG.error(ErrorMessage.UNCAUGHT_EXCEPTION.getMessage(e.getMessage()), e);
            System.err.println(ErrorMessage.UNCAUGHT_EXCEPTION.getMessage(e.getMessage()));
        }
    }
}
