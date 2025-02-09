package com.example.acfac.rest;

import com.example.acfac.common.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoadBalancerController {

    private final RequestProcessor requestProcessor;

    @PostMapping(value = "/forward")
    public Mono<String> forward(
        HttpServletRequest servletRequest,
        @RequestBody UserRequestDto dto
    ) {
        String clientIp = getClientIp(servletRequest);
        return requestProcessor.processRequest(dto, clientIp);
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

