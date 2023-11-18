package com.xw.cloud.controller;

import com.jcraft.jsch.*;
import com.xw.cloud.bean.PvInfo;
import com.xw.cloud.bean.PvcInfo;
import com.xw.cloud.bean.RequestInfo;
import com.xw.cloud.bean.VmInfo;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

@Controller
@CrossOrigin
@RequestMapping("/virtuleStorage")
public class VirtuleStorageController {

    /**
     * 获取持久卷列表
     *
     * @return
     */
    @RequestMapping(value = "/vs", method = RequestMethod.GET)
    public String pv() {
        return "virtuleStorage/vs";
    }

    @RequestMapping(value = "/vs/list", method = RequestMethod.GET)
    public ModelAndView getPvList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call call = api.listPersistentVolumeCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        String responseBody = response.body().string();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        System.out.println("========================");
        System.out.println(responseBody);

        modelAndView.addObject("result", responseBody);

        return modelAndView;
    }


    /**
     * 通过持久卷名、主机路径和容量，持久卷声明名、命名空间和容量，主节点ip、用户名和密码来创建持久卷并关联
     * @param requestInfo
     * @return
     * @throws ApiException
     */
    @RequestMapping(value = "/createVs", method = RequestMethod.POST)
    @ResponseBody
    public String createVs(@RequestBody RequestInfo requestInfo) throws ApiException {
//    public String createVs() throws ApiException {

        PvInfo pvInfo = requestInfo.getPvInfo();
        PvcInfo pvcInfo = requestInfo.getPvcInfo();
        VmInfo vmInfo = requestInfo.getVmInfo();

//        String persistentVolumeName = "example-pv7";
//        String persistentVolumePath = "/mnt/disks/vol2";
//        String persistentVolumeQuantity = "10Gi";
//        String persistentVolumeAccessMode = "ReadWriteOnce";
//
//        String persistentVolumeClaimName = "example-pv7-claim";
//        String persistentVolumeClaimNamespace = "default";
//        String persistentVolumeClaimQuantity = "1Gi";
//
//        String virtualMachineIp = "192.168.174.133";
//        String userName = "root";
//        String userPassword = "@wsad1234";

        String persistentVolumeName = pvInfo.getPvName();
        String persistentVolumePath = pvInfo.getPvPath();
        String persistentVolumeQuantity = pvInfo.getPvQuantity();
        String persistentVolumeAccessMode = pvInfo.getPvAccessMode();

        String persistentVolumeClaimName = pvcInfo.getPvcName();
        String persistentVolumeClaimNamespace = pvcInfo.getPvcNamespace();
        String persistentVolumeClaimQuantity = pvcInfo.getPvcQuantity();


        String virtualMachineIp = vmInfo.getVirtualMachineIp();
        String userName = vmInfo.getUserName();
        String userPassword = vmInfo.getUserPassword(); // 请替换为您的实际密码

        Session session = null;
        Channel channel = null;



        try {

            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            // 检查目标文件夹是否存在，如果不存在则创建
            try {
                sftpChannel.ls(persistentVolumePath); // 尝试列出目录
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    String[] dirs = persistentVolumePath.split("/");
                    String path = "";
                    for (String dir : dirs) {
                        if (!dir.isEmpty()) {
                            path += "/" + dir;
                            try {
                                sftpChannel.ls(path); // 尝试列出目录
                            } catch (SftpException ex) {
                                if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                                    sftpChannel.mkdir(path); // 目录不存在，创建目录
                                }
                            }
                        }
                    }
                }
            }

            InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
            // 使用 InputStream 和 InputStreamReader 读取配置文件
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
            ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();


            // 创建一个持久卷对象
            V1PersistentVolume persistentVolume = new V1PersistentVolume();
            // 创建meta和spec
            V1PersistentVolumeSpec spec = new V1PersistentVolumeSpec();
            V1ObjectMeta meta = new V1ObjectMeta();

            //设置持久卷名称
            meta.setName(persistentVolumeName);

            /**
             * 设置节点亲和
             */
            /*
            V1NodeSelector nodeSelector = new V1NodeSelector();

            // 创建节点名称选择器需求
            V1NodeSelectorRequirement nodeNameRequirement = new V1NodeSelectorRequirement();
            nodeNameRequirement.setKey("kubernetes.io/hostname"); // 设置选择器键为节点主机名
            nodeNameRequirement.setOperator("In"); // 使用 "In" 操作符表示匹配节点名
            nodeNameRequirement.setValues(Collections.singletonList("agent1")); // 设置节点名

            // 创建一个节点选择器项并将节点名称选择器需求添加到其中
            V1NodeSelectorTerm nodeSelectorTerm = new V1NodeSelectorTerm();
            nodeSelectorTerm.setMatchExpressions(Collections.singletonList(nodeNameRequirement));

            // 将节点选择器项添加到节点选择器中
            nodeSelector.addNodeSelectorTermsItem(nodeSelectorTerm);

            // 将节点选择器添加到持久卷规格中
            spec.setNodeAffinity(new V1VolumeNodeAffinity().required(nodeSelector));
             */

            // 创建HostPath的路径
            V1HostPathVolumeSource hostPath = new V1HostPathVolumeSource();
            hostPath.setPath(persistentVolumePath);


            spec.setCapacity(Collections.singletonMap("storage", new Quantity(persistentVolumeQuantity)));
            spec.setAccessModes(Collections.singletonList(persistentVolumeAccessMode));
            spec.setHostPath(hostPath);

            persistentVolume.setMetadata(meta);
            persistentVolume.setSpec(spec);

            // 创建一个持久卷声明对象
            V1PersistentVolumeClaim persistentVolumeClaim = new V1PersistentVolumeClaim();
            // 设置 metadata
            V1ObjectMeta meta1 = new V1ObjectMeta();
            meta1.setName(persistentVolumeClaimName); // 设置声明的名称
            meta1.setNamespace(persistentVolumeClaimNamespace); // 设置声明所在的命名空间
            persistentVolumeClaim.setMetadata(meta1);

            // 设置 spec
            V1PersistentVolumeClaimSpec spec1 = new V1PersistentVolumeClaimSpec();
            // spec1.setStorageClassName("my-storage-class"); // 设置存储类别
            spec1.setAccessModes(Collections.singletonList("ReadWriteOnce")); // 设置访问模式
            spec1.setResources(new V1ResourceRequirements());
            spec1.getResources().requests(Collections.singletonMap("storage", new Quantity(persistentVolumeClaimQuantity))); // 设置容量

            persistentVolumeClaim.setSpec(spec1);


            // 设置持久卷声明绑定的声明引用
            V1ObjectReference claimRef = new V1ObjectReference();
            claimRef.setName(persistentVolumeClaimName); // 设置声明的名称
            claimRef.setNamespace(persistentVolumeClaimNamespace); // 设置声明所在的命名空间
            spec.setClaimRef(claimRef);

            persistentVolume.setSpec(spec);


            // 调用 API 创建持久卷
            V1PersistentVolume createdPv = api.createPersistentVolume(persistentVolume, null, null, null);
            System.out.println("Created PV: " + createdPv.getMetadata().getName());

            // 调用 API 创建持久卷声明
            V1PersistentVolumeClaim createdPvc = api.createNamespacedPersistentVolumeClaim(persistentVolumeClaimNamespace, persistentVolumeClaim, null, null, null);
            System.out.println("Created PVC: " + createdPvc.getMetadata().getName());


            return "Persistent volume created successfully";
        } catch (Exception e) {
            return "Failed to create persistent volume: " + e.getMessage();
        }finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

    }

    /**
     * 通过持久卷名删除持久卷
     * @param pvInfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/deleteVs", method = RequestMethod.POST)
    @ResponseBody
    public String deleteVs(@RequestBody PvInfo pvInfo) throws IOException, ApiException {
//    public String deleteVs() throws IOException, ApiException {

//        String persistentVolumeName = "example-pv11";
        String persistentVolumeName = pvInfo.getPvName();

        try {
            InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
            // 使用 InputStream 和 InputStreamReader 读取配置文件
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
            ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();

            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            api.deletePersistentVolume(persistentVolumeName, null, null, null, null, null, deleteOptions);


            // 获取与要删除的PV相关联的PVC名称（假设关联的PVC名称与PV名称匹配，根据实际情况修改获取PVC名称的方式）
//            String persistentVolumeClaimName = persistentVolumeName + "-claim";
//
//            // 删除持久卷声明（PVC）
//            api.deleteNamespacedPersistentVolumeClaim(
//                    persistentVolumeClaimName,  // PVC名称
//                    "default",  // PVC所在的命名空间
//                    null, null, null, null, null, deleteOptions  // 其他参数为null
//            );

            // 获取所有的持久卷声明（PVC）
            V1PersistentVolumeClaimList pvcList = api.listPersistentVolumeClaimForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
            for (V1PersistentVolumeClaim pvc : pvcList.getItems()) {
                String claimVolumeName = pvc.getSpec().getVolumeName();
                if (claimVolumeName != null && persistentVolumeName.equals(claimVolumeName)) {
                    String persistentVolumeClaimName = pvc.getMetadata().getName();
                    // 删除持久卷声明（PVC）
                    api.deleteNamespacedPersistentVolumeClaim(
                            persistentVolumeClaimName,  // PVC名称
                            pvc.getMetadata().getNamespace(),  // PVC所在的命名空间
                            null, null, null, null, null, deleteOptions  // 其他参数为null
                    );
                    System.out.println("Persistent volume and its related claim deleted successfully");
                    return "Persistent volume and its related claim deleted successfully";
                }
            }

            System.out.println("11111112222222222222333333333333");
            return "Persistent volume deleted successfully";
        } catch (ApiException e) {
            return "Failed to delete persistent volume: " + e.getMessage();
        }

    }

    /**
     * 更新持久卷容量
     * @param pvInfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/updateVs", method = RequestMethod.POST)
    @ResponseBody
    public String updateVs(@RequestBody PvInfo pvInfo) throws IOException, ApiException {
//    public String updateVs() throws IOException, ApiException {

        // 要更新的持久卷的名称
//        String persistentVolumeName = "example-pv1";
//        String newVolumeQuantity = "6Gi";

        String persistentVolumeName = pvInfo.getPvName();
        String newVolumeQuantity = pvInfo.getPvQuantity();

        try {
            InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
            // 使用 InputStream 和 InputStreamReader 读取配置文件
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
            ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();


            // 获取当前持久卷的信息
            V1PersistentVolume persistentVolume = api.readPersistentVolume(persistentVolumeName, null);
            V1PersistentVolumeSpec spec = persistentVolume.getSpec();

//            // 设置新的容量
//            spec.getCapacity().put("storage", new Quantity(persistentVolumeQuantity));

            // 更新持久卷的相关配置，这里只是一个示例
            spec.setCapacity(Collections.singletonMap("storage", new Quantity(newVolumeQuantity)));

            // 将更新后的配置应用到持久卷
            persistentVolume.setSpec(spec);
            api.replacePersistentVolume(persistentVolumeName, persistentVolume, null, null, null);


            return "Persistent volume capacity updated successfully";
        } catch (ApiException e) {
            return "Failed to update persistent volume capacity: " + e.getResponseBody();
        } catch (IOException e) {
            return "Failed to update persistent volume capacity: " + e.getMessage();
        }

    }
}
