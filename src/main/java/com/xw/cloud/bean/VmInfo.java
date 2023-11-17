package com.xw.cloud.bean;

public class VmInfo {

    private String virtualMachineIp;
    private String userName;
    private String userPassword;

    public String getVirtualMachineIp() {
        return virtualMachineIp;
    }

    public void setVirtualMachineIp(String virtualMachineIp) {
        this.virtualMachineIp = virtualMachineIp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
