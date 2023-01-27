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
            [clojure.data.json :as json]))

;;  /////   DATABASE    /////

(def db
  "Reference to the database."
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

(defn get-users []
  (json/generate-string 
    {:status 200 
     :body (first (jdbc/query db ["SELECT * FROM users"]))}))

(defn get-user [id]
  (json/write-str 
  {:status 200
   :body (first (jdbc/query db ["SELECT * FROM users where user_id = ?" id]))})
  )

(defn add-user [first_name last_name]
  (jdbc/insert! db :users {:first_name first_name :last_name last_name}))

(defn update-user [old_last_name new_last_name]
  (jdbc/update! db :users {:last_name new_last_name} ["last_name = ?" old_last_name]))

(defn delete-user [id]
  (jdbc/delete! db :users ["user_id = ?" id]))

;;  /////   SERVER    /////

(def ok (constantly {:status 200 :body "ok"}))

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
                :handler delete-user}}]]])

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
  (println (get-user 7))
  (println (get-users))
  (http/run-server #'app {:port 8081})
  (println "Server started at http:/127.0.0.1:8081/")
)

