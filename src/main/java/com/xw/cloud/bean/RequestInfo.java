package com.xw.cloud.bean;

public class RequestInfo {
    private PodInfo podInfo;
    private PvcInfo pvcInfo;
    private ContainerInfo containerInfo;
    private ImageInfo imageInfo;
    private PvInfo pvInfo;
    private VmInfo vmInfo;
    private DeploymentInfo deploymentInfo;

    public RequestInfo() {
    }

    public RequestInfo(PodInfo podInfo, PvcInfo pvcInfo, ContainerInfo containerInfo, ImageInfo imageInfo, PvInfo pvInfo, VmInfo vmInfo, DeploymentInfo deploymentInfo) {
        this.podInfo = podInfo;
        this.pvcInfo = pvcInfo;
        this.containerInfo = containerInfo;
        this.imageInfo = imageInfo;
        this.pvInfo = pvInfo;
        this.vmInfo = vmInfo;
        this.deploymentInfo = deploymentInfo;
    }

    public PodInfo getPodInfo() {
        return podInfo;
    }

    public void setPodInfo(PodInfo podInfo) {
        this.podInfo = podInfo;
    }

    public PvcInfo getPvcInfo() {
        return pvcInfo;
    }

    public void setPvcInfo(PvcInfo pvcInfo) {
        this.pvcInfo = pvcInfo;
    }

    public ContainerInfo getContainerInfo() {
        return containerInfo;
    }

    public void setContainerInfo(ContainerInfo containerInfo) {
        this.containerInfo = containerInfo;
    }

    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }

    public PvInfo getPvInfo() {
        return pvInfo;
    }

    public void setPvInfo(PvInfo pvInfo) {
        this.pvInfo = pvInfo;
    }

    public VmInfo getVmInfo() {
        return vmInfo;
    }

    public void setVmInfo(VmInfo vmInfo) {
        this.vmInfo = vmInfo;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public void setDeploymentInfo(DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }
}
