package ua.com.juja.microservices.teams.dao.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "gateway")
public interface KeeperClient {
    @RequestMapping(method = RequestMethod.GET, value = "/v1/keepers/{uuid}", consumes =
            MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<String> getDirections(@RequestParam(value = "uuid") String uuid);
}
