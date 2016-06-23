/**
 * Created by ben_29 on 6/21/2016 0021.
 */

import org.apache.http.*;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CQUT_jwxt_Login {

    public static void main(String[] args) {
        //TODO 在此输入学号密码
        String username = "11303010126";
        String password = "ben_29";
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("请在函数入口输入学号和密码");
        } else {
            final String baseUri = "http://jwxt.i.cqut.edu.cn";
            final String loginUrl = "http://i.cqut.edu.cn/zfca/login?yhlx=student&login=0122579031373493728&url=xs_main.aspx";
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
                        } else {
                            System.out.println("学号或密码错误");
                        }
                    } else {
                        System.out.println("学号或密码错误");
                    }

                } else {
                    System.out.println("教务系统暂时不可用");
                }
            } catch (IOException e) {//未连接到互联网
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
