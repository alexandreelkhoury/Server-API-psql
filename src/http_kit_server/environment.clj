(ns http-kit-server.environment
  (:require
   [clojure.string :as string]))

(defmulti load-value :kind)

(defmethod load-value :static [{:keys [value]}]
  value)

(defmethod load-value :environment [{:keys [name]}]
  (System/getenv name))

(defmethod load-value :file [{:keys [path]}]
  (string/trim (slurp path)))
  