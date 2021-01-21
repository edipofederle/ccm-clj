(ns ccm.api-routes
  (:use compojure.core
        cheshire.core
        ring.util.response)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY context]]
            [ccm.core :as c]
            [ccm.customer_node :as cust]
            [ccm.utils :as u]
            [cheshire.core :refer :all]
            [halresource.resource :as hal]
            [clojurewerkz.route-one.core :refer :all]))


(defn customers-resource [base-url]
  (map
    (fn [c]
      (let [r
            (-> (hal/new-resource (str base-url "/customer/" (name (first c))))
              (hal/add-properties
                {
                 :customer (name (first c))
                 :score (:score (last c))
                 :fraudulent (:fraudulent (last c))
                 })
              (hal/add-link
                :rel "set-as-fraudulent"
                :href (u/fraudulent-url base-url (name (first c)))))]
        (parse-string (hal/resource->representation r :json))))
    @cust/customers))


(defresource edges
  :available-media-types ["application/json"]
  :allowed-methods [:post]
  :post! (fn [q]
           (let [{source "source" destination "destination"} (parse-string (slurp (:body (:request q))))]
             (cust/add-edges (vector (keyword source) (keyword destination)))))
  :handle-created (fn [q] (customers-resource (u/build-entry-url (q :request)))))


(defresource customers-list
  :available-media-types ["application/json"]
  :allowed-methods [:post :get]
  :handle-ok (fn [q]
               (customers-resource (u/build-entry-url (q :request)))))

(defresource single-customer [id]
  :allowed-methods [:get, :put]
  :exists? (fn [_]
             (let [e (get @cust/customers (keyword id))]
               (if-not (nil? e)
                 {::entry e})))
  :existed? (fn [_] (nil? (get @cust/customers (keyword id) ::sentinel)))
  :available-media-types ["application/json"]
  :can-put-to-missing? false
  :put! (fn [q] (cust/set-as-fraudulent (keyword id)))
  :handle-ok ::entry
  :handle-created (fn [q] (customers-resource (u/build-entry-url (q :request)))))


(defn api-routes []
  (->
    (routes
      (context "/api" []
        (ANY "/customer" [] customers-list)
        (ANY ["/customer/:id{[0-9]+}"] [id] (single-customer id))
        (ANY ["/customer/:id{[0-9]+}/fraudulent"] [id] (single-customer id))
        (ANY "/edge" [] edges)))))

