/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package org.apache.lucene.demo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 * Indexer.java
 * 
 * @author baojun
 */
public class Indexer {
    private static String INDEX_DIR = "D:\\test\\index";// 索引存放目录
    private static String DATA_DIR = "D:\\test\\small\\";// 小文件存放的目录

    public static void main(String[] args) throws Exception {

        long start = new Date().getTime();
        int numIndexed = index(new File(INDEX_DIR), new File(DATA_DIR));// 调用index方法
        long end = new Date().getTime();
        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }

    /**
     * 索引dataDir下的.txt文件，并储存在indexDir下，返回索引的文件数量
     * 
     * @param indexDir
     * @param dataDir
     * @return int
     * @throws IOException
     */
    public static int index(File indexDir, File dataDir) throws IOException {

        if (!dataDir.exists() || !dataDir.isDirectory()) {
            throw new IOException(dataDir + " does not exist or is not a directory");
        }
        //
        // IndexWriter writer =
        // new IndexWriter(FSDirectory.open(indexDir), new StandardAnalyzer(Version.LUCENE_CURRENT), true,
        // IndexWriter.MaxFieldLength.LIMITED);// 有变化的地方
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, new WhitespaceAnalyzer(Version.LUCENE_34));
        Directory directory = new SimpleFSDirectory(indexDir);
        IndexWriter writer = new IndexWriter(directory, config);
        indexDirectory(writer, dataDir);
        int numIndexed = writer.numDocs();
        writer.optimize();
        writer.close();
        return numIndexed;
    }

    /**
     * 循环遍历目录下的所有.txt文件并进行索引
     * 
     * @param writer
     * @param dir
     * @throws IOException
     */
    private static void indexDirectory(IndexWriter writer, File dir) throws IOException {

        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                indexDirectory(writer, f); // recurse
            }
            else if (f.getName().endsWith(".txt")) {
                indexFile(writer, f);
            }
        }
    }

    /**
     * 对单个txt文件进行索引
     * 
     * @param writer
     * @param f
     * @throws IOException
     */
    private static void indexFile(IndexWriter writer, File f) throws IOException {

        if (f.isHidden() || !f.exists() || !f.canRead()) {
            return;
        }

        System.out.println("Indexing " + f.getCanonicalPath());
        Document doc = new Document();
        doc.add(new Field("contents", new FileReader(f)));// 有变化的地方
        doc.add(new Field("filename", f.getCanonicalPath(), Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));// 有变化的地方

        writer.addDocument(doc);
    }
}
