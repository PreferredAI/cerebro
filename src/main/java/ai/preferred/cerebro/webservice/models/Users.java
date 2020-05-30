package ai.preferred.cerebro.webservice.models;

import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class Users {
    @Id
    public String _id;
    public List<Double> vec;
}
