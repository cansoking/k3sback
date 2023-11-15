package prv.gdk.kubedash.entity;

public class DeploymentInfo {
    private String DeploymentName;
    private String Image;
    private Integer ContainerPort;
    private Integer ServicePort;
    private Integer NodePort;

    public String getDeploymentName() {
        return DeploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        DeploymentName = deploymentName;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public Integer getContainerPort() {
        return ContainerPort;
    }

    public void setContainerPort(Integer containerPort) {
        ContainerPort = containerPort;
    }

    public Integer getServicePort() {
        return ServicePort;
    }

    public void setServicePort(Integer servicePort) {
        ServicePort = servicePort;
    }

    public Integer getNodePort() {
        return NodePort;
    }

    public void setNodePort(Integer nodePort) {
        NodePort = nodePort;
    }
}
