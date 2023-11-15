package prv.gdk.kubedash.controllers;

import okhttp3.Call;
import okhttp3.Response;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingApi;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileReader;
import java.io.IOException;

@Controller
@RequestMapping("/disload")
public class DisLoadController {

  @Value("${k8s.config}")
  private String k8sConfig;
  @RequestMapping(value = "/ingress", method = RequestMethod.GET)
  public String ingress(){
    return "disload/ingress";
  }

  @RequestMapping(value = "/ingress/list", method = RequestMethod.GET)
  public ModelAndView getIngressList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    NetworkingV1Api exV1Api = new NetworkingV1Api();

    Call call = exV1Api.listIngressForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }

  @RequestMapping(value = "/service", method = RequestMethod.GET)
  public String service(){
    return "disload/service";
  }

  @RequestMapping(value = "/service/list", method = RequestMethod.GET)
  public ModelAndView getServiceList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listServiceForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }
}
