package com.example.acfac.rest;

import com.example.acfac.common.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
public class LoadBalancerController {

    private final RequestProcessor requestProcessor;

    public LoadBalancerController(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    @RequestMapping(value = "/forward", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.DELETE})
    public Mono<String> forward(
        HttpServletRequest servletRequest,
        @RequestParam String userRequest
    ) {
        String clientIp = getClientIp(servletRequest);
        String httpMethod = servletRequest.getMethod();
        return requestProcessor.processRequest(userRequest, clientIp, httpMethod);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        }else{
            ip = request.getRemoteAddr();
        }

        try{
            InetAddress inetAddress = InetAddress.getByName(ip);
            if (inetAddress instanceof Inet6Address) {
                return inetAddress.getHostAddress(); //ipV6
            } else if (inetAddress != null) {
                return inetAddress.getHostAddress(); //ipV4
            }
        }catch(UnknownHostException e){
            log.info("Invalid Ip address: {}", ip);
            throw new RuntimeException("Invalid Ip address: " + ip, e);
        }
        return ip;
    }
}

