(ns ccm.api-routes-test
  (:use liberator.core
        cheshire.core
        [liberator.representation :only (ring-response)]
        midje.sweet
        [compojure.core :only [ANY]]
        [ring.mock.request :only [request header]])
  (:require
   [ccm.api-routes :refer :all]
   [ccm.checkers :refer :all]
   [ccm.customer_node :refer :all]))


(facts "about customers"
  (dosync (ref-set customers {}))
  
  (with-redefs [customers (ref (customers-nodes graph-edges))]
    (fact "should return a json with all customers"
      (let [handler (ANY "/api" [] customers-list)
            response (handler (request :get "/api"))]
        response => OK
        response => (content-type "application/json;charset=UTF-8")
        (:status response) => 200
        (parse-string (:body response)) => (parse-stream (clojure.java.io/reader "resources/json-customer.json")))))

  (fact "should return a single customer"
    (with-redefs [customers (ref (customers-nodes graph-edges))]
      (let [handler (ANY ["/api/customer/:id{[0-9]+}"] [id] (single-customer id))
            response (handler (request :get "/api/customer/1"))]
        response => OK
        response => (content-type "application/json;charset=UTF-8")
        (:status response) => 200
        (parse-string (:body response)) => (parse-stream (clojure.java.io/reader "resources/single-customer.json"))))))
