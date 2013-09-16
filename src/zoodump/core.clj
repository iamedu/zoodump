(ns zoodump.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import [com.netflix.curator.framework CuratorFrameworkFactory CuratorFramework]
           [org.apache.zookeeper CreateMode ZooDefs ZooDefs$Ids]
           [com.netflix.curator.retry RetryOneTime])
  (:gen-class))


(defn read-map [^CuratorFramework zk ^String path]
  (let [children (.. zk (getChildren) (forPath path))]
    (if (empty? children)
      (String. (.. zk (getData) (forPath path)))
      (into {}
            (map #(vector (keyword %)
                          (read-map zk (str path "/" %)))
                 children)))))

(defn create-node [transaction ^String path ^bytes data]
  (.. transaction
      (create)
      (withMode CreateMode/PERSISTENT)
      (withACL ZooDefs$Ids/OPEN_ACL_UNSAFE)
      (forPath path data)))

(defn write-map [client ^String path data]
  (let [real-path #(str path "/" %)]
    (if (string? data)
      (do
        (println "Writing string thingies " path)
        (reset! client (.and (create-node @client path (.getBytes data "UTF-8")))))
      (doseq [[k v] data]
        (let [k (name k)]
          (println "Writing thingies " (str path "/" k))
          (if-not (string? v)
            (reset! client (.and (create-node @client (str path "/" k) (.getBytes "" "UTF-8")))))
          (write-map client (str path "/" k) v))))))

(defn export-data [opts]
  (let [{:keys [url file base]} opts
        retry-policy  (RetryOneTime. 500)
        client  (doto (CuratorFrameworkFactory/newClient url retry-policy)
                  (.start))
        data (pr-str (read-map client base))]
    (spit file data)))

(defn import-data [opts]
  (let [{:keys [url file base]} opts
        retry-policy  (RetryOneTime. 500)
        data (with-open [fr (io/reader file)
                         r (java.io.PushbackReader. fr)]
               (edn/read r))
        client  (doto (CuratorFrameworkFactory/newClient url retry-policy)
                  (.start))
        transaction (atom (.inTransaction client))]
    (reset! transaction (.and (create-node @transaction base (.getBytes "" "UTF-8"))))
    (write-map transaction base data)
    (.commit @transaction)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [[options args banner] (cli args
                                   ["-h" "--help" "Show help" :default false :flag true]
                                   ["-u" "--url" "Zookeeper url" :default "localhost:2181"]
                                   ["-a" "--action" "Action (import or export), this is mandatory"]
                                   ["-f" "--file" "File to work with" :default "data.edn"]
                                   ["-b" "--base" "Base zookeeper directory, this is mandatory"])]
    (cond
      (:help options) (println banner)
      (or (nil? (:base options))
          (nil? (:action options))) (do
                                      (println "Missing parameters")
                                      (println banner))
      (= "import" (:action options)) (import-data options)
      (= "export" (:action options)) (export-data options))))
