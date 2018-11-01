(ns leiningen.nrepl.core
  (:require
   [clojure.java.io :as io]
   [nrepl.server :as nrepl.server]
   [reply.main :as reply]))

(defn- require-and-resolve
  [thing]
  (require (symbol (namespace thing)))
  (resolve thing))

(def resolve-mw-xf
  (comp (map require-and-resolve)
        (keep identity)))

(defn- handle-seq-var
  [var]
  (let [x @var]
    (if (sequential? x)
      (into [] resolve-mw-xf x)
      [var])))

(def mw-xf
  (comp (map symbol)
        resolve-mw-xf
        (mapcat handle-seq-var)))

(defn- ->mw-list
  [middleware-var-strs]
  (into [] mw-xf middleware-var-strs))

(defn- build-handler
  [middleware]
  (apply nrepl.server/default-handler (->mw-list middleware)))

(def default-opts {:color true :history-file ".nrepl-history"})

(defn client [opts]
  (let [p (or (:port opts) (try (slurp ".nrepl-port") (catch Throwable _)))
        h (or (:bind opts) "127.0.0.1")
        o (assoc (merge default-opts opts) :attach (str h ":" p))]
    (assert (and h p) "host and/or port not specified for REPL client")
    (reply/launch-nrepl o)))

(defn start-nrepl
  "Starts a socket-based nREPL server. Accepts a map with the following keys:

   * :port — defaults to 0, which autoselects an open port

   * :bind — bind address, by default \"::\" (falling back to \"localhost\" if
     \"::\" isn't resolved by the underlying network stack)

   * :handler — the nREPL message handler to use for each incoming connection;
     defaults to the result of `(nrepl.server/default-handler)`

   * :middleware - a sequence of vars or string which can be resolved to vars,
     representing middleware you wish to mix in to the nREPL handler. Vars can
     resolve to a sequence of vars, in which case they'll be flattened into the
     list of middleware."
  [{:keys [handler middleware bind port] :as opts}]
  (let [handler
        (if handler
          (handler)
          (build-handler middleware))

        {:keys [server-socket port] :as server}
        (nrepl.server/start-server :handler handler
                                   :bind (or bind "localhost")
                                   :port (or port 0))

        bind
        (-> server-socket (.getInetAddress) (.getHostName))]
    (doto (io/file ".nrepl-port")
      (spit port)
      (.deleteOnExit))
    (println (format "nREPL server started on port %d on host %s - nrepl://%s:%d" port bind bind port))
    (when-not (opts :headless)
      (client opts))
    server))
