package com.xw.cloud.bean;

import java.util.List;

public class PodInfo {

    private String podName;
    private String podNamespace;
    private String podNodeName;
    private List<ContainerInfo> containerInfoList;


    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getPodNamespace() {
        return podNamespace;
    }

    public void setPodNamespace(String podNamespace) {
        this.podNamespace = podNamespace;
    }


    public List<ContainerInfo> getContainerInfoList() {
        return containerInfoList;
    }

    public void setContainerInfoList(List<ContainerInfo> containerInfoList) {
        this.containerInfoList = containerInfoList;
    }

    public String getPodNodeName() {
        return podNodeName;
    }

    public void setPodNodeName(String podNodeName) {
        this.podNodeName = podNodeName;
    }
}
