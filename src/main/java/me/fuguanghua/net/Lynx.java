package me.fuguanghua.net;

import com.alibaba.fastjson.JSONObject;
import me.fuguanghua.net.components.Component;
import me.fuguanghua.net.components.ComponentFeature;
import me.fuguanghua.net.dispatch.Dispatcher;
import me.fuguanghua.net.event.EventComponent;
import me.fuguanghua.net.router.RouteComponent;
import me.fuguanghua.net.rpc.client.RpcClientComponent;
import me.fuguanghua.net.session.GateSessionService;
import me.fuguanghua.net.session.NodeSessionService;
import me.fuguanghua.net.utils.*;
import me.fuguanghua.net.utils.extend.ObjectReference;
import me.fuguanghua.net.utils.logback.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lynx {
    private static ObjectReference<Lynx> ref = new ObjectReference<>();
    public Logger logger;
    private boolean debug;
    private String rootConfigPath;
    private String envDir;
    private String[] basePackages;
    private String envName;
    private String nodeId;

    private JSONObject lynxConfig;
    private long startTime = System.currentTimeMillis();

    /**
     * 所有配置的服务器信息
     * key:nodeType, value:List<NodeInfo>
     */
    private Map<String, List<NodeInfo>> nodesInfo = new ConcurrentHashMap<>();

    /**
     * 当前服务器信息
     */
    private NodeInfo currentNode;

    /**
     * components collection
     * key:class, value:Component
     */
    private Map<Class<? extends Component>, Component> componentsMap = new LinkedHashMap<>();

    private GateSessionService sessionService = new GateSessionService();
    private NodeSessionService nodeSessionService = new NodeSessionService();

    private Schedule schedule;

    public static Lynx call() {
        return ref.get();
    }

    public static Lynx createApp(String configPath, String envName, String nodeId) throws Exception {
        Lynx lynx = new Lynx(configPath, "env", envName, nodeId);
        ref.set(lynx);
        lynx.init();
        return ref.get();
    }

    public static Lynx createApp(String configPath, String envDir, String envName, String nodeId) throws Exception {
        Lynx lynx = new Lynx(configPath, envDir, envName, nodeId);
        ref.set(lynx);
        lynx.init();
        return ref.get();
    }

    /**
     * @param configPath 配置文件根路径
     * @param envDir     基于configPath根路径的环境目录
     * @param nodeId     服务器结点id
     */
    private Lynx(String configPath, String envDir, String envName, String nodeId) throws Exception {
        this.rootConfigPath = configPath;
        this.envDir = envDir;
        this.envName = envName;
        this.nodeId = nodeId;

        if (this.rootConfigPath == null || this.envDir == null || this.envName == null || this.nodeId == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("lynx VM options config error...\n");
            sb.append("-Dlynx.config      \t 配置文件相对根路径.  eg. -Dlynx.config=config \n");
            sb.append("-Dlynx.env         \t运行环境名称.  eg. -Dlynx.env=local \n");
            sb.append("-Dlynx.nodeid      \t当前服务器的结点id.  eg. -Dlynx.nodeid=gate-1 \n");
            throw new Exception(sb.toString());
        }

        //check env path
        File path = new File(getEnvPath());
        if (!path.isDirectory()) {
            throw new Exception(String.format("file:%s  is not directory.", getEnvPath()));
        }
    }

    public String getEnvPath() {
        return PathUtils.combine(this.rootConfigPath, this.envDir, this.envName);
    }

    private void init() throws Exception {
        //init logback.xml configure
        String logbackPath = Paths.get(getEnvPath(), Constants.File.LOG_BACK).toString();
        LogUtils.loadFileConfig(logbackPath);

        this.logger = LoggerFactory.getLogger(Lynx.class);

        logger.info("========== node launcher ==========");
        logger.info("nodeId:{}, configPath:{}, envName:{}", this.nodeId, this.rootConfigPath, this.envName);

        loadLynxConfig();
        loadNodesConfig();

        // add default component
        addComponent(new EventComponent(this));
        addComponent(new RouteComponent(this));
    }

    private void loadLynxConfig() {
        this.lynxConfig = JsonUtils.read(getEnvPath(), Constants.File.LYNX_DOT_JSON);
        //start BasePackage
        this.basePackages = JsonUtils.readStringArray(this.lynxConfig, Constants.Component.BASE_PACKAGES);
        this.debug = this.lynxConfig.getBoolean("debug");
    }

    private void loadNodesConfig() throws Exception {
        JSONObject jsonObject = JsonUtils.read(getEnvPath(), Constants.File.NODES_DOT_JSON);
        this.nodesInfo.putAll(NodeInfo.getNodeMaps(jsonObject, this.nodeId));

        for (List<NodeInfo> list : nodesInfo.values()) {
            for (NodeInfo si : list) {
                if (si.getNodeId().equals(nodeId)) {
                    this.currentNode = si;
                }
            }
        }

        if (this.currentNode == null) {
            List<String> idList = new ArrayList<>();
            for (List<NodeInfo> list : nodesInfo.values()) {
                for (NodeInfo serverInfo : list) {
                    idList.add(serverInfo.getNodeId());
                }
            }

            throw new Exception(String.format("can not found current node. nodeId=%s, %s", nodeId, idList));
        }
    }

    public Lynx addComponent(Component component) {
        this.componentsMap.put(component.getClass(), component);
        return this;
    }

    public Lynx addComponent(ComponentFeature<Component> feature) {
        Component c = feature.createComponent(this);
        this.componentsMap.put(c.getClass(), c);
        return this;
    }

    public String[] packagesName() {
        return this.basePackages;
    }

    public String getRootConfigPath() {
        return this.rootConfigPath;
    }

    public String getEnvName() {
        return this.envName;
    }

    public JSONObject config(String nodeName) {
        return this.lynxConfig.getJSONObject(nodeName);
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public NodeInfo currentNode() {
        return this.currentNode;
    }

    public NodeInfo getNodeInfo(String serverType, String serverId) {
        Collection<NodeInfo> serverInfoCollection = getNodeInfoList(serverType);
        for (NodeInfo serverInfo : serverInfoCollection) {
            if (serverInfo.getNodeId().equals(serverId)) {
                return serverInfo;
            }
        }
        return null;
    }

    public List<NodeInfo> getNodeInfoList(String serverType) {
        return this.nodesInfo.getOrDefault(serverType, new ArrayList<>());
    }

    public RouteComponent route() {
        return getComponent(RouteComponent.class);
    }

    /**
     * get component by class info
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends Component> T getComponent(Class<? extends Component> clazz) {
        Component c = this.componentsMap.get(clazz);
        if (c == null) {
            final Optional<Component> result = this.componentsMap.values()
                    .stream()
                    .filter((value) -> clazz.isInstance(value))
                    .findFirst();
            if (result.isPresent()) {
                return (T) result.get();
            }
        }
        return (T) c;
    }

    public EventComponent event() {
        return getComponent((Class<? extends Component>) EventComponent.class);
    }

    public GateSessionService sessionService() {
        return this.sessionService;
    }

    public NodeSessionService nodeSessionService() {
        return nodeSessionService;
    }

    public Lynx setSchedule(int threadSize) {
        if (this.schedule == null) {
            this.schedule = new Schedule(threadSize, "lynx-schedule");
        }
        return this;
    }

    public Schedule schedule() {
        if (this.schedule == null) {
            setSchedule(4);
        }
        return this.schedule;
    }

    public Dispatcher dispatch() {
        return getComponent(Dispatcher.class);
    }

    public RpcClientComponent rpc() {
        return getComponent((Class<? extends Component>) RpcClientComponent.class);
    }
}
