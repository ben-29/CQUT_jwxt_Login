/**
 * Created by ben_29 on 6/21/2016 0021.
 */

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CQUT_jwxt_Login {
    private static final String baseUri = "http://jwxt.i.cqut.edu.cn";
    private static final String loginUrl = "http://i.cqut.edu.cn/zfca/login?yhlx=student&login=0122579031373493728&url=xs_main.aspx";

    public static  void showGrade(List<GradeInfo> list){
        if(list != null){
            System.out.println("\t学年 学期\t成绩\t学分\t绩点\t课程名称");
            double sum = 0,avg_grade = 0,sum_credit = 0;
            int fail = 0,max =0;
            for(GradeInfo i : list){
                System.out.println(i);
                int grade ;
                double point = Double.parseDouble(i.point);
                if(point == 0){
                    grade = 0;
                    fail += 1;
                }
                else{
                    grade = (int) (point * 10 +50);
                }
                if(grade > max)
                    max = grade;
                sum += grade;
                avg_grade += Double.parseDouble(i.credit) * Double.parseDouble(i.point);
                sum_credit += Double.parseDouble(i.credit);
            }
            int total = list.size()-fail;
            System.out.printf("课程总数: %d 平均分: %.2f 平均绩点: %.2f 课程最高分: %d",total,sum/total,avg_grade/sum_credit,max);
        }
    }
    /*
     * 获取成绩
     */
    public static List<GradeInfo> getGrade(String logUri,String id) {
//        String session = logUri.substring(logUri.indexOf('('),logUri.indexOf(')')+1);
//        System.out.println(session);
        String session = logUri.substring(1,27);
        String target = baseUri +'/'+ session + "/xscj_gc.aspx?xh=" + id + "&type=1";
        HttpResponse r;
        HttpContext httpContext = new BasicHttpContext();
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet get = null;
        HttpPost post = null;
        try {
                get = new HttpGet(target);
                r = httpClient.execute(get, httpContext);
                HttpEntity entity = r.getEntity();
                int statusCode = r.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    if (null != entity) {
                        String views = Jsoup
                                .parse(EntityUtils.toString(entity, ContentType
                                        .getOrDefault(entity).getCharset()))
                                .getElementsByAttributeValue("name","__VIEWSTATE")
                                .attr("value");
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("__VIEWSTATE", views));
                        params.add(new BasicNameValuePair("Button2", URLEncoder
                                .encode("在校学习成绩查询", "gbk")));

                        post = new HttpPost(target);
                        entity = new UrlEncodedFormEntity(params,ContentType.getOrDefault(entity).getCharset());
                        post.setEntity(entity);
                        r = httpClient.execute(post);
                        entity = r.getEntity();
                        // System.out.println(r.getStatusLine()+"\n"+params.toString());
                        //System.out.println(r.getStatusLine() + "\n");
                        statusCode = r.getStatusLine().getStatusCode();
                        if (statusCode == 200) {
                            Document d = Jsoup.parse(EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset()));
                            EntityUtils.consume(entity);
                            Element es = d.getElementById("Datagrid1");
                            List<GradeInfo> lg = new ArrayList<>();
                            String[] temp = es.html().split("</tr>");
                            for (int i = 1; i < temp.length - 1; i++) {
                                String[] temp1 = temp[i].split("<td>");
                                GradeInfo g = new GradeInfo();
                                g.stuYear = deletetd(temp1[1]);
                                g.stuPeriod = deletetd(temp1[2]);
                                g.className = deletetd(temp1[4]);
                                g.credit = deletetd(temp1[7]);
                                g.point = deletetd(temp1[8]);
                                g.grade = deletetd(temp1[9]);
                                lg.add(g);
                            }
                            return lg;
                        }
                    } else
                        return null;
                } else
                    return null;
        } catch (ClientProtocolException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        finally {
            if(get!=null)
                get.abort();
            if(post!=null)
                post.abort();
        }
        return null;
    }
    public static String deletetd(String o) {
        return o.substring(0, o.length() - 8);
    }
    public static void main(String[] args) {
        //TODO 在此输入学号密码
        String username = "11303010126";
        String password = "ben_29";
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("请在函数入口输入学号和密码");
        } else {
            CloseableHttpResponse response = null;
            HttpPost post = null;
            HttpGet get = new HttpGet(loginUrl);
            HttpContext httpContext = new BasicHttpContext();

            CloseableHttpClient httpClient =//增加 302 自动跳转
                    HttpClients.custom().setRedirectStrategy(new RedirectStrategy() {
                        @Override
                        public boolean isRedirected(HttpRequest arg0, HttpResponse arg1,
                                                    HttpContext arg2) throws ProtocolException {
                            return arg1.getStatusLine().getStatusCode() == 302;
                        }

                        @Override
                        public HttpUriRequest getRedirect(HttpRequest arg0, HttpResponse arg1,
                                                          HttpContext arg2) throws ProtocolException {
                            String url = arg1.getFirstHeader("Location").getValue();
                            if (!url.startsWith("http://"))
                                url = "http://jwxt.i.cqut.edu.cn" + url;
                            return new HttpGet(url);
                        }
                    }).build();
            String Cookie = null, lt = null;
            try {
                response = httpClient.execute(get, httpContext);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    org.apache.http.Header[] h = response.getAllHeaders();
                    for (org.apache.http.Header i : h) {
//                    System.out.println(i.getName() + " - " + i.getValue() + " - " + i.getElements());
                        if (i.getName().equals("Set-Cookie")) {
                            Cookie = i.getValue();
                        }
                    }
                    post = new HttpPost(loginUrl);
                    //增加cookie
                    post.addHeader("Cookie", Cookie);
                    //获取lt
                    HttpEntity entityhost = response.getEntity();
                    if (entityhost != null) {
                        lt = Jsoup.parse(EntityUtils.toString(entityhost, ContentType.getOrDefault(entityhost).getCharset()))
                                .getElementsByAttributeValue("name", "lt")
                                .attr("value");
                        EntityUtils.consume(entityhost);
                    }
                    //增加参数
                    List<NameValuePair> formparams = new ArrayList<>();
                    formparams.add(new BasicNameValuePair("lt", lt));
                    formparams.add(new BasicNameValuePair("username", username));
                    formparams.add(new BasicNameValuePair("password", password));
                    formparams.add(new BasicNameValuePair("_eventId", "submit"));
                    UrlEncodedFormEntity e = new UrlEncodedFormEntity(formparams, "UTF-8");
                    post.setEntity(e);
                    response = httpClient.execute(post, httpContext);
                    HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute("http.request");
                    if (realRequest != null) {
                        String logUri = realRequest.getURI().toString();
                        if (logUri.contains(username)) {//success
                            System.out.println("登录成功 登录链接: " + baseUri + logUri);
                            List<GradeInfo> list = getGrade(logUri,username);
                            showGrade(list);
                        } else {
                            System.out.println("学号或密码错误");
                        }
                    } else {
                        System.out.println("学号或密码错误");
                    }
                } else {
                    System.out.println("教务系统暂时不可用");
                }
            } catch (IOException e) {
//            e.printStackTrace();
                System.out.println("未连接到互联网");
            } finally {
                get.abort();
                if (post != null) {
                    post.abort();
                }
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
