/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package org.apache.lucene.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * TxtFileIndexer.java
 * 
 * @author baojun
 */
public class TxtFileIndexer {
    public static void main(String[] arg) {
        String outputpath = "D:\\test\\small\\";// 小文件存放路径
        String filename = "D:\\test\\三国演义.txt";// 原文件存放路径
        if (!new File(outputpath).exists()) {
            new File(outputpath).mkdirs();
        }
        splitToSmallFiles(new File(filename), outputpath);
    }

    /**
     * 大文件切割为小的
     * 
     * @param file
     * @param outputpath
     */
    public static void splitToSmallFiles(File file, String outputpath) {
        int filePointer = 0;
        int MAX_SIZE = 10240;
        String filename = "output";

        BufferedWriter writer = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line).append("\r\n");
                if (buffer.toString().getBytes().length >= MAX_SIZE) {
                    writer = new BufferedWriter(new FileWriter(outputpath + filename + filePointer + ".txt"));
                    writer.write(buffer.toString());
                    writer.close();
                    filePointer++;
                    buffer = new StringBuffer();
                }
                line = reader.readLine();
            }
            writer = new BufferedWriter(new FileWriter(outputpath + filename + filePointer + ".txt"));
            writer.write(buffer.toString());
            writer.close();
            System.out.println("The file hava splited to small files !");
        }
        catch (FileNotFoundException e) {
            System.out.println("file not found !");
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
