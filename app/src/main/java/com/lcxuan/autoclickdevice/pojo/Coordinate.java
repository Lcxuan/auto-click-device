package com.lcxuan.autoclickdevice.pojo;

import java.io.Serializable;

public class Coordinate implements Serializable {

    public static final long serialVersionUID = 1122221562L;

    private Integer id;
    private Integer x;
    private Integer y;


    public Coordinate() {
    }

    public Coordinate(Integer id, Integer x, Integer y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
