package com.remind.scanremind;

import java.io.Serializable;

public class BarcodeData implements Serializable {
    private long itemNum; //고유값(기본키 처럼) -> 생성 시점의 시간을 받아와서 넣자
    private String number; //바코드 숫자
    private String name; //이름
    private String imageSrc; //사진
    private String regDate; //등록일
    private String dday; //유통기한

    public BarcodeData() {
        this(-1, null, null, null, null, null);
    }

//    public BarcodeData(String number, String name, String imageSrc, String regDate, String dday) {
//        this.number = number;
//        this.name = name;
//        this.imageSrc = imageSrc;
//        this.regDate = regDate;
//        this.dday = dday;
//    }
    public BarcodeData(long itemNum, String number, String name, String imageSrc, String regDate, String dday) {
        this.itemNum = itemNum;
        this.number = number;
        this.name = name;
        this.imageSrc = imageSrc;
        this.regDate = regDate;
        this.dday = dday;
    }

    public long getItemNum() {
        return itemNum;
    }

    public void setItemNum(long itemNum) {
        this.itemNum = itemNum;
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

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
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
                "itemNum=" + itemNum +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", imageSrc='" + imageSrc + '\'' +
                ", regDate='" + regDate + '\'' +
                ", dday='" + dday + '\'' +
                '}';
    }
}
