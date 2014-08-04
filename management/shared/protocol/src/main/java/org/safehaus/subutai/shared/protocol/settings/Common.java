package org.safehaus.subutai.shared.protocol.settings;


public class Common {

    public static final String UNKNOWN_LXC_PARENT_NAME = "UNKNOWN";
    public static final String BROADCAST_TOPIC = "BROADCAST_TOPIC";   //todo migrate to amqConfig
    public static final int REFRESH_UI_SEC = 3;
    public static final int LXC_AGENT_WAIT_TIMEOUT_SEC = 120;
    public static final int AGENT_FRESHNESS_MIN = 4;
    public static final int MAX_COMMAND_TIMEOUT_SEC = 100 * 60 * 60; // 100 hours
    public static final int INACTIVE_COMMAND_DROP_TIMEOUT_SEC = 24 * 60 * 60; // 24 hours
    public static final String IP_MASK = "^10\\.10\\.10\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$";
    public static final String HOSTNAME_REGEX =
            "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,"
                    + "61}[a-zA-Z0-9]))*$";

    //constant that can be converted into settings in the future
    public static final String BASE_CONTAINER_NAME = "base-container";
    public static final String MASTER_TEMPLATE_NAME = "master";
    public static final String MANAGEMENT_AGENT_HOSTNAME = "management";
    public static final String GIT_REPO_URL = "git@10.10.10.1:/opt/git/project.git";
    public static final String APT_REPO = "trusty";
    public static final String APT_REPO_PATH = "/repo/ksks/";
    public static final String APT_REPO_AMD64_PACKAGES_SUBPATH = "amd64/trusty/";
    public static final String TMP_DEB_PACKAGE_UNPACK_PATH = "/tmp";
    public static final String DEFAULT_LXC_ARCH = "amd64";
    public static final String PACKAGE_PREFIX = "ksks-";
    public static final String DEFAULT_DOMAIN_NAME = "intra.lan";
}
