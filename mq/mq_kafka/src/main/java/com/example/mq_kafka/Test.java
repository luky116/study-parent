package com.example.mq_kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {

    static class Node {
        Object value;
        List<Node> children;
        // 层数
        int level;

        public Node setLevel(int level) {
            this.level = level;
            return this;
        }

        public Node(Object value) {
            this.value = value;
            this.children = new ArrayList<>();
        }

        public Node next(Node... nodes) {
            this.children.addAll(Arrays.asList(nodes));
            return this;
        }

        public String toString() {
            return value.toString() + "（" + level + "层）";
        }
    }

    private static List<Node> result = new ArrayList<>();
    private static List<String> strings = new ArrayList<>();

    // 深度遍历
    public static void getNodes(Node node, int level, String prefix) {
        if (node == null) {
            return;
        }
        if ("==-".equals(prefix)) {
            prefix = "";
        }
        result.add(node.setLevel(level));
        if (node.children != null && !node.children.isEmpty()) {
            for (Node child : node.children) {
                // 遍历
                getNodes(child, level + 1, prefix + node.value + "-");
            }
        } else {
            strings.add(prefix + node.value);
        }
    }

    // 测试
    public static void main(String[] args) {
        Node head = buildNode();
        getNodes(head, 0, "");
        System.out.println(result);
        System.out.println(strings);
    }

    private static Node buildNode() {
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        Node node5 = new Node(5);
        Node node6 = new Node(6);
        Node node7 = new Node(7);
        Node node8 = new Node(8);
        Node node9 = new Node(9);
        Node node10 = new Node(10);
        Node node11 = new Node(11);
        Node node12 = new Node(12);
        Node node13 = new Node(13);
        Node node14 = new Node(14);
        Node node15 = new Node(15);
        Node node16 = new Node(16);
        Node node17 = new Node(17);

        node3.next(node4, node5, node6);
        node2.next(node3, node7);
        node1.next(node2);

        node10.next(node11, node12);
        node9.next(node10);
        node8.next(node9);

        node13.next(node14, node15, node16);

        return new Node("==").next(node1, node8, node13, node17);
    }

}
