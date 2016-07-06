/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import jade.core.AID;
import java.awt.Point;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author maria
 */
public class InjuredPerson implements Comparable<InjuredPerson>, Serializable {

    private int stepToDie;
    private boolean severelyInjured;
    private Point location;
    private boolean isasignedRescuer;
    private AID asignedRescuer;
    private int stepAppeared;

    public InjuredPerson(int stepToDie, boolean severelyInjured, Point location, int stepAppeared) {
        this.stepToDie = stepToDie;
        this.severelyInjured = severelyInjured;
        this.location = location;
        this.isasignedRescuer = false;
        this.stepAppeared=stepAppeared;
    }

    public int getStepToDie() {
        return stepToDie;
    }

    public Point getLocation() {
        return location;
    }

    public boolean isSeverelyInjured() {
        return severelyInjured;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void setSeverelyInjured(boolean severelyInjured) {
        this.severelyInjured = severelyInjured;
    }

    public void setStepToDie(int stepToDie) {
        this.stepToDie = stepToDie;
    }

    @Override
    public int compareTo(InjuredPerson o) {
        return (stepToDie > o.getStepToDie() ? 1 : stepToDie < o.getStepToDie() ? -1 : 0);
    }

    public boolean isAsignedRescuer() {
        return isasignedRescuer;
    }

    public void setAsignedRescuer(AID asignedRescuer) {
        this.asignedRescuer = asignedRescuer;
    }

    public AID getAsignedRescuer() {
        return asignedRescuer;
    }

    public void setIsAsignedRescuer(boolean b) {
        this.isasignedRescuer = b;

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.stepToDie;
        hash = 59 * hash + (this.severelyInjured ? 1 : 0);
        hash = 59 * hash + Objects.hashCode(this.location);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InjuredPerson other = (InjuredPerson) obj;
        if (this.stepToDie != other.stepToDie) {
            return false;
        }
        if (this.severelyInjured != other.severelyInjured) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return true;
    }

    public int getStepAppeared() {
        return stepAppeared;
    }

    

}
