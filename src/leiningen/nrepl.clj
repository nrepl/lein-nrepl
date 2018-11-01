(ns leiningen.nrepl
  (:require
   [leiningen.core.eval :as leval]
   [leiningen.core.project :as lproject]
   [leiningen.nrepl.core :as nrepl-core]))


(defn convert-args
  "Convert the args list to a map."
  [args]
  (->> args
       (map read-string)
       (partition 2)
       (map vec)
       (into {})
       (merge {:block true})))

(def nrepl-profile {:dependencies [['nrepl/lein-nrepl "0.3.0-SNAPSHOT"]]})

(defn nrepl
  "Start a headless nREPL server within your project's context.

  Accepts the following params:

   * :port — defaults to 0, which autoselects an open port

   * :bind — bind address, by default \"::\" (falling back to \"localhost\" if
     \"::\" isn't resolved by the underlying network stack)

   * :handler — the nREPL message handler to use for each incoming connection;
     defaults to the result of `(nrepl.server/default-handler)`

   * :middleware - a sequence of vars or string which can be resolved to vars,
     representing middleware you wish to mix in to the nREPL handler. Vars can
     resolve to a sequence of vars, in which case they'll be flattened into the
     list of middleware.

  * `:headless` - Defaults to `false`. Controls whether to start an interactive
     REPL (powered by REPLy) or not.

   * :block - Defaults to `true`. Set it to `false` for relinquishing control
     to the next Leiningen task: e.g `lein do nrepl :block false, test-refresh`.
     Note that with a `false` value and no next Lein task to run,
     lein-nrepl will immediately close. This option is ignored unless `:headless`
     is also true.

  All of them are collected converted to Clojure data structures, collected into a
  map and passed to `start-nrepl`."
  [project & args]
  (let [profile (or (:nrepl (:profiles project)) nrepl-profile)
        project (lproject/merge-profiles project [profile])
        arg-map (convert-args args)]
    (leval/eval-in-project
     project
     `(nrepl-core/start-nrepl ~arg-map)
     '(require 'leiningen.nrepl.core))
    (when (and (:block arg-map) (:headless arg-map))
      ;; block forever, so the process won't end after the server was started
      @(promise))))
