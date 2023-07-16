package com.xqt.controller;


import com.xqt.util.Request;
import com.xqt.util.RequestUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class request_proxy_controller {

    Logger logger = LoggerFactory.getLogger("proxy_request");

    @PostMapping("request_post")
    public String request_post(@RequestBody String req){
        Request<String, JSONObject> request = RequestUtil.getRequest(req,logger);
        return request.postRequest().toString();
    }

    @PostMapping("request_get")
    public String request_get(@RequestBody String req){
        Request<String, JSONObject> request = RequestUtil.getRequest(req,logger);
        return request.getRequest().toString();
    }

    @GetMapping("gettest")
    public void request_get(){
        logger.info("发送状态成功");
    }

}
