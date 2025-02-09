package com.example.acfac.rest;

import com.example.acfac.config.HttpRequestProcessorStrategy;
import jakarta.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoadBalancerController {

    private final HttpRequestProcessorStrategy processorStrategy;

    @RequestMapping(value = "/forward", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<String> forward(
        HttpServletRequest servletRequest,
        @RequestBody UserRequestDto dto
    ) {
        String clientIp = getClientIp(servletRequest);
        HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod());
        return processorStrategy.getProcessor(method.name()).processRequest(dto, clientIp);
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
                return inetAddress.getHostAddress(); // ipV6
            } else if (inetAddress != null) {
                return inetAddress.getHostAddress(); // ipV4
            }
        }catch(UnknownHostException e){
            log.info("Invalid Ip address: {}", ip);
            throw new RuntimeException("Invalid Ip address: " + ip, e);
        }
        return ip;
    }
}

