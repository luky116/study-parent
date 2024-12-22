package pers.vv.study.etcd;

public class Main {

    public static void main(String[] args) {
        EtcdService etcdService = new EtcdService(Config.ETCD_URL);

        // 示例操作
        etcdService.write("/example/key", "hello-world");
        etcdService.read("/example/key");
        etcdService.delete("/example/key");

        // 关闭客户端
        etcdService.close();
    }

}
