/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 一个关键字标红的通用类<br/>
 * 文本坐标记用的 <br/>
 * tagBegin是开始标记 <br/>
 * tagEnd 是结束标记 <br/>
 * 用了二分法查找来确定单词 <br/>
 * content 是传入的正文 正文可以传多次 标记词语也可以传多次
 * 
 * @author Ansj
 */
public class TagWord {
    private String tagBegin;
    private String tagEnd;
    Branch frontbegin = null;
    Set<String> keyWords = new HashSet<String>();

    public TagWord(String begin, String end) {
        tagBegin = begin;
        tagEnd = end;
    }

    public TagWord addKeyWords(String[] keyWord) {
        if (keyWord.length > 0) {
            for (int i = 0; i < keyWord.length; i++) {
                keyWords.add(keyWord[i].trim());
            }
        }
        return this;
    }

    // 是否发现词
    boolean findWord = false;

    public String getTagContent(String content) {
        if (content == null || content.trim().length() == 0
                || keyWords.size() == 0) {
            return content;
        }
        frontbegin = new MakeLibrary().getStringTree(keyWords);
        if (frontbegin == null) {
            return content;
        }
        char[] chars = content.toCharArray();
        // 正文
        StringBuilder sb = new StringBuilder();

        WoodInterface head = frontbegin;
        int start = 0;
        int end = 1;
        int index = 0;
        boolean isBack = false;
        int length = chars.length;
        // 此处是正向最大匹配
        for (int i = 0; i < length; i++) {
            index++;
            head = head.get(chars[i]);
            if (head == null) {
                if (isBack) {
                    sb.append(tagBegin).append(chars, start, end)
                            .append(tagEnd);
                    start = start + end;
                    i = start - 1;
                    isBack = false;
                } else {
                    sb.append(chars, start, end);
                    i = start;
                    start++;
                }
                head = frontbegin;
                index = 0;
                end = 1;
                continue;
            }
            switch (head.getStatus()) {
                case 1 :
                    break;
                case 2 :
                    end = index;
                    isBack = true;
                    break;
                case 3 :
                    sb.append(tagBegin).append(chars, start, index).append(
                            tagEnd);
                    start = start + index;
                    index = 0;
                    end = 1;
                    isBack = false;
                    head = frontbegin;
                    break;
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String[] keyWords = { "中华人民共和国", "孙健", "伟大", "人民", "中华", "万岁" };
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            String str =
                    new TagWord("<begin>", "<end>")
                            .addKeyWords(keyWords)
                            .getTagContent(
                                    "中华人民共和国是一个伟大的民族我们有振兴民族的需要孙健万岁 . 中 国 万万岁哈哈  。");
            System.out.println(str);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

}

class MakeLibrary {

    public MakeLibrary() {
    }

    // 是否有下一个
    private static boolean hasNext = true;
    // 是否是一个词
    private static boolean isWords = true;

    Iterator<String> it = null;

    public Branch getStringTree(Set<String> keyWords) {
        it = keyWords.iterator();
        Branch head = new Branch('h', 0, 0);
        Branch branch = head;

        while (it.hasNext()) {
            char[] chars = it.next().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars.length == (i + 1)) {
                    isWords = true;
                    hasNext = false;
                } else {
                    isWords = false;
                    hasNext = true;
                }
                int status = 1;
                if (isWords && hasNext) {
                    status = 2;
                }

                if (!isWords && hasNext) {
                    status = 1;
                }

                if (isWords && !hasNext) {
                    status = 3;
                }
                branch.add(new Branch(chars[i], status, 0));
                branch = (Branch) branch.get(chars[i]);
            }
            branch = head;
        }
        return head;
    }
}

interface WoodInterface {
    public WoodInterface add(WoodInterface branch);

    public WoodInterface get(char c);

    public boolean contains(char c);

    public int compareTo(char c);

    public boolean equals(char c);

    public byte getStatus();

    public char getC();

    public void setStatus(int status);

    public byte getNature();

