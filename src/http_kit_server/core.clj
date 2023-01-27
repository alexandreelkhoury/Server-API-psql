(ns http-kit-server.core
  (:gen-class)
  (:require [org.httpkit.server :as http]
            [reitit.ring :as ring]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.swagger :as swagger]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [reitit.coercion.spec]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            ;; [org.clojure/core.async :as async]
            [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            ;;[org.postgresql.util.PSQLException :as PSQLException]
            ))

;;  /////   DATABASE    /////

(def db
  {:dbtype "postgresql"
   :dbname "clojuredb"
   :user "alexkhoury"
   :host "localhost"
   :password "mypassword"})

(def users-sql
  "create table users"
  (jdbc/create-table-ddl :users [[:user_id :serial "PRIMARY KEY"]
                                 [:first_name "VARCHAR(16)"]
                                 [:last_name "VARCHAR(16)"]]))

(defn get-users [req]
  {:status 200
   :body (jdbc/query db ["SELECT * FROM users"])})

(defn get-user [req]
  (let [id (Integer/parseInt (:id (:path-params req)))
        res (jdbc/query db ["SELECT * FROM users where user_id = ?" id])]
    {:status 200
     :body (first res)}))

(defn add-user [req]
  (let [first_name (:first_name (:body-params req))
        last_name (:last_name (:body-params req))]
    {:status 200
     :body (jdbc/insert! db :users {:first_name first_name :last_name last_name})}))

(defn update-user [req]
  (let [id (Integer/parseInt (:id (:path-params req)))
        new_first_name (:first_name (:body-params req))
        new_last_name (:last_name (:body-params req))]
    (jdbc/update! db :users {:first_name new_first_name} ["user_id = ?" id])
    (jdbc/update! db :users {:last_name new_last_name} ["user_id = ?" id])
    {:status 200
     :body (jdbc/query db ["SELECT * FROM users where user_id = ?" id])}))

(defn delete-user [req]
  (let [id (Integer/parseInt (:id (:path-params req)))
        _ (jdbc/delete! db :users ["user_id = ?" id])]
    {:status 200}))

(defn update-first-name [req]
  (let [id (Integer/parseInt (:id (:path-params req)))
        new_first_name (:first_name (:body-params req))]
    (jdbc/update! db :users {:first_name new_first_name} ["user_id = ?" id])
    {:status 200
     :body (jdbc/query db ["SELECT * FROM users where user_id = ?" id])}))

(defn update-last-name [req]
  (let [id (Integer/parseInt (:id (:path-params req)))
        new_last_name (:last_name (:body-params req))]
    (jdbc/update! db :users {:last_name new_last_name} ["user_id = ?" id])
    {:status 200
     :body (jdbc/query db ["SELECT * FROM users where user_id = ?" id])}))
;;  /////   SERVER    /////

(def routes 
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "My API"
                            :description "My API"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/users"
    {:swagger {:tags ["Users"]}}

    [""
     {:get {:summary "Get all users"
            :handler get-users}

      :post {:summary "Add a user"
             :parameters {:body {:first_name string?
                                  :last_name string?}}
             :response {200 {:body {:user_id integer?
                                    :first_name string?
                                    :last_name string?}}}
             :handler add-user}}]

    ["/:id"
      {:get {:summary "Get user by ID"
             :parameters {:path {:id integer?}}
             :handler get-user }
       :put {:summary "Update user by ID"
              :parameters {:path {:id integer?}
                            :body {:first_name string?
                                    :last_name string?}}
             :handler update-user}
       :delete {:summary "Delete user by ID"
                :parameters {:path {:id integer?}}
                :handler delete-user}}]
    
    ["/:id/first_name"
      {:put {:summary "Update user's first name by ID"
             :parameters {:path {:id integer?}
                           :body {:first_name string?}}
             :handler update-first-name}}]
    
    ["/:id/last_name"
      {:put {:summary "Update user's last name by ID"
             :parameters {:path {:id integer?}
                           :body {:last_name string?}}
             :handler update-last-name}}]]])

(def router
  (ring/router routes
               {:data {:coercion reitit.coercion.spec/coercion
                       :muuntaja m/instance
                       :middleware [swagger/swagger-feature
                                    muuntaja/format-negotiate-middleware
                                    muuntaja/format-response-middleware
                                    exception/exception-middleware
                                    muuntaja/format-request-middleware
                                    coercion/coerce-request-middleware
                                    coercion/coerce-response-middleware]}}))

(def app 
  (ring/ring-handler router
                     (ring/routes
                      (swagger-ui/create-swagger-ui-handler 
                       {:path "/"
                        ;; :swagger-ui-url "/swagger-ui"
                        ;; :swagger-docs-url "/swagger.json"
                        }))))

(defn -main
  [& args]
  (http/run-server #'app {:port 8081})
  (println "Server started at http:/127.0.0.1:8081/")
)

