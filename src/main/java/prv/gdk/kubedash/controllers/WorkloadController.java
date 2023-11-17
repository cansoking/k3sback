package prv.gdk.kubedash.controllers;

import io.kubernetes.client.openapi.models.V1DeleteOptions;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import okhttp3.*;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.BatchV1beta1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;

import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import java.io.InputStream;

import okhttp3.Response;
import prv.gdk.kubedash.entity.ContainerInfo;
import prv.gdk.kubedash.entity.PodInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


@CrossOrigin
@Controller
@RequestMapping("workload")
public class WorkloadController {
    @Value("${k8s.config}")
    private String k8sConfig;

    @Value("${k8s.token}")
    private String k8sToken;

    private static final String KUBERNETES_API_SERVER = "https://192.168.174.133:6443";

    

    

    /**
     * 获取pod列表
     *
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/getPodList", method = RequestMethod.GET)
    public ModelAndView getPod() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");

        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();


        V1PodList podList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);

        for (V1Pod pod : podList.getItems()) {
            if (pod.getMetadata().getAnnotations() == null || !pod.getMetadata().getAnnotations().containsKey("status")) {
                pod.getMetadata().setAnnotations(new HashMap<>());
                pod.getMetadata().getAnnotations().put("status", "Yes");
            }
            api.replaceNamespacedPod(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), pod, null, null, null);
        }

        // 发起第二次请求并等待请求完成
        Call call = api.listPodForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);
        Response response = call.execute();

        // 处理第二次请求的响应
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            // 处理响应体，并将其添加到 ModelAndView 中
            modelAndView.addObject("result", responseBody);
        } else {
            // 处理请求失败情况
            modelAndView.addObject("result", "Error: " + response.message());
        }


//        modelAndView.addObject("podList", podList.getItems());


        return modelAndView;
    }

//  @RequestMapping(value = "/getPodList", method = RequestMethod.GET)
//  public String Test(Model model) throws IOException, ApiException{
//    // 通过流读取，方式1
//    InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
//    // 使用 InputStream 和 InputStreamReader 读取配置文件
//    KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
//    Configuration.setDefaultApiClient(client);
//    CoreV1Api api = new CoreV1Api();
//
//
//    V1PodList podList = api.listPodForAllNamespaces(null,null, null, null, null, null, null, null, null,null );
//
//    for (V1Pod pod : podList.getItems()) {
//      if (pod.getMetadata().getAnnotations() == null || !pod.getMetadata().getAnnotations().containsKey("status")) {
//        pod.getMetadata().setAnnotations(new HashMap<>());
//        pod.getMetadata().getAnnotations().put("status", "Yes");
//      }
//
//    }
//
//    model.addAttribute("podList", podList.getItems());
//    return "workload/getPodList";
//  }


    /**
     * 迁移pod
     *
     * @param podName
     * @param podNamespace
     * @param model
     * @return
     */
    @RequestMapping(value = "/editPod", method = RequestMethod.GET)
    public String editPod(@RequestParam("podName") String podName,
                          @RequestParam("podNamespace") String podNamespace,
                          Model model) {

        // 将接收到的值添加到 model 中
        model.addAttribute("podName", podName);
        model.addAttribute("podNamespace", podNamespace);
        return "workload/editPod";
    }

