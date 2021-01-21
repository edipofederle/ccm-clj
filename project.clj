(defproject ccm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [midje "1.7.0"]
                 [compojure "1.4.0"]
                 [ring "1.2.1"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.5.0"]
                 [liberator "0.13"]
                 [org.clojure/data.csv "0.1.2"]
                 [halresource "0.2.0-SNAPSHOT"]
                 [ring/ring-mock "0.3.0"]
                 [clojurewerkz/route-one "1.1.0"]
                 [environ "1.0.1"]]
  :profiles {:dev-common {:plugins [[lein-midje "3.1.1"]
                                    [lein-ring "0.9.6"]
                                    [lein-environ "1.0.1"]]
                          }
             :dev-overrides {} ;; DO NOT CHANGE! It should only be changed in ./profiles.clj if desired
             :test-overrides {} ;; DO NOT CHANGE! It should only be changed in ./profiles.clj if desired
             :dev [:dev-common :dev-overrides]
             :test [:dev-common :test-overrides]
             :midje [:test-overrides]}

  :aliases {"test"  ["midje"]}
  :user {:plugins [[lein-midje "3.1.3"] [lein-ring "0.9.6"] [lein-environ "1.0.1"]]}
  :ring {:handler ccm.server/handler
         :adapter {:port 8080}})

