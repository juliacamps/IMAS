function TF = isPath(map, i, j)
% Comprueba si la casilla esta dentro de los límites del mapa
if i > 0 && i <= 20 && j>0 && j<= 20
    TF = map(i,j) == 1; % Si la casilla tiene un valor 1 indica que es path
else
    TF = false;
end
end