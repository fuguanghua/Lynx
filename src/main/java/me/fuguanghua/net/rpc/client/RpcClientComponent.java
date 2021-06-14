package me.fuguanghua.net.rpc.client;

import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.components.NetComponent;
import me.fuguanghua.net.router.annotation.Route;
import me.fuguanghua.net.rpc.packet.HeartbeatPacket;
import me.fuguanghua.net.rpc.packet.RegisterNodePacket;
import me.fuguanghua.net.rpc.packet.RequestPacket;
import me.fuguanghua.net.session.NettySession;
import me.fuguanghua.net.utils.Constants;
import me.fuguanghua.net.utils.NodeInfo;
import me.fuguanghua.net.utils.PathResolver;
import me.fuguanghua.net.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RpcClientComponent extends NetComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientComponent.class);

    private Map<Class<?>, Route> rpcInfoMap = new HashMap<>();

    /**
     * key:nodeType, value:{key:nodeId,value:NettyRpcClient}
     */
    private Map<String, Map<String, NettyRpcClient>> clientMaps = new HashMap<>();

    private Map<String, SelectClientFactory> clientRouterMap = new HashMap<>();

    /**
     * random获取一个RpcClient
     */
    SelectClientFactory defaultClientRoute = (session, nodeType, request) -> {
        Map<String, NettyRpcClient> clients = clientMaps.get(nodeType);
        if (clients == null) {
            LOGGER.warn("can not find remote client for nodeType = {}", nodeType);
            return null;
        }
        final String index = RandomUtils.randomHit(clients.keySet());
        return clients.get(index);
    };

    public static final Integer CHECK_CONNECTION = 1000;

    private Lynx lynx;

    private String[] nodeTypes;

    public RpcClientComponent(Lynx lynx) {
        this.lynx = lynx;
    }

    public void add(String... nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void addRoute(String nodeType, SelectClientFactory route) {
        this.clientRouterMap.put(nodeType, route);
    }

    public Map<String, Map<String, NettyRpcClient>> getClientMaps() {
        return clientMaps;
    }

    public NettyRpcClient getClient(String nodeType, String nodeId) {
        Map<String, NettyRpcClient> map = clientMaps.get(nodeType);
        if (map == null) {
            return null;
        }
        return map.get(nodeId);
    }

    public SelectClientFactory getClientRoute(String nodeType) {
        SelectClientFactory clientRoute = this.clientRouterMap.get(nodeType);
        if (clientRoute == null) {
            return defaultClientRoute;
        }
        return clientRoute;
    }

    public void addClientSelect(String nodeType, SelectClientFactory selectClientFactory) {
        this.clientRouterMap.put(nodeType, selectClientFactory);
    }

    public void forward(NettySession session, RequestPacket packet) {
        SelectClientFactory clientSelect = this.getClientRoute(packet.nodeType());
        NettyRpcClient rpcClient = clientSelect.select(session, packet.nodeType(), packet);
        if (rpcClient == null) {
            LOGGER.warn("can not find remote client for routePacket = {}", packet);
            return;
        }
        rpcClient.writeRpcPacket(packet);
    }

    public <T> void async(Class<T> clazz, String nodeId, Consumer<T> consumer) {
        T rpc = getProxy(clazz, false, nodeId, 0, 0L, null);
        consumer.accept(rpc);
    }

    public <T> void async(Class<T> clazz, String nodeId, Consumer<T> consumer, int threadId, RpcCallback<?> callBack) {
        T rpc = getProxy(clazz, false, nodeId, threadId,0L, callBack);
        consumer.accept(rpc);
    }

    public <T> void async(Class<T> clazz, String nodeId, Consumer<T> consumer, int threadId, long hash, RpcCallback<?> callBack) {
        T rpc = getProxy(clazz, false, nodeId, threadId, hash, callBack);
        consumer.accept(rpc);
    }

    public <T> T getProxyRandom(Class<T> clazz) {
        return getProxy(clazz, true, "", 0,0L, null);
    }

    public <T> void asyncRandom(Class<T> clazz, Consumer<T> consumer) {
        T rpc = getProxy(clazz, true, "", 0, 0L, null);
        consumer.accept(rpc);
    }

    public <T> void asyncRandom(Class<T> clazz, Consumer<T> consumer, int threadId, RpcCallback<?> callBack) {
        T rpc = getProxy(clazz, true, "", threadId, 0L, callBack);
        consumer.accept(rpc);
    }

    public <T> void asyncRandom(Class<T> clazz, Consumer<T> consumer, int threadId, long hash, RpcCallback<?> callBack) {
        T rpc = getProxy(clazz, true, "", threadId, hash, callBack);
        consumer.accept(rpc);
    }

    public <T> T getProxy(Class<T> clazz, String nodeId) {
        return getProxy(clazz, false, nodeId, 0, 0L, null);
    }

    protected <T> T getProxy(Class<T> clazz, boolean random, String nodeId, int threadId, long hash, RpcCallback<?> callback) {
        Route rpcInfo = rpcInfoMap.get(clazz);
        Map<String, NettyRpcClient> clientMaps = this.clientMaps.get(rpcInfo.nodeType());
        if (clientMaps == null || clientMaps.isEmpty()) {
            LOGGER.info("nodeType:{} not found. clazz={}", rpcInfo.nodeType(), clazz);
            return null;
        }

        //use @Route annoation defaultThreadId
        if (threadId < 1) {
            threadId = rpcInfo.defaultThreadId();
        }

        if (random) {
            final String index = RandomUtils.randomHit(clientMaps.keySet());
            NettyRpcClient rpcClient = clientMaps.get(index);
            return rpcClient.getProxy(clazz, rpcInfo.nodeType(), threadId, hash, callback);
        }

        NettyRpcClient rpcClient = clientMaps.get(nodeId);
        if (rpcClient == null) {
            LOGGER.error("nodeType:{} nodeId:{} remote client not found.", rpcInfo.nodeType(), nodeId);
            return null;
        }
        if (!rpcClient.isConnect()) {
            LOGGER.error("nodeType:{} nodeId:{} remote client not connect.", rpcInfo.nodeType(), nodeId);
            return null;
        }
        return rpcClient.getProxy(clazz, rpcInfo.nodeType(), threadId, hash, callback);
    }

    @Override
    public String name() {
        return Constants.Net.RPC_CLIENT;
    }

    @Override
    public void start() {
        Collection<Class<?>> collection = PathResolver.scanPkgWithAnnotation(Route.class, lynx.packagesName());
        collection.forEach(clazz -> {
            Route route = clazz.getAnnotation(Route.class);
            this.rpcInfoMap.put(clazz, route);
            LOGGER.debug("register rpc interface. class = {}", clazz.getSimpleName());
        });
    }

    @Override
    public void afterStart() {
        //add node
        for (String nodeType : nodeTypes) {
            Collection<NodeInfo> infoList = lynx.getNodeInfoList(nodeType);
            for (NodeInfo info : infoList) {
                Map<String, NettyRpcClient> clientMaps = this.clientMaps.getOrDefault(info.getNodeType(), new HashMap<>());
                if (clientMaps.isEmpty()) {
                    this.clientMaps.put(info.getNodeType(), clientMaps);
                }
                clientMaps.put(info.getNodeId(), NettyRpcClient.valueOfDefault(lynx, info));
            }
        }

        scheduleCheck();
    }

    private void scheduleCheck() {
        // check connection
        lynx.schedule().addEveryMillisecond(() -> {
            for (Map<String, NettyRpcClient> clients : clientMaps.values()) {
                for (NettyRpcClient c : clients.values()) {
                    if (!c.isConnect()) {
                        try {
                            c.connect(true);
                            if (c.isConnect()) {
                                sendRegister(c);
                            }
                        } catch (Exception ex) {
                            LOGGER.error("{}, message:{}", c, ex.getMessage());
                        }
                    }
                }
            }
        }, CHECK_CONNECTION);

        lynx.schedule().addEveryMillisecond(() -> {
            for (Map<String, NettyRpcClient> clients : clientMaps.values()) {
                for (NettyRpcClient c : clients.values()) {
                    try {
                        if (c.isConnect()) {
                            HeartbeatPacket packet = new HeartbeatPacket(lynx.currentNode().getNodeType(), lynx.currentNode().getNodeId());
                            c.writeRpcPacket(packet);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("{}, message:{}", c, ex.getMessage());
                    }
                }
            }
        }, 60 * 1000);
    }

    private void sendRegister(NettyRpcClient client) {
        RegisterNodePacket packet = new RegisterNodePacket();
        packet.nodeType = lynx.currentNode().getNodeType();
        packet.nodeId = lynx.currentNode().getNodeId();
        client.writeRpcPacket(packet);
        LOGGER.info("send register nodeType:{}, nodeId:{}", client.nodeType(), client.nodeId());
    }

    @Override
    public void stop() {
        clientMaps.values().forEach(clients -> {
            clients.values().forEach(item -> item.stop());
        });
    }

    @Override
    public void beforeStop() {

    }


}
