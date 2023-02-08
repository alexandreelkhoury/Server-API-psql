(ns http-kit-server.core
  (:gen-class)
  (:require [reitit.coercion.spec]
            [integrant.core :as ig]
            [http-kit-server.init :as init]))

(defn -main [& _]
  (->
   (ig/read-string (slurp "environment-base.edn"))
   (ig/prep)
   (ig/init)))
