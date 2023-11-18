package com.xw.cloud.controller;
import io.kubernetes.client.openapi.models.*;
import okhttp3.Call;
import okhttp3.Response;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.apis.StorageV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/cluster")
public class ClusterController {

  @Value("${k8s.config}")
  private String k8sConfig;

  @RequestMapping(value = "/getNamespaceList", method = RequestMethod.GET)
  public Model Test(Model model) throws IOException, ApiException{
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();
    V1NamespaceList namespaceList = api.listNamespace(null,null, null, null, null, null, null, null, null, null);

//    System.out.println("1234345");
//    model.addAttribute("podList", podList.getItems());
    model.addAttribute("namespaceList", namespaceList.getItems());

//    return "workload/getPodList";
    return model;
  }

  @RequestMapping(value = "/namespace", method = RequestMethod.GET)
  public String namespace(){
    return "cluster/namespace";
  }

  @RequestMapping(value = "/namespace/list", method = RequestMethod.GET)
  public ModelAndView getNamespaceList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listNamespaceCall(null,null, null, null, null, null, null, null, 5, null,null);

    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }



  @RequestMapping(value = "/node", method = RequestMethod.GET)
  public String node(){
    return "cluster/node";
  }

  @RequestMapping(value = "/node/list", method = RequestMethod.GET)
  public ModelAndView getNodeList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listNodeCall(null,null, null, null, null, null, null, null, 5, null,null);


    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }

  @RequestMapping(value = "/pv", method = RequestMethod.GET)
  public String pv(){
    return "cluster/pv";
  }

  @RequestMapping(value = "/pv/list", method = RequestMethod.GET)
  public ModelAndView getPvList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listPersistentVolumeCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }

  @RequestMapping(value = "/role", method = RequestMethod.GET)
  public String role(){
    return "cluster/role";
  }

  @RequestMapping(value = "/role/list", method = RequestMethod.GET)
  public ModelAndView getRoleList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);


    RbacAuthorizationV1Api api = new RbacAuthorizationV1Api();

    Call call = api.listRoleForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);
    Call callForCluster = api.listClusterRoleCall(null,null, null, null, null, null, null, null, 5, null,null);

    Response response = call.execute();
    Response responseForCluster = callForCluster.execute();

    if (!(response.isSuccessful() && responseForCluster.isSuccessful())) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());
    modelAndView.addObject("result2",responseForCluster.body().string());

    return modelAndView;
  }

  @RequestMapping(value = "/storageclass", method = RequestMethod.GET)
  public String storageclass(){
    return "cluster/storageclass";
  }

  @RequestMapping(value = "/storageclass/list", method = RequestMethod.GET)
  public ModelAndView getStorageclassList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);


    StorageV1Api api = new StorageV1Api();

    Call call = api.listStorageClassCall(null,null, null, null, null, null, null, null, 5, null,null);

    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }


}
