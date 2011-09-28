/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package org.apache.lucene.demo;

import java.io.File;
import java.util.Date;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 * Searcher.java
 * 
 * @author baojun
 */
public class Searcher {
    private static String INDEX_DIR = "D:\\test\\index\\";// 索引所在的路径
    private static String KEYWORD = "main";// 关键词
    private static int TOP_NUM = 2;// 显示前100条结果

    public static void main(String[] args) throws Exception {
        File indexDir = new File(INDEX_DIR);
        if (!indexDir.exists() || !indexDir.isDirectory()) {
            throw new Exception(indexDir + " does not exist or is not a directory.");
        }
        search(indexDir, KEYWORD);// 调用search方法进行查询
    }

    /**
     * 查询
     * 
     * @param indexDir
     * @param q
     * @throws Exception
     */
    public static void search(File indexDir, String q) throws Exception {
        IndexSearcher is = new IndexSearcher(new SimpleFSDirectory(indexDir));// read-only
        String field = "contents";

        QueryParser parser = new QueryParser(Version.LUCENE_34, field, new WhitespaceAnalyzer(Version.LUCENE_34));// 有变化的地方
        Query query = parser.parse(q);

        TopScoreDocCollector collector = TopScoreDocCollector.create(TOP_NUM, false);// 有变化的地方

        long start = new Date().getTime();// start time

        is.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        System.out.println(hits.length);
        for (int i = 0; i < hits.length; i++) {
            Document doc = is.doc(hits[i].doc);// new method is.doc()
            System.out.println(doc.getField("filename") + "   " + hits[i].toString() + "  ");
        }
        long end = new Date().getTime();// end time

        System.out.println("Found " + collector.getTotalHits() + " document(s) (in " + (end - start)
                + " milliseconds) that matched query '" + q + "':");

    }

}
