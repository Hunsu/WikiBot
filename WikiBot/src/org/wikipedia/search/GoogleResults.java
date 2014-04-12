package org.wikipedia.search;

import java.util.ArrayList;

public class GoogleResults {

	public static class Result {
        private String url;
        private String title;
        public String getUrl() {
        	return url;
        }
        public String getTitle() {
        	return title;
        }
        public void setUrl(String url) {
        	this.url = url;
        }
        public void setTitle(String title) {
        	this.title = title;
        }
        public String toString() {
        	return "Result[url:" + url +",title:" + title + "]";
        }
    }

    private ResponseData responseData;
    public ResponseData getResponseData() {
    	return responseData;
    }
    public void setResponseData(ResponseData responseData) {
    	this.responseData = responseData;
    }
    public String toString() {
    	return "ResponseData[" + responseData + "]";
    }

    public static class ResponseData {
        private ArrayList<Result> results;


        public ArrayList<Result> getResults() {
        	return results;
        }


        public void setResults(ArrayList<Result> results) {
        	this.results = results;
        }
        public String toString() {
        	return "Results[" + results + "]";
        }
    }



}
