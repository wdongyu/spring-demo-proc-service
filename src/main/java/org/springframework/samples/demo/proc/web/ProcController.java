package org.springframework.samples.demo.proc.web;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;



/**
 * @author wdongyu
 */

@RestController
public class ProcController {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient client;

    @RequestMapping(value = "/proc/{authPort}/{username}/{password}" ,method = RequestMethod.GET)
    public String proc(@PathVariable String authPort, @PathVariable String username, @PathVariable String password) {
        ServiceInstance instance = client.getLocalServiceInstance();
        //logger.info("/db, host:" + instance.getHost() + ", service_id:" + instance.getServiceId());
        //return "Info from Database";

        //String authUrl = serviceUrlWithId("auth-service", commitId) + "/auth/proc/" + username + "/" + password;   
        String fromAuth = restTemplate.getForEntity("http://" + instance.getHost() + ":" + authPort + "/auth/proc/" + username + "/" + password, String.class).getBody();
        if (fromAuth.equals("Pass")) {
            String dbUrl = serviceUrl("db-service");
            if (dbUrl == null)
                return "No db-service available";
            else 
                return restTemplate.getForEntity(dbUrl + "/db", String.class).getBody();
        }
        else
            return "Fail to authentication";

        //String fromAuth = restTemplate.getForEntity("http://auth-service/auth",String.class).getBody();
        //String fromDb = restTemplate.getForEntity("http://db-service/db",String.class).getBody();
        //return (" ---  Process part  --- " + '\n' + fromAuth + '\n' + fromDb);
    }


    public String serviceUrlWithId(String serviceName, String commitId) {
        List<ServiceInstance> list = this.client.getInstances(serviceName);
        try {
            if (list != null && list.size() > 0 ) {
                for (int i = list.size()-1; i >= 0; i--) {
                    URL url = new URL(list.get(i).getUri().toString() + "/info");
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
                    StringBuffer bs = new StringBuffer();
                    String str = null;
                    while((str=buffer.readLine())!=null){
                        bs.append(str);
                    }
                    buffer.close();
                    if (commitId.equals(getCommitId(bs.toString()))) 
                        return list.get(i).getUri().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list.get(list.size()-1).getUri().toString();
    }

    public String serviceUrl(String serviceName) {
        List<ServiceInstance> list = this.client.getInstances(serviceName);
        try {
            if (list != null && list.size() > 0 ) {
                for (int i = list.size()-1; i >= 0; i--) {
                    URL url = new URL(list.get(i).getUri().toString() + "/health");
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
                    StringBuffer bs = new StringBuffer();
                    String str = null;
                    while((str=buffer.readLine())!=null){
                        bs.append(str);
                    }
                    buffer.close();
                    String status = getStatus(bs.toString());
                    logger.info(status);
                    if (status != null && status.equals("UP"))
                        return list.get(i).getUri().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCommitId(String str) {
        try {
            if (str == null) return null;
            JSONObject json = (JSONObject)(new JSONParser().parse(str));
            json = (JSONObject)(json.get("git"));
            json = (JSONObject)(json.get("commit"));
            return json.get("id").toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStatus(String str) {
        try {
            JSONObject json = (JSONObject)(new JSONParser().parse(str));
            return json.get("status").toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
