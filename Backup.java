import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Backup
 *
 * @author: onlylemi
 * @time: 2016-04-05 16:53
 */
public class Backup {

    private static List<String> list;

    public static void main(String[] args) {
        list = new ArrayList<String>();
        String urlStr = "http://gank.io/history";

        // 生成历史列表
        writeREADMEFile(urlStr);

        // 生成每天推荐文章
        writeEveryDayFile(list.get(0));
    }

    /**
     * 生成 README 文件
     *
     * @param urlStr
     */
    public static void writeREADMEFile(String urlStr) {
        String html = getHTML(urlStr);
        StringBuilder sb = new StringBuilder();

        sb.append("# 干货集中营 [Gank.io](http://gank.io)");
        sb.append("\n\n");
        sb.append("> " + getTitle(html) + "  \n");
        sb.append("> 创始人：@[代码家](https://github.com/daimajia)  \n");
        sb.append("> Github备份：@[onlylemi](https://github.com/onlylemi)");
        sb.append("\n\n");
        sb.append(getHistory(html));

        writeMDFile("README.md", sb.toString());
    }

    /**
     * 生成每日推荐文章
     *
     * @param time
     */
    public static void writeEveryDayFile(String time) {
        String urlStr = "http://gank.io/" + time.replace('-', '/');
        String html = getHTML(urlStr);

        StringBuilder sb = new StringBuilder();
        sb.append("# " + getTitle(html));
        sb.append("\n\n");
        sb.append("> 原文链接：[" + urlStr + "](" + urlStr + ")");
        sb.append("\n\n");
        sb.append(getGirl(html));
        sb.append(getEveryDay(getHTML(urlStr)));

        String[] times = time.split("-");
        String filename = times[0] + "/" + times[1] + "/" + time + "-" + getTitle(html) + ".md";

        writeMDFile(filename, sb.toString());
    }

    /**
     * 得到页面文本
     *
     * @param urlStr
     * @return
     */
    public static String getHTML(String urlStr) {
        StringBuffer sb = new StringBuffer();

        URL url = null;
        String temp;
        try {
            url = new URL(urlStr);
            // 读取网页内容
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            while ((temp = in.readLine()) != null) {
                sb.append(temp);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 解析标题
     *
     * @param strHTML
     * @return
     */
    public static String getTitle(String strHTML) {
        String title = "";

        Matcher matcher = Pattern.compile("<title>(.+?)</title>").matcher(strHTML);
        if (matcher.find()) {
            title = matcher.group(1).replaceAll("\b", "");
        }
        return title;
    }

    /**
     * 解析美女src
     *
     * @param strHTML
     * @return
     */
    public static String getGirl(String strHTML) {
        StringBuilder src = new StringBuilder();

        Matcher matcher = Pattern.compile("<img.*?/>").matcher(strHTML);
        while (matcher.find()) {
            src.append("![](" + getUrl(matcher.group()) + ")");
            src.append("\n\n");
        }
        return src.toString();
    }

    /**
     * 匹配url
     *
     * @param str
     * @return
     */
    public static String getUrl(String str) {
        String href = "";
        Matcher matcher = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w - ./?%&=]*)?").matcher(str);
        if (matcher.find()) {
            href = matcher.group();
        }
        return href;
    }

    /**
     * 解析历史消息
     *
     * @param strHTML
     * @return
     */
    public static String getHistory(String strHTML) {
        StringBuilder arcticles = new StringBuilder();
        String time = "";
        String name;

        // 找到所有历史
        Pattern pattern = Pattern.compile("<li>\\s*<div class=\"row\">.*?</div>\\s*</li>");
        Matcher matcher = pattern.matcher(strHTML);
        while (matcher.find()) {
            // 得到某一行的信息
            String row = matcher.group();

            // 匹配名字
            Matcher matcherName = Pattern.compile("<a.*?</a>").matcher(row);

            // 匹配时间
            Matcher matcherTime = Pattern.compile("<span.*?</span>").matcher(row);


            if (matcherTime.find() && matcherName.find() && !time.equals(matcherTime.group().replaceAll("<.*?>", ""))) {
                time = matcherTime.group().replaceAll("<.*?>", "");
                arcticles.append("* `" + time + "` ");

                name = matcherName.group().replaceAll("<.*?>", "");
                arcticles.append("[" + name + "](http://gank.io/" + time.replace('-', '/') + ")" + "\n");

                list.add(time);
            }
        }
        return arcticles.toString();
    }

    /**
     * 解析每天的推荐页面
     *
     * @param strHTML
     * @return
     */
    public static String getEveryDay(String strHTML) {
        StringBuilder sb = new StringBuilder();
        // 获取div outlink部分
        Matcher matcherOutlink = Pattern.compile("<div class=\"outlink\">.*?</div>").matcher(strHTML);
        String outlink = "";
        if (matcherOutlink.find()) {
            outlink = matcherOutlink.group();
        }
        // 解析标签
        Matcher matcherTag = Pattern.compile("<h\\d[^>]*?>(<.+?>)?" +
                "(Android|iOS|前端|瞎推荐|休息视频|拓展资源|App|福利|晚间电影|晚间小游戏|晚间休息|周末大事回顾|昨日大事件|昨天大事件|微信号推荐|微信公众帐号推荐|YouTube" +
                "频道推荐|干货篮子：)(:|：)?</h\\d>")
                .matcher
                        (outlink);
        // 解析标签下的内容list
        Matcher matcherUl = Pattern.compile("(<(ul|ol).*?>).+?</(ul|ol)>").matcher(outlink);

        while (matcherTag.find()) {
            // 添加标记
            sb.append("### " + matcherTag.group(2));
            sb.append("\n\n");

            // 添加资源列表
            if (matcherUl.find()) {
                Matcher matcherLi = Pattern.compile("<li.*?>(.*?)</li>").matcher(matcherUl.group());
                while (matcherLi.find()) {
                    Matcher matcherA = Pattern.compile("<a[^>]*>([^<]*)</a>([^<]*)").matcher
                            (matcherLi.group(1));
                    if (matcherA.find()) {
                        sb.append("* [" + matcherA.group(1) + "](" + getUrl(matcherLi.group()) + ") " + matcherA
                                .group(2)
                                + "\n");
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 写入信息到文件
     */
    public static void writeMDFile(String filename, String content) {
        File file = new File("D://Github/gank.io/" + filename);
        File folder = new File(file.getParent());
        try {
            if (!folder.exists() && folder.isDirectory()) {
                // 文件不存在创建文件
                folder.mkdirs();
            }

            if (!file.exists() && file.isFile()) {
                // 文件不存在创建文件
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
