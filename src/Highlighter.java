/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Highlighter.java
 * 
 * @author baojun
 */
public class Highlighter {
    public static void main(String[] args) {
        String content =
                "挖掘频繁项集的方法可以扩展到挖掘闭频繁项集（由它们容易导出频繁项集的集合）。这些方法结合了附加的优化技术，如项合并、子项剪枝和项跳过，以及模式树中产生的项集的有效子集检查。挖掘频繁项集和关联已经用不同的方法扩展，包括挖掘多层关联规则和多维关联规则。多层关联规则可以根据每个抽象层的最小支持度阈值如何定义，使用多种策略挖掘。如一致的支持度、递减的支持度和基于分组的支持度。冗余的多层（后代）关联规则可以删除，如果根据其对应的祖先规则，他们的支持度和置信度接近于期望值的话。挖掘多维关联规则的技术可以根据对量化属性的处理分为若干类。第一，量化属性可以根据预定义的概念分层静态离散化。数据立方体非常适合这种方法，因为数据立方体和量化属性都可以利用概念分层。第二，可以挖掘量化关联规则，其中量化属性根据分箱和/或聚类动态离散化，“邻近的”关联规则可以用聚类合并，产生更简洁、更有意义的规则。基于约束的规则挖掘允许用户通过提供元规则（即模式模板）和其他挖掘约束对规则搜索聚焦。这种挖掘推动了说明性数据挖掘查询语言和用户界面的使用，并对挖掘查询优化提出了巨大挑战。规则约束可以分为五类：反单调的、单调的、简洁的、可转变的和不可转变的。前四类约束可以在频繁项集挖掘中使用，使挖掘更有功效，更有效率。没有进一步分析或领域知识，关联规则不应该直接用于预测。它们不必指示因果关系。然而，对于进一步探查，它们是有帮助的切入点，使得它们成为理解数据的流行工具。流数据不断地在计算机系统中流进流出并且具有变化的更新速度，涉及数据流的应用非常广泛。大纲提供数据流的汇总，通常用来返回查询的近似解答。随机抽样、滑动窗口、直方图、多分辨率方法、梗概以及随机算法都是大纲的形式。倾斜时间框架模型允许数据以多个时间粒度存储，最近的时间记录在最细的粒度上，最远的时间记录在最粗的粒度上。流立方体可以存储压缩的数据，对时间维度使用倾斜时间框架模型，并且仅在一些关键的层上存储数据，关键层反映了分析人员最感兴趣的数据层，从而基于到关键层的“常用路径”进行部分物化。";
        String query = "数据挖掘";
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            String str = new Highlighter(query).getBestFragment(content);
        }
        String str = new Highlighter(query).getBestFragment(content);
        System.out.println(str);
        System.out.println(System.currentTimeMillis() - start);
    }

    private static String BEGIN = "<font color=\"red\">";
    private static String END = "</font>";
    private static int size = 250;

    private Set<Character> set = new HashSet<Character>();

    public Highlighter(String query) {
        char[] chars = null;
        chars = query.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            set.add(chars[i]);
        }
    }

    public String getBestFragment(String content) {
        String[] strs = content.replace(".", "。").split("。");
        char[] chars = null;

        TreeSet<Sentence> ts = new TreeSet<Sentence>();
        Sentence sentence = null;
        int score = 0;
        StringBuilder sb = null;
        for (int i = 0; i < strs.length; i++) {
            sentence = new Sentence();
            sb = new StringBuilder();
            sentence.setIndex(i);
            sentence.setText(strs[i]);
            chars = strs[i].toCharArray();
            sentence.setValue(sb.toString());
            for (int j = 0; j < chars.length; j++) {
                if (set.contains(chars[j])) {
                    score++;
                    sb.append(BEGIN);
                    sb.append(chars[j]);
                    sb.append(END);
                } else {
                    sb.append(chars[j]);
                }
            }
            sentence.setValue(sb.toString());
            sentence.setScore(score);
            ts.add(sentence);
            score = 0;
            sb = new StringBuilder();
        }

        Iterator<Sentence> it = ts.iterator();
        int thisSize = 0;

        List<Sentence> all = new ArrayList<Sentence>();
        while (it.hasNext()) {
            sentence = it.next();
            thisSize += sentence.getText().length();
            all.add(sentence);
            if (thisSize >= size) {
                break;
            }
        }

        Object[] sentences = all.toArray();
        Object obj = null;
        for (int i = 0; i < sentences.length; i++) {
            for (int j = i; j < sentences.length; j++) {
                if (((Sentence) sentences[j]).getIndex() < ((Sentence) sentences[i])
                        .getIndex()) {
                    obj = sentences[i];
                    sentences[i] = sentences[j];
                    sentences[j] = obj;
                }
            }
        }
        sb = new StringBuilder();
        for (int i = 0; i < sentences.length; i++) {
            sb.append(((Sentence) sentences[i]).getValue());
            sb.append("。");
        }

        return sb.toString();
    }

    class Sentence implements Comparable<Sentence> {
        String value;
        int index;
        int score;
        String text;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int compareTo(Sentence o) {
            // TODO Auto-generated method stub
            if (score > o.score) {
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        public String toString() {
            return index + " " + score + "  " + value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

    }
}
