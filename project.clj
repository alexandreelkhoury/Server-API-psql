(defproject http-kit-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [http-kit "2.3.0"]
                 [org.clojure/core.async "1.6.673"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.5.1"]
                 [com.github.seancorfield/next.jdbc "1.3.847"]
                 [metosin/reitit "0.5.18"]
                 [cheshire "5.11.0"]
                 [org.clojure/data.json "2.4.0"]
                 [com.github.seancorfield/next.jdbc "1.3.847"]
                 [com.github.seancorfield/honeysql "2.4.972"]
                 [integrant "0.8.0"]]
  :main ^:skip-aot http-kit-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