    /**
     * 迁移镜像（前端给新的节点名）
     *
     * @param podinfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/editPod", method = RequestMethod.POST)
    @ResponseBody
//    public String editPod(@RequestParam("podName") String podName,
//                          @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    public String editPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {
        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        String newNodeName = podinfo.getPodNodeName();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        try {
            // 获取Pod对象并确定其当前节点名称
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);


            // 复制 Pod 对象
            V1Pod newPod = new V1Pod();
            newPod.setMetadata(new V1ObjectMeta());
            newPod.setSpec(new V1PodSpec());
            newPod.setStatus(new V1PodStatus());

            System.out.println(pod.getMetadata().getName());

            newPod.getMetadata().setName(pod.getMetadata().getName());
            newPod.getMetadata().setNamespace(pod.getMetadata().getNamespace());
            newPod.getMetadata().setLabels(pod.getMetadata().getLabels());
            newPod.getMetadata().setAnnotations(pod.getMetadata().getAnnotations());
            newPod.getSpec().setContainers(pod.getSpec().getContainers());
            newPod.getSpec().setVolumes(pod.getSpec().getVolumes());
            newPod.getSpec().setNodeName(newNodeName);
            newPod.setStatus(pod.getStatus());

            // 删除当前 Pod
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setPropagationPolicy("Foreground");
            api.deleteNamespacedPod(podName, podNamespace, null, null, null, null, null, deleteOptions);

            // 在新节点上创建 Pod

            Thread.sleep(5000);

//      System.out.println(newPod.getSpec().getNodeName());
//      System.out.println(newPod);
            api.createNamespacedPod(podNamespace, newPod, null, null, null);

            return "Successfully moved Pod ";
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                // 发生冲突，返回失败响应给前端
                return "Error: Pod creation failed due to conflict";
//        return "错误: 重复创建！";
            } else {
                // 其他错误，返回失败响应给前端
                return "Error: Failed to create Pod";
//        return "错误: 创建失败！";
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 配置镜像（单独配置容器）
     *
     * @param podinfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/configureImage", method = RequestMethod.POST)
    @ResponseBody
//    public String configureImage(@RequestParam("podName") String podName,
//                                 @RequestParam("podNamespace") String podNamespace,
//                                 @RequestParam("containerName") String containerName,
//                                 @RequestParam("imageName") String imageName) throws IOException, ApiException {
    public String configureImage(@RequestBody PodInfo podinfo) throws IOException, ApiException {
        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        List<ContainerInfo> containerInfoList = podinfo.getContainerInfoList();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        try {
            // 获取Pod对象并确定其当前节点名称
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);


            // 新建 Pod 对象
            V1Pod newPod = new V1Pod();
            newPod.setMetadata(new V1ObjectMeta());
            newPod.setSpec(new V1PodSpec());
            newPod.setStatus(new V1PodStatus());

            // 复制 Pod 对象并添加container以配置image
            newPod.getMetadata().setName(pod.getMetadata().getName());
            newPod.getMetadata().setNamespace(pod.getMetadata().getNamespace());
            newPod.getMetadata().setLabels(pod.getMetadata().getLabels());
            newPod.getMetadata().setAnnotations(pod.getMetadata().getAnnotations());
            newPod.getSpec().setContainers(pod.getSpec().getContainers());
            newPod.getSpec().setVolumes(pod.getSpec().getVolumes());
            newPod.setStatus(pod.getStatus());


            for (ContainerInfo containerInfo : containerInfoList) {
                String containerName = containerInfo.getContainerName();
                String containerImage = containerInfo.getContainerImage();
                int port = containerInfo.getPort();

                // 处理每个 containerInfo 对象
                V1Container newContainer = new V1Container()
                        .name(containerName)
                        .image(containerImage)
                        .ports(Collections.singletonList(
                                new V1ContainerPort()
                                        .containerPort(port)));
                newPod.getSpec().getContainers().add(newContainer);
            }



            System.out.println(pod.getMetadata().getName());



            // 删除当前 Pod
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setPropagationPolicy("Foreground");
            api.deleteNamespacedPod(podName, podNamespace, null, null, null, null, null, deleteOptions);

            // 在新节点上创建 Pod

            Thread.sleep(5000);

//      System.out.println(newPod);
            api.createNamespacedPod(podNamespace, newPod, null, null, null);

            return "Successfully moved Pod ";
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                // 发生冲突，返回失败响应给前端
                return "Error: Pod creation failed due to conflict";
//        return "错误: 重复创建！";
            } else {
                // 其他错误，返回失败响应给前端
                return "Error: Failed to create Pod";
//        return "错误: 创建失败！";
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建pod
     *
     * @return
     */
    @RequestMapping(value = "/createPod", method = RequestMethod.GET)
    public String createPod() {
        return "workload/createPod";
    }

