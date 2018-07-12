(defproject dmm "0.1.0-SNAPSHOT"
  :description "Data Flow Matrix Machines"
  :url "https://github.com/jsa-aerial/DMM"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;;[org.clojure/clojure "1.8.0"]
                 [quil "2.6.0"]
                 [net.mikera/vectorz-clj "0.45.0"]
                 [org.clojure/tools.nrepl  "0.2.12"] ; Explicit nREPL
                 [proto-repl "0.3.1"] ; for Atom editor

                 [http.async.client "1.2.0"]
                 [http-kit "2.2.0"]
                 [compojure "1.5.1"]
                 [org.clojure/core.async "0.4.474"]
                 
                 [clojure-msgpack "1.2.1"]
                 [org.clojure/data.json "0.2.6"]

                 [com.rpl/specter "1.1.1"]
                 
                 [aerial.fs "1.1.5"]
                 [aerial.utils "1.0.8"]

                 [expectations "2.1.8"]
                 [seesaw "1.4.5"]
                 [net.apribase/clj-dns "0.1.0"]] ; reverse-dns-lookup
                 

  :plugins [[cider/cider-nrepl "0.16.0"]
            [refactor-nrepl    "2.2.0"]
            [lein-expectations "0.0.8"]
            [lein-gorilla "0.3.6"]]


  ;;:aot :all

  :repositories
  {"lclrepo" "file:lclrepo"})
