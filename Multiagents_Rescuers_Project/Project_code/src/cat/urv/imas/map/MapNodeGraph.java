/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import java.awt.Point;
import java.util.ArrayList;
import static java.util.Collections.min;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Arkard
 */
public class MapNodeGraph {
    
    // Los 'Nodes' son las esquinas de los paths del mapa
    private List<Point> Nodes;
    // Matriz 'dist' representa la distancia mínima entre dos nodos (índice correspondiente al nodo en 'Nodes')
    private int[][] dist;
    // El elemento i,j de la matriz 'next' indica el nodo por el que hay que ir para llegar de 'i' a 'j' de forma óptima
    private int[][] next;
    // Mapa de la montaña
    private Cell[][] map;
    
    // Constructor, a partir del mapa de la montaña
    public MapNodeGraph(Cell[][] map){
        this.map = map;
        initialize();        
    }
    
    // Inicializa el grafo
    private void initialize(){
        // Obtenemos los nodos del grafo
        getMapNodes();
        
        // Declaramos las direcciones posibles en el mapa 2D para buscar las conexiones entre nodos
        int[][] dir = new int[][]{
            {1,0},
            {0,1},
            {-1,0},
            {0,-1}
        };            
            
        int N = Nodes.size(); // Número de nodos
        dist = new int[N][N];
        next = new int[N][N];
        for(int i = 0; i<N; i++){
            for(int j = 0; j<N; j++){
                dist[i][j] = 100000; // Inicializamos las distancias en un valor elevado
            }
        }
        // Para cada nodo...
        for(int i = 0; i<N; i++){
            dist[i][i] = 0; // Distancia a sí mismo es 0
            for(int d = 0; d<4; d++){ // Buscamos en la 4 direcciones el siguiente nodo
                // Si la casilla siguiente en la dirección de búsqueda es path, en esa direccion hay un nodo
                if(isPath(Nodes.get(i).x + dir[d][0],Nodes.get(i).y + dir[d][1]) == 1){
                    int[] edge = getNextNode(Nodes.get(i),dir[d]); // Obtiene índice y distancia al siguiente nodo
                    dist[i][edge[0]] = edge[1]; // Almacenamos la distancia ('peso' en el grafo)
                    next[i][edge[0]] = edge[0]; // Almacenamos el índice
                }
            }
        }
        // Computamos las distancias mínimas entre nodos que no están directamente conectados
        for(int k = 0; k<N; k++){
            for(int i = 0; i<N; i++){
                for(int j = 0; j<N; j++){
                    if(dist[i][j] > dist[i][k] + dist[k][j]){
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
    }
    
    // Obtiene los nodos del grafo
    private void getMapNodes(){
        Nodes = new ArrayList<>();
        for(int i = 0; i<20; i++){
            for(int j = 0; j<20; j++){
                if(isCorner(i,j)){ // Las esquinas de mapa son los nodos
                    Nodes.add(new Point(i,j));
                }
            }
        }
    }
    
    // Busca el siguiente nodo (esquina) en la dirección especificada a partir de nodo anterior
    private int[] getNextNode(Point node, int[] dir){
        double w = Double.POSITIVE_INFINITY; // Valor elevado para la distancia (el siguiente nodo será el más cerano, minimizamos la 'w')
        int idx = 0; // Índice del siguiente nodo (0 por defecto)
        for(int i = 0; i<Nodes.size(); i++){ // Para cada nodo...
            // Vector distancia entre nodos
            int[] d = new int[]{Nodes.get(i).x - node.x,Nodes.get(i).y - node.y};
            // Proyección del vector en la dirección evaluada
            int D = d[0]*dir[0] + d[1]*dir[1];
            // Si la proyección coincide con la norma L1, es un posible candidato (misma dirección)
            if(D == Math.sqrt(d[0]*d[0] + d[1]*d[1])){
                // Si el candidato es más cercano que el anterior lo actualizamos
                if(D < w && D > 0){
                    w = (double)D;
                    idx = i;
                }
            }
        }
        // Devolvemos el índice y la distancia al siguiente nodo
        return new int[]{idx,(int)w};
    }
    
    // Devuelve un booleano indicando si la casilla evaluada es una esquina de un path
    private boolean isCorner(int i, int j){
        // Primero comprobamos que la casilla realmente es path
        if(isPath(i,j) == 1){
            // Contamos las casillas paths que hay justo encima y debajo
            int V = isPath(i + 1,j) + isPath(i - 1,j);
            // Contamos las casillas paths que hay justo a la izq y derecha
            int H = isPath(i,j + 1) + isPath(i,j - 1);
            // Esta expresión incluye las condiciones que ha de reunir una esquina
            return (V*H != 0)||(V + H == 1);
        }else{
            return false;
        }
    }
    
    // Devuelve '1' si la casilla evaluada es path y '0' en caso contrario
    private int isPath(int i, int j){
        int r = 0; // Por defecto el resultado es 0
        // Comprobamos que las coordenadas caen dentro del tablero (20x20)
        if(i >= 0 && i < 20 && j >= 0 && j < 20){
            if(i == 5 && j == 13)
                i = 5;
            // Comprobamos is el tipo de celda coincide con CellType.PATH
            if(map[i][j].getCellType() == CellType.PATH || map[i][j].getCellType() == CellType.AVALANCHE){
                r = 1;
            }
        }
        return r;
    }
    
    // Dado una casilla path, devuelve los índices de los nodos más cercanos y
    // La distancia a ellos en forma de matriz 2x2, donde la primera fila indica
    // Los indices y la segunda las distancias. En caso de que el punto evaluado
    // Se encuentre sobre un nodo, devolvera una matriz del tipo {{idx,-1},{0,0}}
    private int[][] closestNodes(Point p){
        // Comprobamos si es un nodo
        if(isCorner(p.x,p.y)){
            int idx = Nodes.indexOf(p);
            return new int[][]{
                {idx,idx},
                {0,0}
            };
        }else{
            // Comprobamos si el camino en el que estamos es vertical u horizontal
            // ya que NO es una esquina
            int V = isPath(p.x + 1,p.y) + isPath(p.x - 1,p.y);
            // Para hallar los nodos mas cercanos buscamos el de distancia minima
            // Por eso inicializamos 'posD' y 'negD' con valores elevados
            // Una esquina estara en sentido positivo y la otra en el otro sentido,
            // Por eso debemos separar en distancia positiva y distancia negativa
            int posD = 1000;
            int negD = -posD;
            Point node1, node2; // En estas variables guardaremos los nodos encontrados
            if(V != 0){
                // Si el camino es vertical, buscamos nodos con la misma coordenada horizontal
                for(int n = 0; n<Nodes.size(); n++){
                    if(Nodes.get(n).y == p.y){
                        // Por cada nodo en la misma linea vertical, medimos la distancia
                        // Si es positiva y menor que la anterior encontrada, actualizamos su valor
                        // Lo mismo si es negativa
                        int d = Nodes.get(n).x - p.x;
                        if(posD > d && d > 0){
                            posD = d;
                        }else if(negD < d && d <0){
                            negD = d;
                        }
                    }
                }
                // Reconstruimos los nodos sumando las distancias (pos/neg) mínimas encontradas
                node1 = new Point(posD + p.x,p.y);
                node2 = new Point(negD + p.x,p.y);
            }else{ // Mismo procedimiento si el camino es horizontal pero cambiando las coordenadas a comparar
                for(int n = 0; n<Nodes.size(); n++){
                    if(Nodes.get(n).x == p.x){
                        int d = Nodes.get(n).y - p.y;
                        if(posD > d && d > 0){
                            posD = d;
                        }else if(negD < d && d <0){
                            negD = d;
                        }
                    }
                }
                node1 = new Point(p.x,p.y + posD);
                node2 = new Point(p.x,p.y + negD);
            }
            // Devolvemos los índices de los nodos encontrados
            // y las distancias a los mismos
            return new int[][]{
                {Nodes.indexOf(node1),Nodes.indexOf(node2)},
                {posD,-negD}
            };
        }       
    }
    
    // Devuelve la ruta óptima entre dos nodos, los inputs son los índices de estos nodos
    private List<Point> nodePath(int node1, int node2){
        List<Point> path = new ArrayList<>();
        path.add(Nodes.get(node1));
        while(node1 != node2){
            node1 = next[node1][node2]; // La matriz next indica a que nodo debemos movernos
            path.add(Nodes.get(node1));
        }
        return path;
    }
    
    public void minDistance(Path p){
       int[][][] n = new int[2][2][2];
       n[0] = closestNodes(p.getStart());
       n[1] = closestNodes(p.getEnd());
       p.setClosestNodes(n);
        if(n[0][0][0] == n[1][0][0] && n[0][0][1] == n[1][0][1]){
           int dx = p.getStart().x - p.getEnd().x;
           int dy = p.getStart().y - p.getEnd().y;
           p.setDistance((int)(Math.abs(dx) + Math.abs(dy)));
       } else if (n[0][0][0] == n[1][0][1] && n[0][0][1] == n[1][0][0]){
           int dx = p.getStart().x - p.getEnd().x;
           int dy = p.getStart().y - p.getEnd().y;
           p.setDistance((int)(Math.abs(dx) + Math.abs(dy)));
       }else {
           int distance = 1000;
           p.setDistance(distance);
           for(int i = 0; i<2; i++){
               for(int j = 0; j<2; j++){
                   distance = n[0][1][i] + dist[n[0][0][i]][n[1][0][j]] + n[1][1][j];
                   if(distance < p.getDistance()){
                       p.setDistance(distance);
                       p.setAuxIdx(new int[]{i,j});
                   }
               }
           }
       }    
    }    
    // Devuelve la ruta óptima entre dos puntos cualesquiera del mapa (que sean path)
    // El output es una cola de Point
    public void optimalRoute(Path p){
        if(p.getAuxIdx() == null)
            minDistance(p);
        // Inicializamos variables
        Queue<Point> path = new LinkedList<>();

        int[][][] n = p.getClosestNodes();
        if(n[0][0][0] == n[1][0][0] && n[0][0][1] == n[1][0][1]){
            path.add(p.getStart());
            path.add(p.getEnd());
        }else if(n[0][0][0] == n[1][0][1] && n[0][0][1] == n[1][0][0]){
            path.add(p.getStart());
            path.add(p.getEnd());
        }else {           
            int[] idx = p.getAuxIdx();
            // Reconstruimos el path
            if(n[0][0][0] != n[0][0][1])
                path.add(new Point(p.getStart()));
            path.addAll(nodePath(n[0][0][idx[0]],n[1][0][idx[1]]));
            if(n[1][0][0] != n[1][0][1])
                path.add(new Point(p.getEnd()));
        }
        p.setPath(path);   
    }
}
