(ns http-kit-server.init
  (:require
   [http-kit-server.handler :as handler]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as jdbc-rs]
   [org.httpkit.server :as server]
   [reitit.ring :as ring]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.swagger :as swagger]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]))

(defn wrap-environment [config handler]
  (fn [request]
    (handler (merge request config))))

(defmethod ig/init-key :http/server [_ {:keys [handler] :as description}]
  (-> handler
      (server/run-server description)))

(defmethod ig/init-key :api/handler [_ config]
  (wrap-environment config
                    (ring/ring-handler
                     (ring/router
                      handler/routes
                      {:data {:coercion reitit.coercion.spec/coercion
                              :muuntaja m/instance
                              :middleware [swagger/swagger-feature
                                           muuntaja/format-negotiate-middleware
                                           muuntaja/format-response-middleware
                                           exception/exception-middleware
                                           muuntaja/format-request-middleware
                                           coercion/coerce-request-middleware
                                           coercion/coerce-response-middleware]}})
                     (ring/routes
                      (swagger-ui/create-swagger-ui-handler
                       {:path "/"})))))

(defmethod ig/init-key :db/postgresql [_ description]
  (-> description
      (jdbc/get-datasource)
      (jdbc/with-options {:builder-fn jdbc-rs/as-unqualified-kebab-maps})))
