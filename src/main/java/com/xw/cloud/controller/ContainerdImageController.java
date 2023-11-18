package com.xw.cloud.controller;

import com.jcraft.jsch.*;
import com.xw.cloud.bean.ImageInfo;
import com.xw.cloud.bean.RequestInfo;
import com.xw.cloud.bean.VmInfo;
import groovy.util.logging.Slf4j;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Controller
@CrossOrigin
@RequestMapping("/containerd")
public class ContainerdImageController {


    @RequestMapping(value = "/images", method = RequestMethod.GET)
    public String container() {
        return "containerd/images";
    }

    /**
     * 通过节点ip、用户名和密码查询镜像
     * @param vmInfo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/images/list", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView getContainerdImages(@RequestBody VmInfo vmInfo) throws IOException {
//    public ModelAndView getContainerdImages() throws IOException {

        ModelAndView modelAndView = new ModelAndView("jsonView");


        /**
         * 通过Kubernetes客户端获取镜像
         */

       /* InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();*/

/*
        // 指定节点名称
        String nodeName = "your-node-name";

        // 获取指定节点
        V1Node node = api.readNode(nodeName, null, null, null);

        // 获取节点的状态
        V1NodeStatus status = node.getStatus();

        // 获取节点的镜像列表
        List<V1ContainerImage> images = status.getImages();

        // 遍历每个镜像
        for (V1ContainerImage image : images) {
            // 输出镜像名称
            System.out.println(image.getNames());
        }

 */

        // 获取所有节点列表
        /*V1NodeList nodeList = null;
        try {
            nodeList = api.listNode(null,null, null, null, null, null, null, null, null, null);
            List<V1Node> nodes = nodeList.getItems();

            // 遍历每个节点
            for (V1Node node : nodes) {
                // 获取节点的状态
                V1NodeStatus status = node.getStatus();

                // 获取节点的镜像列表
                List<V1ContainerImage> images = status.getImages();


                // 遍历每个镜像
                for (V1ContainerImage image : images) {
                    System.out.println(image.getNames());
//                    System.out.println(image.getSizeBytes());

                    String imageName = image.getNames().get(0);
                    String imageName1 = image.getNames().get(1);
                    // 解析镜像摘要
                    String[] parts = imageName.split("@");
                    String digest = parts[1];

                    // 拆分镜像名称和标签
                    String[] parts1 = imageName1.split(":");
                    String name = parts1[0];
                    String tag = parts1[1];

                    //将信息放到imageList
                    List<Object> imageList = new ArrayList<>();

                    imageList.add(name);
                    imageList.add(digest);
                    imageList.add(tag);
                    imageList.add(image.getSizeBytes());

//                    modelAndView.addObject("imageList", imageList);

                    // 输出镜像名称
                    System.out.println("镜像名称: " + name);
                    // 输出镜像摘要
                    System.out.println("镜像摘要: " + digest);
                    // 输出镜像标签
                    System.out.println("镜像标签: " + tag);
                    // 输出镜像大小
                    System.out.println("镜像大小: " + image.getSizeBytes());

                }
            }

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }/*


        /**
         * 通过JSch命令获取镜像列表
         */

//        String virtualMachineIp = "192.168.174.133";
//        String userName = "root";
//        String userPassword = "@wsad1234"; // 请替换为您的实际密码

        String virtualMachineIp = vmInfo.getVirtualMachineIp();
        String userName = vmInfo.getUserName();
        String userPassword = vmInfo.getUserPassword(); // 请替换为您的实际密码

        Session session = null;

        StringBuilder result = new StringBuilder();
        try {

            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("crictl images"); // 设置执行的命令
            InputStream in = execChannel.getInputStream();  // 获取命令执行结果的输入流

            execChannel.connect();  // 连接远程执行命令

            byte[] tmp = new byte[1024];
            StringBuilder commandOutput = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    commandOutput.append(new String(tmp, 0, i));
                }
                if (execChannel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("exit-status: " + execChannel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    // 处理异常
                }
            }
            System.out.println("----------------");
            System.out.println(commandOutput);  // 输出命令执行结果
            System.out.println("----------------");

            modelAndView.addObject("result", commandOutput);

