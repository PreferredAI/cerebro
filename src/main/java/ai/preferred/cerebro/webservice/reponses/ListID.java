package ai.preferred.cerebro.webservice.reponses;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class ListID {
    List<String> ids;

    public ListID(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
