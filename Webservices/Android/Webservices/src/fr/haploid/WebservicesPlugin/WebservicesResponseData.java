package fr.haploid.WebservicesPlugin;

/**
 * Created by sebastienfamel on 06/11/14.
 */
public class WebservicesResponseData {
    private String responseData;
    private int statusCode;
    private boolean success;

    public WebservicesResponseData(String responseData, int statusCode, boolean success) {
        super();
        this.responseData = responseData;
        this.statusCode = statusCode;
        this.success = success;

    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public String getResponseData() {

        return responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
