(ns http-kit-server.handler
  (:gen-class)
  (:require [reitit.coercion.spec]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [ring.util.response :as response]
            [reitit.swagger :as swagger]))

(defn get-users [req]
  (let [{:keys [db]} req
        sql (-> (h/select :*)
                (h/from :users)
                (sql/format))]
    (response/response (jdbc/execute! db sql))))

(defn get-user [req]
  (let [{:keys [db]} req
        id (Integer/parseInt (:id (:path-params req)))
        sql (-> (h/select :*)
                (h/from :users)
                (h/where [:= :user_id id])
                (sql/format))]
    (response/response (jdbc/execute-one! db sql))))

(defn add-user [req]
  (let [{:keys [db]} req
        first_name (:first_name (:body-params req))
        last_name (:last_name (:body-params req))
        sql (-> (h/insert-into :users)
                (h/values [{:first_name first_name, :last_name last_name}])
                (sql/format))
        res (-> (h/select :*)
                (h/from :users)
                (h/where [:= :first_name first_name])
                (sql/format))]
    (jdbc/execute-one! db sql)
    (response/response (jdbc/execute! db res))))

(defn update-user [req]
  (let [{:keys [db]} req
        id (Integer/parseInt (:id (:path-params req)))
        new_first_name (:first_name (:body-params req))
        new_last_name (:last_name (:body-params req))
        sql (-> (h/update :users)
                (h/set {:first_name new_first_name :last_name new_last_name})
                (h/where [:= :user_id id])
                (sql/format))
        res (-> (h/select :*)
                (h/from :users)
                (h/where [:= :user_id id])
                (sql/format))]
    (jdbc/execute! db sql)
    (response/response (jdbc/execute! db res))))

(defn delete-user [req]
  (let [{:keys [db]} req
        id (Integer/parseInt (:id (:path-params req)))
        sql (-> (h/delete-from :users)
                (h/where [:= :user_id id])
                (sql/format))]
    (jdbc/execute! db sql)
    (response/response "User deleted !")))

(defn update-first-name [req]
  (let [{:keys [db]} req
        id (Integer/parseInt (:id (:path-params req)))
        new_first_name (:first_name (:body-params req))
        sql (-> (h/update :users)
                (h/set {:first_name new_first_name})
                (h/where [:= :user_id id])
                (sql/format))
        res (-> (h/select :*)
                (h/from :users)
                (h/where [:= :user_id id])
                (sql/format))]
    (jdbc/execute! db sql)
    (response/response (jdbc/execute! db res))))

(defn update-last-name [req]
  (let [{:keys [db]} req
        id (Integer/parseInt (:id (:path-params req)))
        new_last_name (:last_name (:body-params req))
        sql (-> (h/update :users)
                (h/set {:last_name new_last_name})
                (h/where [:= :user_id id])
                (sql/format))
        res (-> (h/select :*)
                (h/from :users)
                (h/where [:= :user_id id])
                (sql/format))]
    (jdbc/execute! db sql)
    (response/response (jdbc/execute! db res))))


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
            :handler get-user}
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