            /**
             * 构造`crictl`命令获取镜像列表
             */
            // 构造`crictl`命令
            /*String crictlCommand = "ssh root@192.168.174.133 crictl images";



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
            }*/


        } catch (IOException | JSchException e) {
            e.printStackTrace();
        }


        return modelAndView;

    }


    @RequestMapping(value = "/uploadImage", method = RequestMethod.GET)
    public String createPod() {
        return "containerd/uploadImage";
    }

    /**
     * 通过节点ip、用户名和密码，镜像文件实现上传并创建镜像
     * @param tarFile
     * @param virtualMachineIp
     * @param userName
     * @param userPassword
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    @ResponseBody
    public String uploadImage(@RequestParam("tarFile") MultipartFile tarFile,
                              @RequestParam("virtualMachineIp") String virtualMachineIp,
                              @RequestParam("userName") String userName,
                              @RequestParam("userPassword") String userPassword) throws IOException {


//        String virtualMachineIp = "192.168.174.133";
//        String userName = "root";
//        String userPassword = "@wsad1234";

//        String virtualMachineIp = vmInfo.getVirtualMachineIp();
//        String userName = vmInfo.getUserName();
//        String userPassword = vmInfo.getUserPassword();

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
                sftpChannel.ls("/opt/tar"); // 尝试列出目录
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    sftpChannel.mkdir("/opt/tar"); // 目录不存在，创建目录
                }
            }

            // 传输文件
            sftpChannel.put(tarFile.getInputStream(), "/opt/tar/" + tarFile.getOriginalFilename());

            System.out.println(tarFile.getOriginalFilename());

            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("ctr -n=k8s.io  image import  /opt/tar/" + tarFile.getOriginalFilename()); // 设置执行的命令
            execChannel.setInputStream(null);
            ((ChannelExec) execChannel).setErrStream(System.err);
            InputStream in = execChannel.getInputStream();
            execChannel.connect();

            return "Image uploaded successfully";
        } catch (JSchException | SftpException e) {
            throw new RuntimeException("Failed to upload image", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        /*
        try {
            String uploadCommand = "scp " + tarFile +"root@192.168.174.133:/tar/opt/";
            String ctrCommand = "ssh root@192.168.174.133 ctr image import "+ tarFile.getOriginalFilename();

                System.out.println(tarFile.getOriginalFilename());

            String crictlCommand = "ssh root@192.168.174.133 crictl images";
            // 构造导入镜像的命令
            String importCommand = "ctr --address=ssh://" + username + ":" + password + "@" + virtualMachineIp +
                    " image import " + tarFile.getOriginalFilename();

            // 执行导入镜像的命令
            Process process = Runtime.getRuntime().exec(crictlCommand);

            // 等待命令执行完成
            int exitCode = process.waitFor();
        if (exitCode == 0) {
            return "Image imported successfully";
        } else {
            return "Failed to import image";
        }
        } catch (InterruptedException e) {
            // 处理中断异常
            Thread.currentThread().interrupt();
            throw new RuntimeException("Image import interrupted", e);
        }*/
    }

    /**
     * 通过节点ip、用户名和密码，镜像ID删除镜像
     *
     * @param requestInfo
     * @return
     * @throws IOException
     * @throws ApiException
     */
    @RequestMapping(value = "/deleteImage", method = RequestMethod.POST)
    @ResponseBody
    public String deletePod(@RequestBody RequestInfo requestInfo) throws IOException, ApiException {

        VmInfo vmInfo = requestInfo.getVmInfo();
        ImageInfo imageInfo = requestInfo.getImageInfo();

        System.out.println("11111");

//        String virtualMachineIp = "192.168.174.133";
//        String username = "root";
//        String password = "@wsad1234"; // 请替换为您的实际密码
//        String imageId = "80d28bedfe5de";

        String virtualMachineIp = vmInfo.getVirtualMachineIp();
        String userName = vmInfo.getUserName();
        String userPassword = vmInfo.getUserPassword(); // 请替换为您的实际密码
        String imageId = imageInfo.getImageId();

        Session session = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("crictl rmi " + imageId); // 设置执行的命令
            execChannel.setInputStream(null);
            ((ChannelExec) execChannel).setErrStream(System.err);
            InputStream in = execChannel.getInputStream();
            execChannel.connect();

            System.out.println("==================删除成功到这");
            return "Image deleted successfully";

        } catch (JSchException e) {
            throw new RuntimeException("Failed to upload image", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }


    }
}
