package com.xw.cloud.bean;

public class PvcInfo {
    private String pvcName;
    private String pvcNamespace;
    private String pvcQuantity;

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public String getPvcNamespace() {
        return pvcNamespace;
    }

    public void setPvcNamespace(String pvcNamespace) {
        this.pvcNamespace = pvcNamespace;
    }

    public String getPvcQuantity() {
        return pvcQuantity;
    }

    public void setPvcQuantity(String pvcQuantity) {
        this.pvcQuantity = pvcQuantity;
    }
}
