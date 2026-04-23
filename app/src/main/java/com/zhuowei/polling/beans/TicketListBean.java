package com.zhuowei.polling.beans;

import java.io.Serializable;
import java.util.List;

public class TicketListBean {


    private int total;
    private List<RowsDTO> rows;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<RowsDTO> getRows() {
        return rows;
    }

    public void setRows(List<RowsDTO> rows) {
        this.rows = rows;
    }

    public static class RowsDTO implements Serializable {
        private int id;
        private String surveyStatus;
        private String userNo;
        private String userName;
        private String areaCompany;
        private String delFlag;
        private String images;
        private String address;
        private String userAddress;
        private String buildLocation;
        private String buildLocationCoord;


        public String getBuildLocationCoord() {
            return buildLocationCoord;
        }

        public void setBuildLocationCoord(String buildLocationCoord) {
            this.buildLocationCoord = buildLocationCoord;
        }

        public String getImages() {
            return images;
        }


        public void setImages(String images) {
            this.images = images;
        }

        public String getUserAddress() {
            return userAddress;
        }

        public void setUserAddress(String userAddress) {
            this.userAddress = userAddress;
        }

        public String getBuildLocation() {
            return buildLocation;
        }

        public void setBuildLocation(String buildLocation) {
            this.buildLocation = buildLocation;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSurveyStatus() {
            return surveyStatus;
        }

        public void setSurveyStatus(String surveyStatus) {
            this.surveyStatus = surveyStatus;
        }

        public String getUserNo() {
            return userNo;
        }

        public void setUserNo(String userNo) {
            this.userNo = userNo;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getAreaCompany() {
            return areaCompany;
        }

        public void setAreaCompany(String areaCompany) {
            this.areaCompany = areaCompany;
        }

        public String getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(String delFlag) {
            this.delFlag = delFlag;
        }
    }
}
