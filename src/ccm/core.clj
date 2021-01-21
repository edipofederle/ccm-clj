(ns ccm.core
  (:require
    [clojure.string :as str]
    [clojure.data.csv :as csv]))

(def infinity Double/POSITIVE_INFINITY)


(defn get-unvisited-nodes [graph-costs initial-node]
  (disj
    (apply sorted-set (keys graph-costs))
      initial-node))


(defn get-next-vertice [costs unvisited]
  "Get the next vertice to visit considering
  the current costs. The next vertice should
  be the least on costs considering unvisited nodes"
  (apply min-key costs unvisited))


(defn get-costs [graph initial-node]
  "Given a graph and a initial-node
   returns initial costs.

   Dijkstra algorithm step 2:
    ``Assign to every node a tentative distance value:
       set it to zero for our initial node and to infinity for all other nodes.``"
  (assoc
    (zipmap (keys graph) (repeat infinity))
    initial-node 0))


(defn has-more-nodes-to-visit? [unvisited]
  (empty? unvisited))


(defn get-neighbors [graph for-node]
  (graph for-node))


(defn update-costs-from-node [graph costs unvisited-nodes from-node]
  "This function get neighbors of a node (from-node)
  and update their costs (distance) only if the node
  yet not been visited.

  Dijkstra's algorithm step 4:
  ``For the current node, consider all of its unvisited neighbors±
    and calculate their tentative distances. Compare the newly
    calculated tentative distance to the current assigned
    value and assign the smaller one``

  Returns the graph costs updated."
  (let [current-cost-from-node (get costs from-node)]
    (reduce-kv
      (fn [current-costs nbr nbr-cost]
        (if (unvisited-nodes nbr)
          (do
            (update-in current-costs [nbr] min (+ current-cost-from-node nbr-cost)))
          current-costs))
      costs (get-neighbors graph from-node))))


(defn dijkstra [graph initial-node]
  (loop [costs (get-costs graph initial-node)
         current-node initial-node
         unvisited (get-unvisited-nodes costs initial-node)]

    (if (has-more-nodes-to-visit? unvisited)
        (assoc-in {} [initial-node] {:shortest-paths costs} ) ;; stopping point, all nodes visited.

      (let [next-costs        (update-costs-from-node graph costs unvisited current-node) ;; current-node is next-vertice
            next-vertice      (get-next-vertice next-costs unvisited)
            unvisited-updated (disj unvisited next-vertice)]  ;; remove vertex being visited from unvisited set

        (recur next-costs next-vertice unvisited-updated))))) ;; recursively


(defn dijkstra-all-paths [graph]
  "This Function receive a graph and compute the shortest-path
  for all nodes"
  (reduce-kv (fn [m k _] (merge m (dijkstra graph k) )) {} graph))


(defn score-for-node [paths-for-node]
  "This function receive a map with nodes and their costs and
  returns the score.

  e.g: given a map with nodes and costs: {:1 0, :2 1, :3 2, :4 3, :5 2}
  The score is given by the average of number of nodes (least 1) by the sum of all costs"
  (/ (- (count paths-for-node) 1) (reduce + (vals paths-for-node))))


(defn read-graph-file [path-file]
  "This function receive a path to the edges file and return a data-structure like:
  [[:1 :2] [:2 :3] [:3 :4]] where each vector representing a edge."
  (with-open [in-file (clojure.java.io/reader path-file)]
    (let [file-graph
      (doall
        (csv/read-csv in-file  :separator \space))]
        (into []
          (map
            (fn [edge] (into [] (map keyword edge )))
           file-graph)))))


(defn get-edges [vertices]
  "Build the graph edges given the vertices.
  e.g: vertices = [[:1 :2] [:2 :3] [:3 :4]]
      graph: {:1 {:2 1}, :2 {:3 1}, :3 {:4 1}}"
  (let [verts (into vertices (map reverse vertices)) ]
    (reduce
      (fn [m [k v]]
        (assoc-in m [k v] 1))
      {}
      verts)))
