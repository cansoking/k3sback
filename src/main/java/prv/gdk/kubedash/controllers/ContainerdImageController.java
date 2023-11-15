package prv.gdk.kubedash.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import prv.gdk.kubedash.ContainerdImagesResult;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Controller
@RequestMapping("/containerd")
public class ContainerdImageController {


    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public String container(){
        return "/containerd/image";
    }

    @RequestMapping(value = "/image/list", method = RequestMethod.GET)
    public String getContainerdImages() {
        StringBuilder result = new StringBuilder();
        try {
            // 构造`crictl`命令
            String crictlCommand = "ssh root@192.168.174.133 crictl images";

            // 执行命令并获取输出
            Process process = Runtime.getRuntime().exec(crictlCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 读取命令输出并拼接结果
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                result.append("命令执行失败");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(result.toString());
        return result.toString();
    }


    /*@RequestMapping(value = "/container/list", method = RequestMethod.GET)
    public ContainerdImagesResult getContainerdImages() {
        ContainerdImagesResult containerdImagesResult = new ContainerdImagesResult();
        StringBuilder result = new StringBuilder();
        try {
            // 构造`crictl`命令
            String crictlCommand = "ssh root@192.168.174.133 crictl images";

            // 执行命令并获取输出
            Process process = Runtime.getRuntime().exec(crictlCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 读取命令输出并拼接结果
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                result.append("命令执行失败");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(result.toString());

        containerdImagesResult.setResult(result.toString());
        return containerdImagesResult;
    }*/

}
