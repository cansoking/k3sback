package prv.gdk.kubedash.controllers;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import prv.gdk.kubedash.entity.ContainerInfo;
import prv.gdk.kubedash.entity.DeploymentInfo;
import prv.gdk.kubedash.entity.PodInfo;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@Controller
@RequestMapping("/deployment")
public class DeploymentController {
    @Value("${k8s.config}")
    private String k8sConfig;

    @Value("${k8s.token}")
    private String k8sToken;

    private static final String KUBERNETES_API_SERVER = "https://192.168.64.129:6443";

    @CrossOrigin
    @RequestMapping(value = "/deleteDeployment", method = RequestMethod.GET)
    public String deleteDeploymentAndService(@RequestParam("deploymentName") String deploymentName) throws IOException, ApiException {
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();
        // 删除 Deployment
        appsApi.deleteNamespacedDeployment(deploymentName, "default", null, null, null, null, null, null);

        // 删除 Service
        V1ServiceList serviceList = api.listNamespacedService("default", null, null, null, null, null, null, null, null, null, null);
        for (V1Service service : serviceList.getItems()) {
            if (deploymentName.equals(getServiceLabelValue(service, "app"))) {
                api.deleteNamespacedService(service.getMetadata().getName(), "default", null, null, null, null, null, null);
            }
        }
        return "Deployment and Service deleted successfully.";

    }


