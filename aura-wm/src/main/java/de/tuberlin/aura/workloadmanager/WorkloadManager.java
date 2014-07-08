package de.tuberlin.aura.workloadmanager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import de.tuberlin.aura.core.config.IConfigFactory;
import de.tuberlin.aura.core.config.IConfig;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.type.FileArgumentType;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.internal.HelpScreenException;
import org.apache.log4j.Logger;

import de.tuberlin.aura.core.common.eventsystem.Event;
import de.tuberlin.aura.core.common.eventsystem.IEventHandler;
import de.tuberlin.aura.core.descriptors.DescriptorFactory;
import de.tuberlin.aura.core.descriptors.Descriptors.MachineDescriptor;
import de.tuberlin.aura.core.iosystem.IOEvents;
import de.tuberlin.aura.core.iosystem.IOManager;
import de.tuberlin.aura.core.iosystem.RPCManager;
import de.tuberlin.aura.core.protocols.ClientWMProtocol;
import de.tuberlin.aura.core.topology.Topology.AuraTopology;
import de.tuberlin.aura.core.zookeeper.ZookeeperHelper;


// TODO: introduce the concept of a session, that allows to submit multiple queries...

public class WorkloadManager implements ClientWMProtocol {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private static final Logger LOG = Logger.getLogger(WorkloadManager.class);

    private final MachineDescriptor machineDescriptor;

    private final IOManager ioManager;

    private final RPCManager rpcManager;

    private final InfrastructureManager infrastructureManager;

    private final Map<UUID, TopologyController> registeredTopologies;

    private final Map<UUID, Set<UUID>> registeredSessions;

    private final WorkloadManagerContext managerContext;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public WorkloadManager(IConfig config) {
        final String zkServer = config.getString("zookeeper.server.address");

        // sanity check.
        ZookeeperHelper.checkConnectionString(zkServer);

        this.machineDescriptor = DescriptorFactory.createMachineDescriptor(config, "wm");

        this.ioManager = new IOManager(this.machineDescriptor, null);

        this.rpcManager = new RPCManager(ioManager);

        this.infrastructureManager = InfrastructureManager.getInstance(zkServer, machineDescriptor);

        this.registeredTopologies = new ConcurrentHashMap<>();

        this.registeredSessions = new ConcurrentHashMap<>();

        rpcManager.registerRPCProtocolImpl(this, ClientWMProtocol.class);

        ioManager.addEventListener(IOEvents.ControlEventType.CONTROL_EVENT_REMOTE_TASK_STATE_UPDATE, new IEventHandler() {

            @Override
            public void handleEvent(Event e) {
                final IOEvents.TaskControlIOEvent event = (IOEvents.TaskControlIOEvent) e;
                registeredTopologies.get(event.getTopologyID()).dispatchEvent(event);
            }
        });

        ioManager.addEventListener(IOEvents.ControlEventType.CONTROL_EVENT_REMOTE_TASK_TRANSITION, new IEventHandler() {

            @Override
            public void handleEvent(Event e) {
                final IOEvents.TaskControlIOEvent event = (IOEvents.TaskControlIOEvent) e;
                registeredTopologies.get(event.getTopologyID()).getTopologyFSMDispatcher().dispatchEvent((Event) event.getPayload());
            }
        });

        this.managerContext = new WorkloadManagerContext(this, ioManager, rpcManager, infrastructureManager);
    }

    // ---------------------------------------------------
    // Public.
    // ---------------------------------------------------

    /**
     * 
     * @param sessionID
     */
    @Override
    public void openSession(final UUID sessionID) {
        // sanity check.
        if (sessionID == null)
            throw new IllegalArgumentException("sessionID == null");

        if (registeredSessions.containsKey(sessionID))
            throw new IllegalStateException("session with this ID [" + sessionID.toString() + "] already exists");

        // register a new session for a examples.
        registeredSessions.put(sessionID, new HashSet<UUID>());
        LOG.info("OPENED SESSION [" + sessionID + "]");
    }

    /**
     * @param sessionID
     * @param topology
     */
    @Override
    public void submitTopology(final UUID sessionID, final AuraTopology topology) {
        // sanity check.
        if (topology == null)
            throw new IllegalArgumentException("topology == null");

        if (registeredTopologies.containsKey(topology.name))
            throw new IllegalStateException("topology already submitted");

        LOG.info("TOPOLOGY '" + topology.name + "' SUBMITTED");
        registerTopology(sessionID, topology).assembleTopology(topology);
    }

    /**
     * 
     * @param sessionID
     */
    @Override
    public void closeSession(final UUID sessionID) {
        // sanity check.
        if (sessionID == null)
            throw new IllegalArgumentException("sessionID == null");

        if (!registeredSessions.containsKey(sessionID))
            throw new IllegalStateException("session with this ID [" + sessionID.toString() + "] does not exist");

        // register a new session for a examples.
        final Set<UUID> assignedTopologies = registeredSessions.get(sessionID);

        for (final UUID topologyID : assignedTopologies) {
            final TopologyController topologyController = registeredTopologies.get(topologyID);
            // topologyController.cancelTopology(); // TODO: Not implemented yet!
            // unregisterTopology(topologyID);
        }

        LOG.info("CLOSED SESSION [" + sessionID + "]");
    }

