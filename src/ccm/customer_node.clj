(ns ccm.customer_node
  (:require [ccm.core :as c]
            [ccm.utils :as u]
            [environ.core :refer [env]]))

(def customers (ref {}))

(def all-vertices (ref (c/read-graph-file (env :graph-file))))

(def graph-edges
  (c/get-edges @all-vertices))

(defn choose-score [new-score old-score]
  (cond
    (nil? old-score) new-score
    (< new-score old-score) new-score
    :else
    old-score))

(defn build-all-customer [graph]
  (into {}
    (let [shortest-paths (c/dijkstra-all-paths graph)]
      (reduce (fn [m [k v]]
                (let [score (c/score-for-node (:shortest-paths v))]
                  (if (= (:fraudulent ((first [k]) @customers)) true)
                    (update-in m [k] assoc :fraudulent true :score (choose-score score (:score ((first [k]) @customers))))
                    (update-in m [k] assoc :fraudulent false :score (choose-score score (:score ((first [k]) @customers)))))))
        shortest-paths shortest-paths))))


(defn customers-nodes [edges]
  (into {}
    (sort-by (comp :score second) <
      (build-all-customer edges))))


(defn add-edges [new-edge]
  (dosync
    (let [vertices-updated (ref-set all-vertices (conj @all-vertices new-edge))]
      (let [customers-updated (customers-nodes (c/get-edges vertices-updated))]
        (ref-set customers customers-updated)))))

(defn set-as-fraudulent [id]
  (let [customers-updated 
        (map (fn [customer]
               (let [cust (apply hash-map customer)]
                 (cond
                   (= id (first (keys cust)))  (update-in cust [id] assoc :score 0, :fraudulent true)
                   (= ((:shortest-paths (first (vals cust))) id) 1)
                   (update-in cust [(first (keys cust))] update-in [:score] * 0.5)
                   (contains? (:shortest-paths (first (vals cust))) id)
                   (update-in cust [(first (keys cust))]
                     update-in [:score] * (- 1 (u/exp (/ 1 2) ((:shortest-paths (first (vals cust))) id))))
                   :else
                   cust)))
          @customers)]
    (dosync
      (ref-set customers (into {} customers-updated)))))

(dosync
  (ref-set customers (customers-nodes graph-edges)))
