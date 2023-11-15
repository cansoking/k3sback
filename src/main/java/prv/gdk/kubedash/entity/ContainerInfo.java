package prv.gdk.kubedash.entity;

public class ContainerInfo {
    private String containerName;
    private String containerImage;
    private int port;


    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    // 省略 getter 和 setter 方法
}
