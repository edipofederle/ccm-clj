(ns ccm.core-test
  (:use midje.sweet)
  (:require [ccm.core :refer :all]))

(def g {:1 {:2 1}
        :2 {:1 1 :3 1 :5 1}
        :3 {:2 1 :4 1}
        :4 {:3 1 :5 1}
        :5 {:4 1}})

(facts "about `get-unvisited-nodes`"
  (fact "return all nodes excluding :1"
    (get-unvisited-nodes g :1) => #{:2 :3 :4 :5})
  (fact "return all nodes excluding :5"
    (get-unvisited-nodes g :5) => #{:1 :2 :3 :4}))

(facts "about `get-next-vertice`"
  (fact "vertice should be :3"
    (let [costs {:1 0 :2 infinity :3 infinity :4 infinity :5 infinity}
          unvisited #{:2 :3 :4 :5}]
      (get-next-vertice costs unvisited) => :3)))

(facts "about `get-costs`"
  (fact "should assoc zero to initial-node and inifite to rest"
      (get-costs g :1) => {:1 0
                           :2 infinity
                           :3 infinity
                           :4 infinity
                           :5 infinity}))

(facts "about `get-neighbors`"
  (fact "neighbors for node :3 should be :2 and :4"
    (get-neighbors g :3) => {:2 1 :4 1})
  (fact "neighbors for node :2 should be :1, :3 and :4"
    (get-neighbors g :2) => {:1 1 :3 1 :5 1}))


(facts "about `update-costs-from-node`"
  (fact "should update cost of node 2"
    (let [costs (get-costs g :1)
          unvisited (get-unvisited-nodes costs :1)]
      (update-costs-from-node g costs unvisited :1) => {:1 0
                                                        :2 1
                                                        :3 infinity
                                                        :4 infinity
                                                        :5 infinity}))

  (fact "should update cost of node 3 and 5"
    (let [costs {:1 0 :2 1 :3 infinity :4 infinity :5 infinity}
          unvisited #{:3 :4 :5}]
      (update-costs-from-node g costs unvisited :2) => {:1 0 :2 1 :3 2 :4 infinity :5 2})))


(facts "about `dijkstra`"
  (fact "shortest path from node n"
    (dijkstra g :1) => {:1 {:shortest-paths {:1 0 :2 1 :3 2 :4 3 :5 2}}}
    (dijkstra g :2) => {:2 {:shortest-paths {:1 1 :2 0 :3 1 :4 2 :5 1}}}
    (dijkstra g :3) => {:3 {:shortest-paths {:1 2 :2 1 :3 0 :4 1 :5 2}}}
    (dijkstra g :4) => {:4 {:shortest-paths {:1 3 :2 2 :3 1 :4 0 :5 1}}}
    (dijkstra g :5) => {:5 {:shortest-paths {:1 4 :2 3 :3 2 :4 1 :5 0}}}))


(facts "about `dijkstra-all-paths`"
  (fact "shortest-paths"
    (dijkstra-all-paths g) => {:1 {:shortest-paths {:1 0 :2 1 :3 2 :4 3 :5 2}}
                               :2 {:shortest-paths {:1 1 :2 0 :3 1 :4 2 :5 1}}
                               :3 {:shortest-paths {:1 2 :2 1 :3 0 :4 1 :5 2}}
                               :4 {:shortest-paths {:1 3 :2 2 :3 1 :4 0 :5 1}}
                               :5 {:shortest-paths {:1 4 :2 3 :3 2 :4 1 :5 0}}}))


(facts "about score-for-node"
  (fact "initial score for a node is the sum of all costs divide by number of nodes least 1"
    (let [paths-for-node-1 {:1 0 :2 1 :3 2 :4 3 :5 2}
          paths-for-node-2 {:1 1 :2 0 :3 1 :4 2 :5 1}
          paths-for-node-3 {:1 2 :2 1 :3 0 :4 1 :5 2}]
      (score-for-node paths-for-node-1) => 1/2
      (score-for-node paths-for-node-2) => 4/5
      (score-for-node paths-for-node-3) => 2/3)))


(facts "about `read-graph-file`"
  (fact "should read vertices and turn it into a vector of pairs keywords"
    (let [path-file "resources/graph-test.txt"]
      (read-graph-file path-file) => [[:1 :2] [:2 :3] [:3 :4]])))


(facts "about `get-edges`"
  (fact "should convert pair of vertices into a graph"
    (let [vertices-pairs [[:1 :2] [:2 :3] [:3 :4]]]
      (get-edges vertices-pairs) => {:1 {:2 1}
                                     :2 {:1 1 :3 1}
                                     :3 {:2 1 :4 1}
                                     :4 {:3 1}})))