    @RequestMapping(value = "/createPod", method = RequestMethod.POST)
    @ResponseBody

//    public String createPod(@RequestParam("podName") String podName,
//                            @RequestParam("podNamespace") String podNamespace,
//                            @RequestParam("containerName") String containerName,
//                            @RequestParam("containerImage") String containerImage

    public String createPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        String podNodeName = podinfo.getPodNodeName();
        List<ContainerInfo> containerInfoList = podinfo.getContainerInfoList();
//        nodeName = "server1";


        System.out.println(podNamespace);

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        try {



            // 添加容器到Pod的规格中
            List<V1Container> containers = new ArrayList<>();

            for (ContainerInfo containerInfo : containerInfoList) {
                String containerName = containerInfo.getContainerName();
                String containerImage = containerInfo.getContainerImage();
                int port = containerInfo.getPort();

                port = 80;
                // 处理每个 containerInfo 对象...
                V1Container container = new V1Container()
                        .name(containerName)
                        .image(containerImage)
                        .ports(Collections.singletonList(
                                new V1ContainerPort()
                                        .containerPort(port)));
                containers.add(container);
            }



//            V1Container container1 = new V1Container()
//                    .name("test")
//                    .image("rancher/klipper-lb:v0.4.4")
//                    .ports(Collections.singletonList(
//                            new V1ContainerPort()
//                                    .containerPort(port)));

            // 将容器添加到容器列表中

//            containers.add(container1);

            V1PodSpec podSpec = new V1PodSpec()
                    .nodeName(podNodeName)
                    .containers(containers);

            //添加单个container
//      V1PodSpec podSpec = new V1PodSpec()
//              .containers(Collections.singletonList(container));

            V1ObjectMeta podMetadata = new V1ObjectMeta()
                    .namespace(podNamespace)
                    .name(podName);

            V1Pod pod = new V1Pod()
                    .metadata(podMetadata)
                    .spec(podSpec);


            System.out.println("创建111111111111111");
            V1Pod createdPod = api.createNamespacedPod(podNamespace, pod, null, null, null);
            return "Pod created successfully.";

        } catch (ApiException e) {
            if (e.getCode() == 409) {
                // 发生冲突，返回失败响应给前端
                return "Error: Pod creation failed due to conflict";
//        return "错误: 重复创建！";
            } else {
                // 其他错误，返回失败响应给前端
                return e.getResponseBody();
//        return "错误: 创建失败！";
            }
        }


        //yaml文件创建
    /*String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
    Object obj = Yaml.load(yamlContent);



    if (obj instanceof V1Pod) {
      System.out.println("V1Pod");
      V1Pod pod = (V1Pod) obj;
      V1ObjectMeta metadata = pod.getMetadata();
      if (metadata != null) {
        String kind = pod.getKind();
        String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
        switch (kind) {
          case "Pod":
            api.createNamespacedPod(namespace, pod, null, null, null);

            break;

          default:
            throw new IllegalArgumentException("Unknown resource type: " + kind);
        }
      }
    } else if (obj instanceof V1Deployment) {
      System.out.println("deployment类型");
    // 处理 Deployment 类型
      V1Deployment deployment = (V1Deployment) obj;
      V1ObjectMeta metadata = deployment.getMetadata();
      if (metadata != null) {
        String kind = deployment.getKind();
        String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
        switch (kind) {
          case "Pod":
//            api.createNamespacedPod(namespace, pod, null, null, null);
            break;
          case "Deployment":
            appsApi.createNamespacedDeployment(namespace, deployment, null, null, null);
            break;
          // 处理其他资源类型的逻辑
          default:
            throw new IllegalArgumentException("Unknown resource type: " + kind);
        }
      }

  }else if (obj != null ){
      return "null";
    }
*/
//    return "Pod created successfully.";


//    return "Pod created successfully: " + createdPod.getMetadata().getName();
//    System.out.println("11111");
//    return "success";
    }

    @RequestMapping(value = "/pod", method = RequestMethod.GET)
    public String pod(Model model) throws IOException, ApiException {

        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList podList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);

        System.out.println("1234345");
