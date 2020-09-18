package com.remind.scanremind;

import java.sql.Date;

public class BarcodeData {
    private String number; //바코드 숫자
    private String name; //이름
    private int imageID; //사진
    private String regDate; //등록일
    private String dday; //유통기한

    public BarcodeData() {
        this(null, null, 0, null, null);
    }

    public BarcodeData(String number, String name, int imageID, String regDate, String dday) {
        this.number = number;
        this.name = name;
        this.imageID = imageID;
        this.regDate = regDate;
        this.dday = dday;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getDday() {
        return dday;
    }

    public void setDday(String dday) {
        this.dday = dday;
    }

    @Override
    public String toString() {
        return "BarcodeData{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", imageID=" + imageID +
                ", regDate=" + regDate +
                ", dday=" + dday +
                '}';
    }
}