    /**
     *
     * @param sessionID
     * @param topologyID
     * @param topology
     */
    @Override
    public void submitToTopology(UUID sessionID, UUID topologyID, AuraTopology topology) {
        // sanity check.
        if(sessionID == null)
            throw new IllegalArgumentException("sessionID == null");
        if(topologyID == null)
            throw new IllegalArgumentException("topologyID == null");
        if(topology == null)
            throw new IllegalArgumentException("topology == null");

        final TopologyController topologyController = this.registeredTopologies.get(topologyID);

        if(topologyController == null)
            throw new IllegalStateException("topologyController == null");

        topologyController.assembleTopology(topology);
    }

    /**
     *
     * @param sessionID
     * @param topologyID1
     * @param taskNodeID1
     * @param topologyID2
     * @param taskNodeID2
     */
    /*@Override
    public void connectTopologies(final UUID sessionID, final UUID topologyID1, final UUID taskNodeID1, final UUID topologyID2, final UUID taskNodeID2) {
        // sanity check.
        if(topologyID1 == null)
            throw new IllegalArgumentException("topologyID1 == null");
        if(taskNodeID1 == null)
            throw new IllegalArgumentException("taskNodeID1 == null");
        if(topologyID2 == null)
            throw new IllegalArgumentException("topologyID2 == null");
        if(taskNodeID2 == null)
            throw new IllegalArgumentException("taskNodeID2 == null");


        final TopologyController topologyControllerSrc = this.registeredTopologies.get(topologyID1);

        if(topologyController == null)
            throw new IllegalStateException("topologyController == null");

        topologyController.createOutputGateAndConnect(taskNodeID1);
    }*/

    /**
     * @param sessionID
     * @param topology
     * @return
     */
    public TopologyController registerTopology(final UUID sessionID, final AuraTopology topology) {
        // sanity check.
        if (topology == null)
            throw new IllegalArgumentException("topology == null");

        final TopologyController topologyController = new TopologyController(managerContext, topology.topologyID);
        registeredTopologies.put(topology.topologyID, topologyController);
        return topologyController;
    }

    /**
     * 
     * @param topologyID
     */
    public void unregisterTopology(final UUID topologyID) {
        // sanity check.
        if (topologyID == null)
            throw new IllegalArgumentException("topologyID == null");

        if (registeredTopologies.remove(topologyID) == null)
            throw new IllegalStateException("topologyID not found");

        for (final Set<UUID> assignedTopologies : registeredSessions.values()) {
            if (assignedTopologies.contains(topologyID))
                assignedTopologies.remove(topologyID);
        }
    }

    // ---------------------------------------------------
    // Entry Point.
    // ---------------------------------------------------

    /**
     * TaskManager entry point.
     *
     * @param args
     */
    public static void main(final String[] args) {
        // construct base argument parser
        ArgumentParser parser = getArgumentParser();

        try {
            // parse the arguments and store them as system properties
            for (Map.Entry<String, Object> e : parser.parseArgs(args).getAttrs().entrySet()) {
                System.setProperty(e.getKey(), e.getValue().toString());
            }
            // load configuration
            IConfig config = IConfigFactory.load();

            // start the workload manager
            long start = System.nanoTime();
            new WorkloadManager(config);
            LOG.info("WM startup: " + Long.toString(Math.abs(System.nanoTime() - start) / 1000000) + " ms");
        } catch (HelpScreenException e) {
            parser.handleError(e);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (Throwable e) {
            System.err.println(String.format("Unexpected error: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private static ArgumentParser getArgumentParser() {
        //@formatter:off
        ArgumentParser parser = ArgumentParsers.newArgumentParser("aura-wm")
                .defaultHelp(true)
                .description("AURA WorkloadManager.");

        parser.addArgument("--config")
                .type(new FileArgumentType().verifyIsDirectory().verifyCanRead())
                .dest("aura.path.config")
                .setDefault("config")
                .metavar("PATH")
                .help("config folder");
        parser.addArgument("--log")
                .type(new FileArgumentType().verifyIsDirectory().verifyCanRead())
                .dest("aura.path.log")
                .setDefault("log")
                .metavar("PATH")
                .help("log folder");
        parser.addArgument("--zookeeper-url")
                .type(String.class)
                .dest("zookeeper.server.address")
                .metavar("URL")
                .help("zookeeper server URL");
        parser.addArgument("--data-port")
                .type(Integer.class)
                .dest("wm.io.tcp.port")
                .metavar("PORT")
                .help("port for data transfer");
        parser.addArgument("--control-port")
                .type(Integer.class)
                .dest("wm.io.rpc.port")
                .metavar("PORT")
                .help("port for control messages");
        //@formatter:on

        return parser;
    }
}
