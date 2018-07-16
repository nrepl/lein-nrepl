(defproject nrepl/lein-nrepl "0.1.0-SNAPSHOT"
  :description "A lein plugin to start nREPL"
  :url "https://github.com/nrepl/lein-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true

  :dependencies [[nrepl "0.4.1"]]

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_username
                                    :password :env/clojars_password
                                    :sign-releases false}]])
