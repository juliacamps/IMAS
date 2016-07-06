/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.HelicopterAgent;
import cat.urv.imas.map.InjuredPerson;
import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author axel
 */
public class Proposal implements Serializable, Comparable<Proposal>{
    private double d1;
    private double d2;
    //private HelicopterAgent ha; //AID
    private AID aid;
    private InjuredPerson ip;
    private byte type;
    public static final byte HELICOPTER_AGENT_TYPE = 1;
    public static final byte RURAL_AGENT_TYPE = 2;
    
    public Proposal(double d1, double d2, byte type) {
        this.d1 = d1;
        this.d2 = d2;
        this.type = (type == RURAL_AGENT_TYPE)? RURAL_AGENT_TYPE:HELICOPTER_AGENT_TYPE;
        this.aid = null;
        this.ip = null;
    }
    
    public Proposal(double d1, byte type) {
        this.d1 = d1;
        this.d2 = Double.POSITIVE_INFINITY;
        this.type = (type == RURAL_AGENT_TYPE)? RURAL_AGENT_TYPE:HELICOPTER_AGENT_TYPE;; //by deault is a helicopter agent type
        this.aid = null;
        this.ip = null;
    }

    public double getD1() {
        return d1;
    }

    public double getD2() {
        return d2;
    }

    public AID getAid() {
        return aid;
    }

    public InjuredPerson getIp() {
        return ip;
    }

    public byte getType() {
        return type;
    }

    public void setD1(double d1) {
        this.d1 = d1;
    }

    public void setD2(double d2) {
        this.d2 = d2;
    }

    public void setAid(AID aid) {
        this.aid = aid;
    }

    public void setIp(InjuredPerson ip) {
        this.ip = ip;
    }

    public void setType(byte type) {
        this.type = (type == RURAL_AGENT_TYPE)? RURAL_AGENT_TYPE:HELICOPTER_AGENT_TYPE;
    }
    
    public int getLength() {
        if (d2 == Double.POSITIVE_INFINITY) {
            return 1;
        }else{
            return 2;
        }
    }

    /*
    a negative int if this < o
    0 if this == o
    a positive int if this > o
    
    cost function: 20 x 
    */
    @Override
    public int compareTo(Proposal o) {
        if (this.type == o.type) {
            if (this.d2 > o.d2 || this.d1 > o.d1) {
                return 1;
            } else if (this.d2 == o.d2 && this.d1 == o.d1) {
                return 0;
            } else {
                return -1;
            }
        } else if (this.type == RURAL_AGENT_TYPE) {          
            return (this.d1<20)? -1:1;
        } else {
            // this.type == HELICOPTER_AGENT_TYPE case
            return (o.d1>20)? -1:1;
        }
    }

    
}