    public void setNature(byte nature);
}

class Branch implements WoodInterface {
    /**
     * status 此字的状态1，继续 2，是个词语但是还可以继续 ,3确定 nature 词语性质 0.未知 . 1是姓 . 2 是职位名称 3
     * 是数量级的词 . 4 是数字词语 5 是标点
     */
    WoodInterface[] branches = new WoodInterface[0];
    private char c;
    // 状态
    private byte status = 1;
    // 索引
    private short index = -1;
    // 词性
    private byte nature = 0;
    // 单独查找出来的对象
    WoodInterface branch = null;

    public WoodInterface add(WoodInterface branch) {
        if ((this.branch = get(branch.getC())) != null) {
            switch (branch.getStatus()) {
                case 1 :
                    if (this.branch.getStatus() == 2) {
                        this.branch.setStatus(2);
                    }
                    if (this.branch.getStatus() == 3) {
                        this.branch.setStatus(2);
                    }
                    break;
                case 2 :
                    this.branch.setStatus(2);
                case 3 :
                    if (this.branch.getStatus() == 2) {
                        this.branch.setStatus(2);
                    }
                    if (this.branch.getStatus() == 1) {
                        this.branch.setStatus(2);
                    }
            }
            this.branch.setNature(branch.getNature());
            return this.branch;
        }
        index++;
        if ((index + 1) > branches.length) {
            branches = Arrays.copyOf(branches, index + 1);
        }
        branches[index] = branch;
        AnsjArrays.sort(branches);
        return branch;
    }

    public Branch(char c, int status, int nature) {
        this.c = c;
        this.status = (byte) status;
        this.nature = (byte) nature;
    }

    int i = 0;

    public WoodInterface get(char c) {
        int i = AnsjArrays.binarySearch(branches, c);
        if (i > -1) {
            return branches[i];
        }
        return null;
    }

    public boolean contains(char c) {
        if (AnsjArrays.binarySearch(branches, c) > -1) {
            return true;
        } else {
            return false;
        }
    }

    public int compareTo(char c) {
        if (this.c > c) {
            return 1;
        } else if (this.c < c) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean equals(char c) {
        if (this.c == c) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return c;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = (byte) status;
    }

    public char getC() {
        return c;
    }

    public byte getNature() {
        return nature;
    }

    public void setNature(byte nature) {
        this.nature = nature;
    }

}

class AnsjArrays {
    private static final int INSERTIONSORT_THRESHOLD = 7;

    /**
     * 二分法查找.摘抄了jdk的东西..只不过把他的自动装箱功能给去掉了
     * 
     * @param branches
     * @param c
     * @return
     */
    public static int binarySearch(WoodInterface[] branches, char c) {
        int high = branches.length - 1;
        if (branches.length < 1) {
            return high;
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = branches[mid].compareTo(c);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -1; // key not found.
    }

    public static void sort(WoodInterface[] a) {
        WoodInterface[] aux = a.clone();
        mergeSort(aux, a, 0, a.length, 0);
    }

    public static void sort(WoodInterface[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        WoodInterface[] aux = copyOfRange(a, fromIndex, toIndex);
        mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
    }

    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLen) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }

    private static void mergeSort(WoodInterface[] src, WoodInterface[] dest,
            int low, int high, int off) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low
                        && (dest[j - 1]).compareTo(dest[j].getC()) > 0; j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }

        // Recursively sort halves of dest into src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (src[mid - 1].compareTo(src[mid].getC()) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && src[p].compareTo(src[q].getC()) <= 0) {
                dest[i] = src[p++];
            } else {
                dest[i] = src[q++];
            }
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(WoodInterface[] x, int a, int b) {
        WoodInterface t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    public static <T> T[] copyOfRange(T[] original, int from, int to) {
        return copyOfRange(original, from, to, (Class<T[]>) original.getClass());
    }

    public static <T, U> T[] copyOfRange(U[] original, int from, int to,
            Class<? extends T[]> newType) {
        int newLength = to - from;
        if (newLength < 0) {
            throw new IllegalArgumentException(from + " > " + to);
        }
        T[] copy =
                ((Object) newType == (Object) Object[].class)
                        ? (T[]) new Object[newLength] : (T[]) Array
                                .newInstance(newType.getComponentType(),
                                        newLength);
        System.arraycopy(original, from, copy, 0, Math.min(original.length
                - from, newLength));
        return copy;
    }
}
