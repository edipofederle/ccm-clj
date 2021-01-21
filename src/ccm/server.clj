(ns ccm.server
  (:require
   [ring.adapter.jetty :as jetty]
   [ccm.api-routes :as ar])
  (:use
   [ring.util.response :only [header]]
   [compojure.handler :only [api]]))

(def handler
  (-> (ar/api-routes)
      api))
