function segments = extractSegments(map,C)
% Extrae los segmentos de paths del mapa y los devuelve en un array de
% structs con la siguiente forma:
%       - points: matriz Nx2 con las coordenadas de los N puntos que forman
%       el segmento
%       - length: vector con la distancia desde el origen del segmento a
%       cada punto del mismo (suma acumulativa)

% Definimos las 4 direcciones posibles
directions =[1 0;
    0, 1;
    -1, 0;
    0, -1];


% Inicializamos el contador de segmentos
nSegments = 0;

for i = 1:size(C.corners,1)
    % Los segmentos comienzan en los nodos, realizamos el proceso para cada
    % nodo
    if C.isNode(i)
        % Dado un nodo, comprobamos si hay camino en las 4 direcciones
        for m = 1:size(directions,1)
            % Inicializamos el segmento con el nodo como primer punto
            segment.length = [];
            segment.points(1,:) = C.corners(i,:);
            done = false; % Booleano que nos indicara cuando ha finalizado el segmento
            n = 2; % Contador de puntos del segmento (empieza en 2 porque añadimos el primero manualmente)
            dir = directions(m,:); % Escogemos la primera direccion
            p = segment.points(end,:) + dir;
            % Comprobamos que existe camino en la direccion escogida
            if isPath(map,p(1),p(2));
                % Comenzamos a montar el segmento
                while ~done
                    if isPath(map,p(1),p(2))
                        % Obtenemos el punto mas cercano en la direccion
                        % escogida y la distancia al mismo
                        [idx,segment.length(n - 1)]= getNextCornerIndex(segment.points(end,:),C.corners,dir);
                        segment.points(n,:) = C.corners(idx,:); % Añadimos dicho punto
                        n = n + 1;
                        if C.isNode(idx) % Si el punto añadido es un nodo implica fin de segmento
                            % Debido a la naturaleza del algoritmo, los
                            % segmentos seran considerados dos veces en
                            % sentidos opuestos, debemos descartarlos en
                            % caso de ya existir en el output
                            if nSegments ~= 0
                                if isNewSegment(segment,segments)
                                    segment.length = cumsum(segment.length);
                                    segments(nSegments + 1) = segment;
                                    nSegments = nSegments + 1;
                                end
                            else
                                segment.length = cumsum(segment.length);                
                                segments(1) = segment;
                                nSegments = nSegments + 1;
                            end
                            clear segment
                            done = true; % Segmento finalizado (sea o no descartado)
                        % Si no se trata de un nodo, debemos seguir la
                        % búsqueda de puntos
                        else
                            % Rotamos la direccion 90 grados. Dado que
                            % todos los puntos son esquinas, no habra
                            % camino en la misma direccion de nuevo
                            dir = [dir(2) dir(1)];
                            % Calculamos el primer punto en esa direccion
                            % para poder comprobar si hay camino
                            p = segment.points(end,:) + dir;
                        end
                    else
                        % Si nos encontramos en una esquina y no hay camino
                        % en la direccion escogida, el camino estara por
                        % fuerza en la direccion opuesta, dado que al
                        % llegar a la esquina realizamos un cambio de
                        % direccion de 90 grados
                        if size(segment.points,1) ~= 1
                            dir = -dir;
                            p = segment.points(end,:) + dir;              
                        end
                    end
                end
            end
        end
    end
end
end

function [idx, minD] = getNextCornerIndex(p,C,dir)
% Esta funcion devuelve el indice que corresponde dentro de la matriz de
% esquinas (C.corners) de la siguiente esquina dado un punto p y una
% direccion dir. Tambien devuelve la distancia a dicha esquina

% Inicializamos la distancia minD en el maximo valor posible (tamaño map)
minD = 20;
% Comprobamos para cada punto si se encuentra en la direccion escogida
for k = 1:size(C,1)
    d = C(k,:) - p;
    D = sum(d.*dir);
    % Comprobamos que el resultado de producto escalar es 1
    if D == norm(d,1)
        % Comprobamos si el punto esta mas cerca que algun otro punto
        % encontrado previamente. Tambien debe ser mayor que 0 para que no
        % considere el punto evaluado como el mas cercano
        if D < minD && D>0
            minD = D;
            idx = k;
        end
    end
end
end

function TF = isNewSegment(segment,s)
% Devuelve un booleano, true si el segmento no se encuentra dentro del
% array de segmentos s. False en caso contrario.
TF = true; % Por defecto es un nuevo segmento
for h = 1:length(s)
    if size(s(h).points) == size(segment.points)
        if s(h).points == flipud(segment.points)
            % En caso de hallar un segmento equivalente, debera devolver
            % false
            TF = false;
        end
    end
end
end