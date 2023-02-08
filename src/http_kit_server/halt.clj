(ns http-kit-server.halt
  (:require
   [integrant.core :as ig]))

(defmethod ig/halt-key! :http/server [_ stop-server]
  (stop-server))

(defmethod ig/halt-key! :api/handler [_ _])

(defmethod ig/halt-key! :db/postgresql [_ _])