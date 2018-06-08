package com.wtgroup.ohm.example.entity;

import com.wtgroup.ohm.annotation.Column;
import com.wtgroup.ohm.annotation.HEntity;
import com.wtgroup.ohm.annotation.RowKey;

/**
 * 用户交易事件
 *
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-22-23:49
 */
@HEntity(table = "lh_test2",defaultFamily = "cf")
public class CustomerEvent {

    //row key
    @RowKey
    private String certNo;

    //column family: event(事件)
    //totest 不加注解, 预期: 不会映射
    private String name;
    @Column(family = "cf")          //totest 不指定name, 预期: 默认用字段名
    private String event;
    @Column(name="event_type")      //数据库字段习惯是下划线风格, 所以需要制定. totest 默认采用类上的defaultFamily
    private Integer eventType;      // 1: 注册  2: 登录  3: 绑卡  4: 交易
    @Column     //totest 缺省字段名, 和defaultFamily
    private String content;

    //column family: action_stat(行为统计数据)
//    @Column(name = "login_count",family="action_stat")
    private Long loginCount;        //登录次数统计
//    @Column(name = "max_online",family = "action_stat")
    private Long maxOnline;         //最大在线时长

    @RowKey
    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
