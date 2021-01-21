(ns ccm.utils
  (:require
   [clojurewerkz.route-one.core :refer :all])
  (:import java.net.URL))

(defn exp [x n]
  (reduce * (repeat n x)))

(defn fraudulent-url [base-url id]
  (with-base-url base-url
    (url-for "/customer/:id/fraudulent" {:id id})))

(defn build-entry-url [request]
  (URL. (format "%s://%s:%s%s"
          (name (:scheme request))
          (:server-name request)
          (:server-port request)
          "/api")))
