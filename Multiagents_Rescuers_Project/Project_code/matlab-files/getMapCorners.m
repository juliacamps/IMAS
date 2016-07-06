function C = getMapCorners(map)
% Esta funcion devuelve un struct C donde se almacenan las esquinas del
% mapa con el siguiente formato:
%       - corners: Matriz Nx2 con las coordenadas de las N esquinas
%       - isNode: vector booleano de N elementos indicando si se trata de
%       un nodo (bifurcacion)

% Aplicamos la funcion isCorner para cada casilla path
n = 1;
for i = 1:20
    for j = 1:20
        if isPath(map,i,j)
            R = isCorner(map,i,j); % Obtenemos el tipo de path
            if R % Entrara en este 'if' cuando R>0, descarta no esquinas (no relevantes)
                C.corners(n,:) = [i j];
                C.isNode(n) = R==2; % Se trata de un nodo si R es 2
                n = n + 1;
            end            
        end
    end
end
end


function R = isCorner(map,i,j)
% Esta funcion devuelve un entero distinto en funcion del tipo de casilla
% path en la coordenada i, j:
%   - 0: no esquina
%   - 1: esquina
%   - 2: nodo (bifurcacion o final de camino)

% Comprobamos si hay path arriba y/o abajo
V = isPath(map,i-1,j) + isPath(map,i+1,j);
% Comprobamos si hay path a izquierda y/o derecha
H = isPath(map,i,j-1) + isPath(map,i,j+1);

% Contamos el numero de caminos que parten desde el punto
R = V+H;
if R == 2 % Si hay 2 caminos puede tratarse de una recta o una esquina
    R = V*H; % Este producto sera 0 para las rectas y 1 para las esquinas     
else % El número de caminos es 1, 3 o 4, indicando una bifurcación o fin de camino (nodo)
    R = 2;
end
end