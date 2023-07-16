package com.xqt.util;


import lombok.NoArgsConstructor;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;

public class RequestUtil<T,D> extends Request{

    public static  Request getRequest(String content, Logger logger) {
        JSONObject jsonObject = JSONObject.fromObject(content);
        Request requestUtil = new RequestUtil(jsonObject.getString("url"), jsonObject.getJSONObject("headers"), jsonObject.getString("ip"),jsonObject.getString("params"),logger);
        return requestUtil;
    }




    public RequestUtil(String url, JSONObject headers, String ip,T params,Logger logger) {
        this.url = url;
        this.headers = headers;
        this.client = getHttpsClient(ip);
        this.t = params;
        this.logger = logger;
        this.d = JSONObject.class;
    }

    @Override
    public D postRequest() {
        HttpPost httpPost = new HttpPost(url);
        setHeader(httpPost);
        try {
            httpPost.setEntity(new StringEntity((String) t));
            HttpResponse response = client.execute(httpPost);
            logger.info("post-request--"+url+"--"+response+"");
            return (D) getResult(response);
        }catch (Exception e){
            logger.info("post-request-"+url+"-exception--"+e.getMessage()+"");
            return (D) getResultException(e);
        }
    }

    @Override
    public Object getRequest() {
        HttpGet httpGet = new HttpGet(url);
        setHeader(httpGet);
        try {
            HttpResponse response = client.execute(httpGet);
            logger.info("get-request--"+url+"--"+response+"");
            return (D) getResult(response);
        }catch (Exception e){
            logger.info("get-request-"+url+"-exception--"+e.getMessage()+"");
            return (D) getResultException(e);
        }
    }


}
