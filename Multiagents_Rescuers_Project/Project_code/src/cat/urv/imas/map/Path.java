/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Arkard
 */
public class Path {
    
    private Queue<Point> path; // List of points
    private Point start, end; // Start and ending of the path
    private int[][][] closestNodes; // Closest nodes to start and ending points
    private int distance; // Total distance of the path
    
    private int[] auxIdx;
    
    public Path(Point start, Point end){
        path = new LinkedList<>();
        this.start = start;
        this.end = end;
    }
    
    public Point getStart(){
        return start;
    }
    
    public Point getEnd(){
        return end;
    }
    
    public void setPath(Queue<Point> path){
        this.path = path;
    }
    
    public Queue<Point> getPath(){
        return path;
    }
    
    public void setClosestNodes(int[][][] closestNodes){
        this.closestNodes = closestNodes;
    }
    
    public int[][][] getClosestNodes(){
        return closestNodes;
    }

    
    public void setDistance(int dist){
        this.distance = dist;
    }
    
    public int getDistance(){
        return distance;
    }
    
    public void setAuxIdx(int[] idx){
        this.auxIdx = idx;
    }
    
    public int[] getAuxIdx(){
        return auxIdx;
    }
    
}