//    model.addAttribute("podList", podList.getItems());
        model.addAttribute("podList", podList.getItems());


//    convertIntOrStringToString(podList);
//     使用 Jackson ObjectMapper 将 List 转换为 JSON 字符串
//    ObjectMapper objectMapper = new ObjectMapper();
//    String jsonString = objectMapper.writeValueAsString(podList);
//
//    modelAndView.addObject("result",jsonString);
        return "workload/pod";
    }

    /**
     * 删除pod
     *
     * @param podinfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/deletePod", method = RequestMethod.POST)
    @ResponseBody
//    public String deletePod(@RequestParam("podName") String podName, @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    public String deletePod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        System.out.println("11111");


        long startTime = System.currentTimeMillis();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        deleteOptions.setPropagationPolicy("Foreground");
        try {
            api.deleteNamespacedPod(podName, podNamespace, null, null, null, null, null, deleteOptions);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("删除 Pod 操作的执行时间为：" + executionTime + " 毫秒");

            return "Pod deleted successfully: " + podName;
        } catch (ApiException e) {
            return "Failed to delete the Pod: " + podName + ", Error: " + e.getMessage();
        }
//    return "success";
    }

    /**
     * 停止pod
     *
     * @param podinfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/stopPod", method = RequestMethod.POST)
    @ResponseBody
//    public String stopPod(@RequestParam("podName") String podName, @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    public String stopPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();

        long startTime = System.currentTimeMillis();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        try {
//      V1PodStatus newStatus = new V1PodStatus();
//      // 设置新的状态属性
//      newStatus.setPhase("Pending");
//      newStatus.setMessage("Pod is stopping successfully");


            // 获取Pod的当前状态
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);


//      System.out.println("------------------------------------------");
//      System.out.println(pod.getMetadata().getName());
//      String podLogs = api.readNamespacedPodLog(podName, podNamespace, null, null,null, null, null, null, null, null, null);
//      System.out.println(podLogs);


//      pod.setStatus(newStatus);

            // 检查Annotations是否为null
            if (pod.getMetadata().getAnnotations() == null) {
                pod.getMetadata().setAnnotations(new HashMap<>());
            }
            System.out.println("到这里");
            // 修改Pod的状态为Stopped
            pod.getMetadata().getAnnotations().put("status", "No");
            api.replaceNamespacedPod(podName, podNamespace, pod, null, null, null);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("停止 Pod 操作的执行时间为：" + executionTime + " 毫秒");

            return "Pod stopped successfully: " + podName;
        } catch (ApiException e) {
            return "Failed to stop the Pod: " + podName + ", Error: " + e.getMessage();
        }
    }

    /**
     * 启动pod
     *
     * @param podinfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/startPod", method = RequestMethod.POST)
    @ResponseBody
//    public String startPod(@RequestParam("podName") String podName, @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    public String startPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        System.out.println("333333");

        long startTime = System.currentTimeMillis();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        try {

            V1PodStatus newStatus = new V1PodStatus();
// 设置新的状态属性
            newStatus.setPhase("Running");
            newStatus.setMessage("Pod is running successfully");

            // 获取Pod的当前状态
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);
            pod.setStatus(newStatus);

            if (pod.getMetadata().getAnnotations() == null) {
                pod.getMetadata().setAnnotations(new HashMap<>());
            }


            // 修改Pod的状态为Running
            pod.getMetadata().getAnnotations().put("status", "Yes");
            api.replaceNamespacedPod(podName, podNamespace, pod, null, null, null);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("启动 Pod 操作的执行时间为：" + executionTime + " 毫秒");

            return "Pod started successfully: " + podName;
        } catch (ApiException e) {
            return "Failed to start the Pod: " + podName + ", Error: " + e.getMessage();
        }
    }


   

    private class V1HTTPGetAction {
    }
}
