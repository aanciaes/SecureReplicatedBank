package rest.sgx.model;

import java.io.Serializable;
import java.util.List;

public class GetBetweenResponse implements Serializable {

    private List<String> results;

    public GetBetweenResponse() {
    }

    public GetBetweenResponse(List<String> results) {
        this.results = results;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }
}
