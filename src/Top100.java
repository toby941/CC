/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */


import java.util.Arrays;
import java.util.Random;

/**
 * Top100.java 10000+个数字钟找出top100
 * 
 * @author baojun
 */
public class Top100 {
    private static Node head = null;
    private static Node end = null;
    private static Node tempNode = null;
    private static Node node = null;

    public static int[] getTop100(int[] inputArray) {

        int result[] = new int[100];
        int k = 100;
        if (inputArray.length < 100) {
            k = inputArray.length;
        }
        for (int i = 0; i < 100; ++i) {
            result[i] = inputArray[i];
        }

        Arrays.sort(result);

        for (int i = k - 1; i >= 0; i--) {
            node = new Node(result[i], tempNode);
            if (i == k - 1) {
                head = node;
            } else {
                tempNode.right = node;
            }
            if (i == 0) {
                end = node;
            } else {
                tempNode = node;
            }
        }
        tempNode = end;

        for (int i = 100; i < inputArray.length; i++) {
            int tempValue = inputArray[i];
            if (tempValue <= end.value) {
                continue;
            } else {
                tempNode = end;
                setValue(inputArray[i]);
            }
        }

        for (int i = 0; i < 100; i++) {
            if (i == 0) {
                node = head;
            } else {
                node = node.right;
            }
            result[i] = node.value;
        }

        return result;

    }

    private static void setValue(int tempValue) {
        if (tempNode.value < tempValue) {
            tempNode = tempNode.left;
            // 最大的
            if (tempNode == null) {
                node = new Node(head, tempValue);
                head.left = node;
                head = node;
                removeEnd();
            } else {
                setValue(tempValue);
            }
        } else if (tempNode.value != tempValue) {
            node = new Node(tempValue, tempNode);
            // 要替代end
            if (tempNode.right == end) {
                end.left.right = node;
                end = node;
            } else {
                try {
                    tempNode.right.left = node;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    System.err.println(tempNode.right);
                    e.printStackTrace();
                    System.exit(0);
                }
                tempNode.right = node;
                removeEnd();
            }
        }
    }

    private static void removeEnd() {
        end = end.left;
        end.right = null;
    }

    public static void main(String[] args) {

        int numberCount = 1000000;

        int maxNumber = numberCount;

        int inputArray[] = new int[numberCount];

        Random random = new Random();

        for (int i = 0; i < numberCount; ++i) {

            inputArray[i] = Math.abs(random.nextInt(maxNumber));

        }

        System.out.println("Sort begin...");

        long current = System.currentTimeMillis();

        int[] result = Top100.getTop100(inputArray);

        System.out.println(System.currentTimeMillis() - current + "ms");

        for (int i = 0; i < result.length; ++i) {

            System.out.print(i + "." + result[i] + ",");

        }

    }

}

class Node {
    protected int value;
    protected Node left;
    protected Node right;

    public Node(int value) {
        this.value = value;
    }

    public Node(int value, Node left) {
        this.value = value;
        this.left = left;
    }

    public Node(Node right, int value) {
        this.right = right;
        this.value = value;
    }
}
