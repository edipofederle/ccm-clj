(ns ccm.customer-node-test
  (:use midje.sweet)
  (:require [ccm.core :as c]
            [ccm.customer_node :refer :all]))

(def g {:1 {:2 1}
        :2 {:1 1 :3 1}
        :3 {:2 1}})


(facts "about `build-all-customer`"
  (fact "all the nodes (customer) should start as non fraudulent"
    (let [customers-nodes (build-all-customer g)]
      (:fraudulent (:1 customers-nodes)) => false
      (:fraudulent (:2 customers-nodes)) => false
      (:fraudulent (:3 customers-nodes)) => false))

  (fact "score should be initialized"
    (dosync (ref-set customers {}))
    (let [customers-nodes (build-all-customer g)]
      (:score (:1 customers-nodes)) => 2/3
      (:score (:2 customers-nodes)) => 1
      (:score (:3 customers-nodes)) => 2/3)))

(facts "about `add-egdes`"
  (fact "should add new edge"
    (dosync (ref-set customers {}))
    (add-edges [:2 :4])
    (add-edges [:1 :5])
    (add-edges [:5 :6])
    (let [result (add-edges [:1 :4])]
      (:4 (:shortest-paths (:2 result))) => 1
      (:4 (:shortest-paths (:1 result))) => 1
      (:1 (:shortest-paths (:5 result))) => 1
      (:1 (:shortest-paths (:6 result))) => 2
      (:6 (:shortest-paths (:4 result))) => 3)))

(facts "about `set-as-fraudulent`"
  (dosync (ref-set customers {}))
  (fact "score should be zero and fraudulent should be true"
    (with-redefs [customers (ref (customers-nodes graph-edges))]
      (set-as-fraudulent :1)
      (let [{fraudulent :fraudulent score :score} (:1 @customers)]
        fraudulent => true
        score => 0
        (:score (:3 @customers)) => 9/16
        (:fraudulent (:3 @customers)) => false
        (:score (:4 @customers)) => 7/16
        (:fraudulent (:4 @customers)) => false
        ))))



