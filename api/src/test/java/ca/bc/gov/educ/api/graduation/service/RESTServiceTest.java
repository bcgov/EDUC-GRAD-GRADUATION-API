package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.ServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RESTServiceTest {

    @Autowired
    private RESTService restService;

    @Test
    public void testGet_GivenProperData_Expect200Response(){
        String response;
        response = this.restService.get("https://httpstat.us/200", String.class, "1234");
        Assert.assertEquals("200 OK", response);
    }

    @Test(expected = ServiceException.class)
    public void testGet_Given5xxErrorFromService_ExpectServiceError(){
        this.restService.get("https://httpstat.us/503", String.class, "1234");
    }

    @Test(expected = ServiceException.class)
    public void testGet_Given4xxErrorFromService_ExpectServiceError(){
        this.restService.get("https://httpstat.us/403", String.class, "1234");
    }

}