    @CrossOrigin
    @RequestMapping(value = "/createDeployment", method = RequestMethod.POST)
    @ResponseBody
    public String createDeployment(@RequestParam("yamlFile") MultipartFile yamlFile) throws IOException, ApiException {
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
        Iterable<Object> objects = Yaml.loadAll(yamlContent);

        for (Object obj : objects) {


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
                        case "Deployment":
                            System.out.println("deployment类型qqqqq");
                            // 处理 Deployment 类型
                            appsApi.createNamespacedDeployment(namespace, (V1Deployment) obj, null, null, null);
                            break;
                        // ... 其他处理逻辑
                        // 添加其他资源类型的处理逻辑
                        default:
                            throw new IllegalArgumentException("Unknown resource type: " + kind);
                    }
                }
            } else if (obj instanceof V1Deployment) {
                System.out.println("deployment类型");
                // 处理 Deployment 类型
                //V1Deployment testdeployment = new V1Deployment();
                V1Deployment deployment = (V1Deployment) obj;
                V1ObjectMeta metadata = deployment.getMetadata();
                //输出deployment的metadata
                System.out.println("---"+metadata);
                System.out.println("---"+deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
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

            }else if (obj instanceof V1Service) {
                // 处理 Service
                System.out.println("V1Service");
                V1Service service = (V1Service) obj;
                V1ObjectMeta metadata = service.getMetadata();
                if (metadata != null) {
                    String kind = service.getKind();
                    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                    switch (kind) {
                        case "Service":
                            api.createNamespacedService(namespace, service, null, null, null);
                            break;
                        // 处理其他资源类型的逻辑
                    }
                }
            } else if (obj != null) {
                return "null";
            }
        }

        return "Deployment created successfully.";
    }
    @CrossOrigin
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getDeploymentList() throws IOException, ApiException {


        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api api = new AppsV1Api();

        Call call = api.listDeploymentForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();
        System.out.print(response);
        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }

    private String getServiceLabelValue(V1Service service, String labelKey) {
        if (service.getMetadata() != null && service.getMetadata().getLabels() != null) {
            return service.getMetadata().getLabels().get(labelKey);
        }
        return null;
    }

    @RequestMapping(value ="/deployByParam", method = RequestMethod.POST)
    @ResponseBody
    public String deploy(@RequestBody DeploymentInfo request) throws IOException, ApiException {

        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        /*//初始化DeploymentInfo
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setDeploymentName("your-java-web-app5");
        deploymentInfo.setImage("zytest:1.1");
        deploymentInfo.setContainerPort(8080);
        deploymentInfo.setServicePort(80);
        deploymentInfo.setNodePort(30005);*/

        // 创建 Deployment 和 Service
        createDeploymentByParam(request);
        createServiceByParam(request);

        return "Deployment and Service created successfully.";
    }

    private void createDeploymentByParam(DeploymentInfo request) throws ApiException {
        AppsV1Api appsApi = new AppsV1Api();

        String deploymentName = request.getDeploymentName();

        // 构建 Deployment 对象
        V1Deployment deployment = new V1Deployment()
                .apiVersion("apps/v1")
                .kind("Deployment")
                .metadata(new V1ObjectMeta().name(deploymentName).labels(Collections.singletonMap("app", deploymentName)))
                .spec(new V1DeploymentSpec()
                        .replicas(1)
                        .selector(new V1LabelSelector().matchLabels(Collections.singletonMap("app", deploymentName)))
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(Collections.singletonMap("app", deploymentName)))
                                .spec(new V1PodSpec()
                                        .containers(Collections.singletonList(new V1Container()
                                                .name(deploymentName)
                                                .image(request.getImage())
                                                .ports(Collections.singletonList(new V1ContainerPort().containerPort(request.getContainerPort()))))))));


        // 创建 Deployment
        appsApi.createNamespacedDeployment("default", deployment, null, null, null);
    }

    private void createServiceByParam(DeploymentInfo request) throws ApiException {
        CoreV1Api coreApi = new CoreV1Api();

        String serviceName = request.getDeploymentName() + "-service";

        // 构建 Service 对象
        V1Service service = new V1Service()
                .apiVersion("v1")
                .kind("Service")
                .metadata(new V1ObjectMeta().name(serviceName).labels(Collections.singletonMap("app", request.getDeploymentName())))
                .spec(new V1ServiceSpec()
                        .selector(Collections.singletonMap("app", request.getDeploymentName()))
                        .type("NodePort")
                        .ports(Collections.singletonList(new V1ServicePort().port(request.getServicePort()).targetPort(new IntOrString(request.getContainerPort())).nodePort(request.getNodePort()))));
        // 创建 Service
        coreApi.createNamespacedService("default", service, null, null, null);
    }

    @RequestMapping(value = "/stopDeploymentName", method = RequestMethod.GET)
    public String stopDeployment(@RequestParam("deploymentName") String deploymentName) throws IOException, ApiException {
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api appsApi = new AppsV1Api();
        //修改k3s中Deployment的replicas为0
        //V1Patch patch = new V1Patch("{\"spec\":{\"replicas\":0}}");
        V1Patch patch = new V1Patch("[{ \"op\": \"replace\", \"path\": \"/spec/replicas\", \"value\": 0 }]");

        try {
            appsApi.patchNamespacedDeployment(deploymentName, "default", patch, null, null, null, null);
        } catch (ApiException e) {
            System.out.println("Exception caught!");
            System.out.println("Status code: " + e.getCode());
            System.out.println("Response body: " + e.getResponseBody());
            e.printStackTrace();
        }




        return "Deployment stopped successfully.";
    }

    @RequestMapping(value = "/startDeploymentName", method = RequestMethod.GET)
    public String startDeploymentName(@RequestParam("deploymentName") String deploymentName) throws IOException, ApiException {
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api appsApi = new AppsV1Api();
        //修改k3s中Deployment的replicas为0
        //V1Patch patch = new V1Patch("{\"spec\":{\"replicas\":0}}");
        V1Patch patch = new V1Patch("[{ \"op\": \"replace\", \"path\": \"/spec/replicas\", \"value\": 1 }]");

        try {
            appsApi.patchNamespacedDeployment(deploymentName, "default", patch, null, null, null, null);
        } catch (ApiException e) {
            System.out.println("Exception caught!");
            System.out.println("Status code: " + e.getCode());
            System.out.println("Response body: " + e.getResponseBody());
            e.printStackTrace();
        }




        return "Deployment start successfully.";
    }

    


   


}
