
package mybnb.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="auth", url="${api.url.auth}")
public interface AuthService {

    @RequestMapping(method= RequestMethod.POST, path="/auths")
    public void auth(@RequestBody Auth auth);

}