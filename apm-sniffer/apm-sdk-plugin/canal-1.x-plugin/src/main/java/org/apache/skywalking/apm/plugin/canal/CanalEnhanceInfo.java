package org.apache.skywalking.apm.plugin.canal;

public class CanalEnhanceInfo {
    private String url;
    private String destination;

    /* GET SET METHODS */
    public String getUrl() {
        return url;
    }

    public CanalEnhanceInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public CanalEnhanceInfo setDestination(String destination) {
        this.destination = destination;
        return this;
    }

}
