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

public class main {
    public static void main(String[] args) {
        String username = "";
        String password = "";
        CloseableHttpResponse response = null;
        HttpPost post = null;
        HttpGet get = new HttpGet("http://i.cqut.edu.cn/zfca/login?yhlx=student&login=0122579031373493728&url=xs_main.aspx");
        HttpContext httpContext = new BasicHttpContext();

        CloseableHttpClient httpClient =//增加 302 自动跳转
                HttpClients.custom().setRedirectStrategy(new RedirectStrategy() {
                    @Override
                    public boolean isRedirected(HttpRequest arg0, HttpResponse arg1,
                                                HttpContext arg2) throws ProtocolException {
                        if (arg1.getStatusLine().getStatusCode() == 302)
                            return true;
                        else
                            return false;
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
        String jsessionid = null, lt = null;
        try {
            response = httpClient.execute(get, httpContext);
            if (response.getStatusLine().getStatusCode() == 200) {
                org.apache.http.Header[] h = response.getAllHeaders();
                for (org.apache.http.Header i : h) {
//                    System.out.println(i.getName() + " - " + i.getValue() + " - " + i.getElements());
                    if (i.getName().equals("Set-Cookie")) {
                        String value = i.getValue();
                        jsessionid = value.substring(value.indexOf('=') + 1, value.indexOf(';'));
                    }
                }
//                System.out.println(jsessionid);
                //增加cookie
                post = new HttpPost("http://i.cqut.edu.cn/zfca/login;JSESSIONID=" + jsessionid + "?yhlx=student&login=0122579031373493728&url=xs_main.aspx");
                HttpEntity entityhost = response.getEntity();
                if (entityhost != null) {
                    lt = Jsoup.parse(EntityUtils.toString(entityhost, ContentType.getOrDefault(entityhost).getCharset()))
                            .getElementsByAttributeValue("name", "lt")
                            .attr("value");
                    EntityUtils.consume(entityhost);
                }
                List<NameValuePair> formparams = new ArrayList<>();
                formparams.add(new BasicNameValuePair("lt", lt));
                formparams.add(new BasicNameValuePair("username", username));
                formparams.add(new BasicNameValuePair("password", password));
                formparams.add(new BasicNameValuePair("_eventId", "submit"));
                UrlEncodedFormEntity e = new UrlEncodedFormEntity(formparams, "UTF-8");
                post.setEntity(e);
                response = httpClient.execute(post, httpContext);

                HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
                String logUri = realRequest.getURI().toString();
                if (logUri.contains(username)) {//success
                    System.out.println(logUri);
                } else {
//                    System.out.println("账号或密码错误");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            get.abort();
            if (post != null) {
                post.abort();
            }
        }
        System.out.println("end");
    }

}
