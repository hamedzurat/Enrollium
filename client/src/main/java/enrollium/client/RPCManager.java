package enrollium.client;

import client.ClientRPC;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RPCManager {
    private static RPCManager instance;
    @Getter
    private        ClientRPC  client;
    private        boolean    initialized = false;

    private RPCManager() {}

    public static RPCManager getInstance() {
        if (instance == null) instance = new RPCManager();

        return instance;
    }

    public void initializeConnection(String email, String password) {
        if (initialized) throw new IllegalStateException("RPC connection already initialized");

        client = new ClientRPC(email, password);
        client.start();
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void shutdown() {
        if (client != null) client.close();

        initialized = false;
    }
}
